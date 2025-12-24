package com.jys.smartbudget.batch.job;

import com.jys.smartbudget.dto.BatchBudgetFailHistory;
import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.mapper.BatchBudgetFailHistoryMapper;
import com.jys.smartbudget.mapper.BudgetMapper;
import com.jys.smartbudget.mapper.ExpenseMapper;
import com.jys.smartbudget.mapper.UserMapper;
import com.jys.smartbudget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MonthlyBudgetJobConfig {

    private final ExpenseMapper expenseMapper;
    private final BudgetMapper budgetMapper;
    private final BudgetService budgetService;
    private final UserMapper userMapper;  
    private final BatchBudgetFailHistoryMapper batchBudgetFailHistoryMapper;  

    @Bean
    public Job monthlyBudgetJob(JobRepository jobRepository, Step calculateMonthlyExpenseStep) {
        return new JobBuilder("monthlyBudgetJob", jobRepository)
                .start(calculateMonthlyExpenseStep)
                .build();
    }

    @Bean
    public Step calculateMonthlyExpenseStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("calculateMonthlyExpenseStep", jobRepository)
                .<String, List<BudgetDTO>>chunk(10, transactionManager) 
                .reader(userItemReader())           
                .processor(userExpenseProcessor())  
                .writer(budgetItemWriter())
                .build();
    }

    /**
     * Reader: 모든 사용자 ID 조회
     */
    @Bean
    public ListItemReader<String> userItemReader() {
        // 모든 사용자 ID 조회
        List<String> userIds = userMapper.selectAllUserIds();
        log.info("전체 사용자 수: {}명", userIds.size());
        
        return new ListItemReader<>(userIds);
    }

    /**
     * Processor: 각 사용자의 지난달 지출 → 다음달 예산 변환
     */
    @Bean
    public ItemProcessor<String, List<BudgetDTO>> userExpenseProcessor() {
        return userId -> {
            log.info("사용자 {} 처리 시작", userId);

            // 기준 날짜 계산
            LocalDate now = LocalDate.now();
            LocalDate lastMonth = now.minusMonths(1);
            LocalDate nextMonth = now.plusMonths(1);

            int lastYear = lastMonth.getYear();
            int lastMonthValue = lastMonth.getMonthValue();

            int nextYear = nextMonth.getYear();
            int nextMonthValue = nextMonth.getMonthValue();

            // 1. 지난달 지출 조회
            ExpenseDTO condition = new ExpenseDTO();
            condition.setUserId(userId);
            condition.setYear(lastYear);
            condition.setMonth(lastMonthValue);

            List<ExpenseDTO> expenses = expenseMapper.searchExpenses(condition);
 
            if (expenses.isEmpty()) {
                log.info(" → 지출 내역 없음, 건너뜀");
                return null;
            }

            // 2. 카테고리별 지출 합계
            Map<String, Integer> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                    ExpenseDTO::getCategoryCode,
                    Collectors.summingInt(ExpenseDTO::getAmount)
                ));

            // 3. 다음달 예산 생성
            List<BudgetDTO> budgets = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : categoryTotals.entrySet()) {
                BudgetDTO budget = new BudgetDTO();
                budget.setUserId(userId);
                budget.setCategory(entry.getKey());
                budget.setAmount(entry.getValue() + 50_000); // 여유 금액
                budget.setYear(nextYear);
                budget.setMonth(nextMonthValue);
                budget.setBudgetDescription(
                    String.format("%d년 %d월 지출 기반 자동 생성",
                        lastYear, lastMonthValue)
                );

                budgets.add(budget);

                log.info(
                    " → [{}] {}원 → {}원",
                    entry.getKey(),
                    entry.getValue(),
                    budget.getAmount()
                );
            }

            return budgets;
        };
    }


    /**
     * Writer: 예산 저장
     */
    @Bean
    public ItemWriter<List<BudgetDTO>> budgetItemWriter() {
        return budgetLists -> {
            int insertCount = 0;
            int skipCount = 0;

            for (List<BudgetDTO> budgets : budgetLists) {
                if (budgets == null) continue;

                for (BudgetDTO budget : budgets) {

                    // 이미 예산이 존재하면 건너뜀
                    if (budgetService.existsByYearMonthCategory(budget)) {
                        skipCount++;
                        log.info(
                            "이미 예산 존재 → 스킵 (user={}, {}-{}, {})",
                            budget.getUserId(),
                            budget.getYear(),
                            budget.getMonth(),
                            budget.getCategory()
                        );
                        continue;
                    }
                    try {
                        budgetMapper.insertBudget(budget);
                        insertCount++;
                    } catch (Exception e) {
                        batchBudgetFailHistoryMapper.insert(
                            BatchBudgetFailHistory.builder()
                                .jobName("monthlyBudgetJob")
                                .userId(budget.getUserId())
                                .year(budget.getYear())
                                .month(budget.getMonth())
                                .category(budget.getCategory())
                                .reason(e.getMessage())
                                .build()
                        );

                        log.error("배치 예산 생성 실패", e);
                    }
                }
            }

            log.info("배치 결과: 생성 {}건, 스킵 {}건", insertCount, skipCount);
        };
    }

}