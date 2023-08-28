package com.linkedin.batch;

import com.linkedin.batch.decider.DeliveryDecider;
import com.linkedin.batch.decider.itemCorrectDecider;
import com.linkedin.batch.listener.FlowersSelectionStepExecutionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class LinkedinBatchApplication {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public JobExecutionDecider decider() {
		return new DeliveryDecider();
	}

	@Bean
	public JobExecutionDecider correctDecider() {
		return new itemCorrectDecider();
	}

	@Bean
	public Flow deliveryFlow() {
		return new FlowBuilder<SimpleFlow>("deliveryFlow").start(driveToAddressStep())
				.on("FAILED").fail()
				.from(driveToAddressStep())
				.on("*").to(decider())
				.on("PRESENT").to(givePackageToCustomerStep())
				.next(correctDecider()).on("CORRECT").to(thankCustomerStep())
				.from(correctDecider()).on("INCORRECT").to(giveRefundStep())
				.from(decider())
				.on("NOT_PRESENT").to(leaveAtDoorStep()).build();
	}

	@Bean
	public Job deliverPackageJob() {
		return this.jobBuilderFactory.get("deliverPackageJob")
				.start(packageItemStep())
				.on("*").to(deliveryFlow())
				.end()
				.build();
	}

	@Bean
	public Job prepareFlowers() {
		return this.jobBuilderFactory.get("prepareFlowersJob")
				.start(selectFlowersStep())
				.on("TRIM_REQUIRED").to(removeThornsStep()).next(arrangeFlowersStep())
				.from(selectFlowersStep())
				.on("NO_TRIM_REQUIRED").to(arrangeFlowersStep())
				.from(arrangeFlowersStep()).on("*").to(deliveryFlow())
				.end()
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(LinkedinBatchApplication.class, args);
	}

	@Bean
	public StepExecutionListener selectFlowerListener() {
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


	@Bean
	public Step removeThornsStep() {
		return this.stepBuilderFactory.get("arrangeFlowersStep").tasklet(
				(stepContribution, chunkContext) -> {
					System.out.println("Remove thorns from roses.");
					return RepeatStatus.FINISHED;
				}).build();
	}

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
