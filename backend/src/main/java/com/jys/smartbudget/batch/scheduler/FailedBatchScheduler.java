package com.jys.smartbudget.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.jys.smartbudget.mapper.BatchBudgetMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class FailedBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job failedBudgetReprocessJob;
    private final BatchBudgetMapper batchBudgetMapper;

    @Scheduled(cron = "0 30 2 1 * ?") // 매월 1일 02:30
    public void runFailedBudgetReprocessJobIfNeeded() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {

        int failCount = batchBudgetMapper.countUnprocessedFailHistories();

        if (failCount == 0) {
            log.info("재배치 대상 없음 → Job 실행 스킵");
            return;
        }

        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

        log.info("재배치 대상 {}건 → 재배치 Job 실행", failCount);

        jobLauncher.run(failedBudgetReprocessJob, params);


    }
}
