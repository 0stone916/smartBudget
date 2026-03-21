package com.jys.smartbudget.batch.config;

import com.jys.smartbudget.batch.reader.BankApiItemReader;
import com.jys.smartbudget.dto.BankTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Configuration
@RequiredArgsConstructor
public class ReconciliationBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BankApiItemReader bankApiItemReader; // StepScope 빈 주입

    @Bean // Bean으로 등록해둬서 scheduler에서 사용하기 위함.
    public Job reconciliationJob(Step reconciliationStep) {
        return new JobBuilder("reconciliationJob", jobRepository)
                .start(reconciliationStep)
                .build();
    }

    @Bean
    @JobScope // Step에 파라미터를 쓰려면 필요.
    public Step reconciliationStep(
            ItemProcessor<BankTransactionResponse, Object> processor,
            ItemWriter<Object> writer) {

        return new StepBuilder("reconciliationStep", jobRepository)
                .<BankTransactionResponse, Object>chunk(100, transactionManager)
                .reader(bankApiItemReader) // 이미 빈으로 등록된 reader 사용
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                // 1. 재시도 설정 (Retry)
                .retryLimit(3)
                .retry(ResourceAccessException.class)
                .retry(HttpServerErrorException.class)
                .backOffPolicy(new FixedBackOffPolicy() {
                    {
                        setBackOffPeriod(2000L);
                    }
                })
                // 2. 건너뛰기 설정 (Skip)
                .skipLimit(100)
                .skip(Exception.class)
                .build();
    }
}