package com.christianoette._A_the_basics._05_steps_in_separate_files.config;

import com.christianoette._A_the_basics._05_steps_in_separate_files.dto.InputData;
import com.christianoette._A_the_basics._05_steps_in_separate_files.dto.OutputData;
import com.christianoette._A_the_basics._05_steps_in_separate_files.processor.UpperCaseJsonProcessor;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.batch.item.ItemReader;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@AllArgsConstructor
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final JobRepository jobRepository;
    private final StepBuilderFactory stepBuilderFactory;
    private final ItemReader<InputData> myItemReader;
    private final UpperCaseJsonProcessor upperCaseJsonProcessor;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("upperCaseProcessingJob")
                .start(step())
                .build();
    }

    @Bean
    public Step step() {
        SimpleStepBuilder<InputData, OutputData> chunk =
                stepBuilderFactory.get("jsonItemReader")
                .repository(jobRepository)
                .chunk(1);

        return chunk.reader(this.myItemReader)
                .processor(this.upperCaseJsonProcessor)
                .writer(writer(null))
                .build();
    }

    @Bean
    @StepScope
    public JsonFileItemWriter<OutputData> writer(@Value("#{jobParameters['outputPath']}") String outputPath) {
        Resource outputResource = new FileSystemResource(outputPath);

        return new JsonFileItemWriterBuilder<OutputData>()
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(outputResource)
                .name("jsonItemWriter")
                .build();
    }
}
