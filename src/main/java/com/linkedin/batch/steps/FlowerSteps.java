package com.linkedin.batch.steps;

import com.linkedin.batch.listener.FlowersSelectionStepExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class FlowerSteps {
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public FlowersSelectionStepExecutionListener selectFlowerListener() {
        return new FlowersSelectionStepExecutionListener();
    }

    @Bean
    public Step selectFlowersStep() {
        return this.stepBuilderFactory.get("selectFlowersStep").tasklet(
                (stepContribution, chunkContext) -> {
                    System.out.println("Gathering Flowers for order");
                    return RepeatStatus.FINISHED;
                }).listener(selectFlowerListener()).build();
    }

    @Bean
    public Step arrangeFlowersStep() {
        return this.stepBuilderFactory.get("arrangeFlowersStep").tasklet(
                (stepContribution, chunkContext) -> {
                    System.out.println("Arranging flowers for order.");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    public Step removeThornsStep() {
        return this.stepBuilderFactory.get("arrangeFlowersStep").tasklet(
                (stepContribution, chunkContext) -> {
                    System.out.println("Remove thorns from roses.");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
