package com.example.weather.hongkong;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

@Configuration
class BatchConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    Step importRainfallStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   RainfallReader reader,
                                   ChangeDetectionProcessor processor,
                                   FileWriter writer) {
        return new StepBuilder("importRainfallStep", jobRepository)
            .<RainfallData, RainfallData>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }

    @Bean
    Job rainfallSyncJob(JobRepository jobRepository, Step importRainfallStep) {
        return new JobBuilder("rainfallSyncJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(importRainfallStep)
            .build();
    }
}
