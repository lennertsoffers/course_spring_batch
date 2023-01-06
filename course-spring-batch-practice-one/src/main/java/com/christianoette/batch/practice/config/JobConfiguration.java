package com.christianoette.batch.practice.config;

import com.christianoette.batch.dontchangeit.utils.CourseUtils;
import com.christianoette.batch.practice.FileHandlingJobExecutionListener;
import com.christianoette.batch.practice.model.Person;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class JobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public JobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job job(FileHandlingJobExecutionListener listener) {
        return this.jobBuilderFactory
                .get("anonymizeJob")
                .start(this.step())
                .listener(listener)
                .validator(new AnonymizeJobParameterValidator())
                .build();
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory
                .get("anonymizeStep")
                .<Person, Person>chunk(1)
                .reader(this.reader(null))
                .processor(this.processor(null))
                .writer(this.writer(null))
                .build();
    }

    @Bean
    @StepScope
    public JsonItemReader<Person> reader(@Value(AnonymizeJobParameterKeys.INPUT_PATH_REFERENCE) String inputPath) {
        FileSystemResource resource = CourseUtils.getFileResource(inputPath);

        return new JsonItemReaderBuilder<Person>()
                .name("jsonItemReader")
                .resource(resource)
                .jsonObjectReader(new JacksonJsonObjectReader<>(Person.class))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Person, Person> processor(@Value(AnonymizeJobParameterKeys.ANONYMIZE_REFERENCE) String anonymize) {
        return input -> {
            if (!input.isCustomer) return null;

            Person output = new Person();

            if (anonymize != null && anonymize.equals("true")) {
                output.email = "";
                output.name = "John Doe";
            } else {
                output.email = input.email;
                output.name = input.name;
            }

            output.birthday = input.birthday;
            output.isCustomer = true;
            output.revenue = input.revenue;

            return output;
        };
    }

    @Bean
    @StepScope
    public JsonFileItemWriter<Person> writer(@Value(AnonymizeJobParameterKeys.OUTPUT_PATH_REFERENCE) String outputPath) {
        FileSystemResource resource = CourseUtils.getFileResource(outputPath);

        return new JsonFileItemWriterBuilder<Person>()
                .name("jsonItemWriter")
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(resource)
                .build();
    }
}
