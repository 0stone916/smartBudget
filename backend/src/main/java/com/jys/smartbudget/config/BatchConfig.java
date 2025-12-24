package com.jys.smartbudget.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing // Spring Batch 기능 활성화
public class BatchConfig {
    // 기본 설정 외 특이사항 없을 시 비워두어도 무방
}