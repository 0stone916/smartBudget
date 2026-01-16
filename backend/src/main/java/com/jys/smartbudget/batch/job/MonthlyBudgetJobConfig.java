package com.jys.smartbudget.batch.job;

import com.jys.smartbudget.batch.listener.BatchSkipListener;
import com.jys.smartbudget.batch.listener.BatchSummaryStepListener;
import com.jys.smartbudget.batch.support.BudgetCalculationHelper;
import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.mapper.BudgetMapper;
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
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MonthlyBudgetJobConfig {

    private final BudgetMapper budgetMapper;
    private final BudgetService budgetService;
    private final UserMapper userMapper;  
    private final BatchSkipListener batchSkipListener;  
    private final BatchSummaryStepListener batchSummaryStepListener;  
    private final BudgetCalculationHelper budgetCalculationHelper;

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
                .listener(batchSummaryStepListener) // 요약 
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

            YearMonth now = YearMonth.now();
            YearMonth baseYm = now.minusMonths(1);
            YearMonth targetYm = now;

            log.info("사용자 {} / 기준월 {} / 대상월 {}", userId, baseYm, targetYm);

            List<BudgetDTO> budgets =
                budgetCalculationHelper.calculateBudgets(userId, baseYm, targetYm);

            if (budgets.isEmpty()) {
                auditLog.info(
                    "AUTO_BUDGET_NO_EXPENSE user={} reason=NO_EXPENSE baseYm={}",
                    userId, baseYm
                );
                return null;
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
            int businessSkipCount = 0;

            for (List<BudgetDTO> budgets : budgetLists) {
                if (budgets == null) continue;

                for (BudgetDTO budget : budgets) {

                    // 이미 예산이 존재하면 건너뜀
                    if (budgetService.existsByYearMonthCategory(budget)) {
                        businessSkipCount++;
                        auditLog.info(
                            "AUTO_BUDGET_BUSINESS_SKIPPED user={} year={} month={} category={} reason=ALREADY_EXISTS",
                            budget.getUserId(),
                            budget.getYear(),
                            budget.getMonth(),
                            budget.getCategory().getCode()
                        );
                        continue;
                    }

                    // ===== 테스트용 실패 유도 =====
                    // testuser + FOOD 카테고리만 강제 실패
                    if ("testuser".equals(budget.getUserId())
                        && "FOOD".equals(budget.getCategory().getCode())) {

                        throw new RuntimeException("TEST_FORCE_FAIL");
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

            log.info("배치 결과: 생성 {}건, 비지니스스킵 {}건", insertCount, businessSkipCount);
        };
    }
}