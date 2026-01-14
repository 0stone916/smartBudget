package com.jys.smartbudget.batch.job;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jys.smartbudget.dto.BatchBudgetFailHistory;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ReprocessFailedBudgetJobConfig {

    private final SqlSessionFactory sqlSessionFactory;

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
}
