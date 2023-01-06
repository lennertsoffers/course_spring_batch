package com.christianoette._A_the_basics._01_hello_world;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@SpringBootTest(classes = HelloWorldTest.TestConfig.class)
class HelloWorldTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void test() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addParameter("outputText", new JobParameter("Hello Spring Batch"))
                .toJobParameters();

        jobLauncherTestUtils.launchJob(jobParameters);
    }

    @Configuration
    @EnableBatchProcessing
    @AllArgsConstructor
    static class TestConfig {
        private JobBuilderFactory jobBuilderFactory;
        private StepBuilderFactory stepBuilderFactory;

        @Bean
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

        @Bean
        public JobLauncherTestUtils utils() {
            return new JobLauncherTestUtils();
        }
    }
}
