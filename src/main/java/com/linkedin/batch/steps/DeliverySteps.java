package com.linkedin.batch.steps;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class DeliverySteps {
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step driveToAddressStep() {
        boolean GOT_LOST = false;
        return this.stepBuilderFactory.get("driveToAddressStep").tasklet(
                (stepContribution, chunkContext) -> {
                    if(GOT_LOST) {
                        throw new RuntimeException("Got lost driving to address");
                    }

                    System.out.println("Successfully arrived at the address.");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step storePackageStep() {
        return this.stepBuilderFactory.get("storePackageStep").tasklet(
                (stepContribution, chunkContext) -> {
                    System.out.println("Storing the package while the customer address is located.");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step leaveAtDoorStep() {
        return this.stepBuilderFactory.get("leaveAtDoorStep").tasklet(
                (stepContribution, chunkContext) -> {
                    System.out.println("Leaving the package at the door.");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step givePackageToCustomerStep() {
        return this.stepBuilderFactory.get("givePackageToCustomerStep").tasklet(
                (stepContribution, chunkContext) -> {
                    System.out.println("Given the package to the customer.");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step thankCustomerStep() {
        return this.stepBuilderFactory.get("thankCustomerStep").tasklet(
                (stepContribution, chunkContext) -> {
                    System.out.println("Thank you customer!");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step giveRefundStep() {
        return this.stepBuilderFactory.get("giveRefundStep").tasklet(
                (stepContribution, chunkContext) -> {
                    System.out.println("Please forgive us... Here is your refund.");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step packageItemStep() {
        return this.stepBuilderFactory.get("packageItemStep").tasklet(
                (stepContribution, chunkContext) -> {
                    String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
                    String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();
                    System.out.printf("The %s has been packaged on %s.%n", item, date);
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
