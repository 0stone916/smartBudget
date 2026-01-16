package com.jys.smartbudget.batch.job;

import java.util.List;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import com.jys.smartbudget.batch.processor.FailedBudgetReprocessProcessor;
import com.jys.smartbudget.batch.writer.FailedBudgetReprocessWriter;
import com.jys.smartbudget.dto.BatchBudgetFailHistory;
import com.jys.smartbudget.dto.BudgetDTO;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ReprocessFailedBudgetJobConfig {

    private final SqlSessionFactory sqlSessionFactory;
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;

    private final FailedBudgetReprocessWriter failedBudgetReprocessWriter;
    private final FailedBudgetReprocessProcessor failedBudgetReprocessProcessor;

    @Bean
    public MyBatisPagingItemReader<BatchBudgetFailHistory> failedBudgetReader() {
        MyBatisPagingItemReader<BatchBudgetFailHistory> reader =
            new MyBatisPagingItemReader<>();

        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId(
            "com.jys.smartbudget.mapper.BatchBudgetMapper.selectUnprocessedFailHistories"
        );
        reader.setPageSize(10);

        return reader;
    }

    @Bean
    public Step failedBudgetReprocessStep() {
        return new StepBuilder("failedBudgetReprocessStep", jobRepository)
            .<BatchBudgetFailHistory, List<BudgetDTO>>chunk(10, transactionManager)
            .reader(failedBudgetReader())
            .processor(failedBudgetReprocessProcessor)
            .writer(failedBudgetReprocessWriter)
            .build();
    }

    @Bean
    public Job failedBudgetReprocessJob() {
        return new JobBuilder("failedBudgetReprocessJob", jobRepository)
            .start(failedBudgetReprocessStep())
            .build();
    }
}
