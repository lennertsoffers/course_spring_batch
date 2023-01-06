package com.christianoette._A_the_basics._01_hello_world_application.jobconfig;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@AllArgsConstructor
public class JobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @MyJob
    public Job helloWorldJob() {
        Step step = this.stepBuilderFactory.get("step")
                .tasklet((stepContribution, chunkContext) -> {
                    Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
                    Object outputText = jobParameters.get("outputText");
                    System.out.println(outputText);
                    return RepeatStatus.FINISHED;
                })
                .build();

        return this.jobBuilderFactory.get("helloWorldJob")
                .start(step)
                .build();
    }
}
