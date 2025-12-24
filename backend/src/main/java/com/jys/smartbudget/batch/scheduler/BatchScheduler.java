package com.jys.smartbudget.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job monthlyBudgetJob;

    /**
     * 매월 1일 오전 2시에 실행
     * - 전월 지출 데이터 기반으로 이번 달 예산 자동 생성
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void runMonthlyBudgetJob() {
        try {
            log.info("=== 월별 예산 배치 작업 시작 ===");
            
            // 매번 다른 JobParameters로 실행 (중복 방지)
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
            
            jobLauncher.run(monthlyBudgetJob, params);
            
            log.info("=== 월별 예산 배치 작업 완료 ===");
            
        } catch (Exception e) {
            log.error("배치 작업 실패: {}", e.getMessage(), e);         //실무라면 어딘가 테이블에 남겨야되지않나??
        }
    }
}