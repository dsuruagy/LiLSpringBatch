package com.linkedin.batch;

import com.linkedin.batch.reader.SimpleItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@EnableBatchProcessing
@SpringBootApplication
public class ChunkBatchApplication {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("job")
                .start(chunkBasedStep())
                .build();
    }

    @Bean
    public ItemReader<String> itemReader() {
        return new SimpleItemReader();
    }

    @Bean
    public Step chunkBasedStep() {
        return this.stepBuilderFactory.get("chunkBasedStep")
                .<String, String> chunk(3)
                .reader(itemReader())
                .writer(list -> {
                            System.out.printf("Received list of size: %s\n", list.size());
                            list.forEach(System.out::println);
                        })
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(ChunkBatchApplication.class, args);
    }
}
