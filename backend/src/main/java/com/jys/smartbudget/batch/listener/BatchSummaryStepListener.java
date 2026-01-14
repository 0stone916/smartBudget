package com.jys.smartbudget.batch.listener;

import java.time.YearMonth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import com.jys.smartbudget.dto.BatchJobSummary;
import com.jys.smartbudget.mapper.BatchBudgetMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BatchSummaryStepListener implements StepExecutionListener {

    private final BatchBudgetMapper batchBudgetMapper;
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        long successCount = stepExecution.getWriteCount();

        long skipCount =
                stepExecution.getReadSkipCount()
            + stepExecution.getProcessSkipCount()
            + stepExecution.getWriteSkipCount();

        long failCount = stepExecution.getSkipCount();

        YearMonth now = YearMonth.now();
        YearMonth baseYm = now.minusMonths(1);
        YearMonth targetYm = now;

        batchBudgetMapper.insertBatchJobSummary(
            BatchJobSummary.builder()
                .jobName(stepExecution.getJobExecution().getJobInstance().getJobName())
                .baseYear(baseYm.getYear())
                .baseMonth(baseYm.getMonthValue())
                .targetYear(targetYm.getYear())
                .targetMonth(targetYm.getMonthValue())
                .successCount(successCount)
                .skipCount(skipCount)
                .failCount(failCount)
                .build()
        );

        auditLog.info(
            "BATCH_SUMMARY job={} base={} target={} success={} skip={} fail={}",
            stepExecution.getJobExecution().getJobInstance().getJobName(),
            baseYm,
            targetYm,
            successCount,
            skipCount,
            failCount
        );

        return stepExecution.getExitStatus();
    }

}
