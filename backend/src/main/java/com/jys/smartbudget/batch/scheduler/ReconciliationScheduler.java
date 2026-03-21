package com.jys.smartbudget.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.jys.smartbudget.dto.AccountDto;
import com.jys.smartbudget.mapper.AccountMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationScheduler {

    private final JobLauncher jobLauncher;
    private final Job reconciliationJob; // config에서 등록해둔 reconciliationJob이름의 Job을 가져와 사용.
    private final AccountMapper accountMapper;

    /**
     * 매일 00시 00분 00초에 전일자 지출 내역 대조 배치 실행
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyReconciliation() {

        // 1. 실행 시점 기준 어제 날짜 계산 (T-1 정산)
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        List<AccountDto> accountList = accountMapper.getAccounts();

        log.info("=== [일일 정산 시작] 대상 날짜: {}, 총 계좌 수: {}건 ===", yesterday, accountList.size());

        for (AccountDto account : accountList) {

            try {
                log.info("정산 배치 자동 스케줄링 시작 - 대상 날짜: {}", yesterday);

                // 2. JobParameters 설정
                // targetDate와 accountNumber를 'identifying=true'로 설정하여 실패 시 해당 지점부터 재시작 가능하게 함
                JobParameters params = new JobParametersBuilder()
                        .addString("targetDate", yesterday, true)
                        .addString("accountNumber", account.getAccountNumber(), true)
                        .addLocalDateTime("runAt", LocalDateTime.now(), false) // 실행 기록용 (비식별)
                        .toJobParameters();

                // 3. 배치 실행
                jobLauncher.run(reconciliationJob, params);

                log.info("정산 완료 - 계좌: {}, 날짜: {}", account.getAccountNumber(), yesterday);

            } catch (JobInstanceAlreadyCompleteException e) { // targetDate와 accountNumber로 구분되는 동일 배치 작업 방지.
                log.warn("이미 완료된 작업 - 계좌: {}, 날짜: {}", account.getAccountNumber(), yesterday);
            } catch (Exception e) {
                log.error("계좌 [{}] 정산 중 오류 발생: {}", account.getAccountNumber(), e.getMessage(), e);
            }

        }
    }
}