package com.jys.smartbudget.batch.job;

import com.jys.smartbudget.batch.listener.BatchSkipListener;
import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.mapper.BudgetMapper;
import com.jys.smartbudget.mapper.ExpenseMapper;
import com.jys.smartbudget.mapper.UserMapper;
import com.jys.smartbudget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.YearMonth;
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
    private final BatchSkipListener batchSkipListener;  
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

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
                .faultTolerant()              //장애 허용 모드 ON
                .skip(Exception.class)   // 어떤 예외를 스킵할지
                .skipLimit(1000)         // 최대 스킵 허용 개수
                .listener(batchSkipListener)
                .build();
    }

    /**
     * Reader: 모든 사용자 ID 조회
     */
    @Bean
    public ListItemReader<String> userItemReader() {
        // 모든 사용자 ID 조회
        List<String> userIds = userMapper.selectAutoBudgetTargetUserIds();
        log.info("자동 예산 생성 사용자 수: {}명", userIds.size());
        return new ListItemReader<>(userIds);
    }

    /**
     * Processor: 각 사용자의 지난달 지출 → 다음달 예산 변환
     */
    @Bean
    public ItemProcessor<String, List<BudgetDTO>> userExpenseProcessor() {
        return userId -> {
            // 1. 배치 실행 시점 기준
            YearMonth now = YearMonth.now(); 
            YearMonth baseYm = now.minusMonths(1);
            YearMonth targetYm = now; 

            log.info("사용자 {} / 기준월 {} / 대상월 {}", userId, baseYm, targetYm);

            // 2. 기준월 지출 조회
            ExpenseDTO condition = new ExpenseDTO();
            condition.setUserId(userId);
            condition.setYear(baseYm.getYear());
            condition.setMonth(baseYm.getMonthValue());

            List<ExpenseDTO> expenses = expenseMapper.searchExpenses(condition);

            if (expenses.isEmpty()) {
                auditLog.info(
                    "AUTO_BUDGET_SKIPPED user={} reason=NO_EXPENSE baseYm={}",
                    userId, baseYm
                );
                return null;
            }

            // 3. 카테고리별 합계
            Map<String, Integer> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                    expense -> (expense.getCategory() != null) ? expense.getCategory().getCode() : "UNKNOWN",
                    Collectors.summingInt(ExpenseDTO::getAmount)
                ));

            // 4. 다음 달 예산 생성
            List<BudgetDTO> budgets = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : categoryTotals.entrySet()) {
                BudgetDTO budget = new BudgetDTO();
                budget.setUserId(userId);
                budget.getCategory().setCode(entry.getKey());
                budget.setAmount(entry.getValue() + 50_000);
                budget.setYear(targetYm.getYear());
                budget.setMonth(targetYm.getMonthValue());
                budget.setDescription(
                    String.format("%d년 %d월 지출 기반 자동 생성",
                        baseYm.getYear(), baseYm.getMonthValue())
                );

                budgets.add(budget);

                log.info(
                    " → (user={}) [{}] {}원 → {}원",
                    userId,
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

                    // if ("FOOD".equals(budget.getCategory())                          BatchSkipListener를 통한 BatchBudgetFailHistory 테스트용
                    //         && "user1".equals(budget.getUserId())) {
                    //         throw new RuntimeException("강제 실패 테스트");
                    //     }


                    // 이미 예산이 존재하면 건너뜀
                    if (budgetService.existsByYearMonthCategory(budget)) {
                        skipCount++;
                        auditLog.info(
                            "AUTO_BUDGET_SKIPPED user={} year={} month={} category={} reason=ALREADY_EXISTS",
                            budget.getUserId(),
                            budget.getYear(),
                            budget.getMonth(),
                            budget.getCategory().getCode()
                        );
                        continue;
                    }
                        budgetMapper.insertBudget(budget);
                        insertCount++;

                        auditLog.info(
                            "AUTO_BUDGET_CREATED user={} year={} month={} category={} amount={}",
                            budget.getUserId(),
                            budget.getYear(),
                            budget.getMonth(),
                            budget.getCategory().getCode(),
                            budget.getAmount()
                        );
                }
            }

            log.info("배치 결과: 생성 {}건, 스킵 {}건", insertCount, skipCount);
        };
    }


}