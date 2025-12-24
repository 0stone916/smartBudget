package com.jys.smartbudget.batch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MonthlyBudgetJobTest {


    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job monthlyBudgetJob;

    @Test
    public void Job_실행_성공_확인() throws Exception {
        jobLauncherTestUtils.setJob(monthlyBudgetJob);

        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        Assertions.assertEquals(
            ExitStatus.COMPLETED,
            jobExecution.getExitStatus()
        );
    }
}
