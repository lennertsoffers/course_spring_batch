package com.christianoette.batch.practice.config;

import com.christianoette.batch.practice.FileHandlingJobExecutionListener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.contentOf;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(classes = {JobConfigurationTest.TestConfig.class, JobConfiguration.class})
class JobConfigurationTest {
    @Autowired
    private Job job;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @MockBean
    private FileHandlingJobExecutionListener fileHandlingJobExecutionListener;

    @Test
    void happyCaseTest() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(AnonymizeJobParameterKeys.INPUT_PATH, "classpath:unitTestData/persons.json")
                .addString(AnonymizeJobParameterKeys.OUTPUT_PATH, "output/unitTestOutput.json")
                .addString(AnonymizeJobParameterKeys.ERROR_PATH, "ignored")
                .addString(AnonymizeJobParameterKeys.UPLOAD_PATH, "ignored")
                .toJobParameters();

        JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        String outputContent = contentOf(new File("output/unitTestOutput.json"));
        assertThat(outputContent).contains("Wei Lang");
        assertThat(outputContent).doesNotContain("Daliah Shah");
        Mockito.verify(this.fileHandlingJobExecutionListener).beforeJob(jobExecution);
        Mockito.verify(this.fileHandlingJobExecutionListener).afterJob(jobExecution);
    }

    @Test
    void anonymizeTest() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(AnonymizeJobParameterKeys.INPUT_PATH, "classpath:unitTestData/persons.json")
                .addString(AnonymizeJobParameterKeys.OUTPUT_PATH, "output/unitTestOutput.json")
                .addString(AnonymizeJobParameterKeys.ERROR_PATH, "ignored")
                .addString(AnonymizeJobParameterKeys.UPLOAD_PATH, "ignored")
                .addString(AnonymizeJobParameterKeys.ANONYMIZE_DATA, "true")
                .toJobParameters();

        JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        String outputContent = contentOf(new File("output/unitTestOutput.json"));
        assertThat(outputContent).contains("John Doe");
        assertThat(outputContent).doesNotContain("Wei Lang");
        assertThat(outputContent).doesNotContain("Wei.Lang@domain.xyz");
        assertThat(outputContent).doesNotContain("Daliah Shah");
        assertThat(outputContent).doesNotContain("Deliah.Shah@domain.xyz");
        Mockito.verify(this.fileHandlingJobExecutionListener).beforeJob(jobExecution);
        Mockito.verify(this.fileHandlingJobExecutionListener).afterJob(jobExecution);
    }

    @Test
    void testInvalidParametersThrowsException() throws Exception {
        assertThatThrownBy(() -> this.jobLauncherTestUtils.launchJob(new JobParameters()))
                .isInstanceOf(JobParametersInvalidException.class)
                .hasMessageContaining("The JobParameters do not contain required keys");
    }

    @Configuration
    @EnableBatchProcessing
    static class TestConfig {
        @Bean
        public JobLauncherTestUtils jobLauncherTestUtils() {
            return new JobLauncherTestUtils();
        }
    }
}
