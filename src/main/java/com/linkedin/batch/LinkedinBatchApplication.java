package com.linkedin.batch;

import com.linkedin.batch.decider.DeliveryDecider;
import com.linkedin.batch.decider.itemCorrectDecider;
import com.linkedin.batch.listener.FlowersSelectionStepExecutionListener;
import com.linkedin.batch.steps.DeliverySteps;
import com.linkedin.batch.steps.FlowerSteps;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
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
	public DeliverySteps deliverySteps() {
		return new DeliverySteps();
	}

	@Bean
	public FlowerSteps flowerSteps() {
		return new FlowerSteps();
	}

	@Bean
	public Job deliverPackageJob() {
		return this.jobBuilderFactory.get("deliverPackageJob")
				.start(deliverySteps().packageItemStep())
				.next(deliverySteps().driveToAddressStep())
					.on("FAILED")
						// Ao parar ou falhar a execucao, eh possivel retomar a mesma Job Instance
						// apos a correcao da falha
						//.stop()
						.fail()
				.from(deliverySteps().driveToAddressStep())
					.on("*").to(decider())
						.on("PRESENT").to(deliverySteps().givePackageToCustomerStep())
							.next(correctDecider()).on("CORRECT").to(deliverySteps().thankCustomerStep())
							.from(correctDecider()).on("INCORRECT").to(deliverySteps().giveRefundStep())
					.from(decider())
						.on("NOT_PRESENT").to(deliverySteps().leaveAtDoorStep())
				.end()
				.build();
	}

	@Bean
	public Job prepareFlowers() {
		return this.jobBuilderFactory.get("prepareFlowersJob")
				.start(flowerSteps().selectFlowersStep())
					.on("TRIM_REQUIRED").to(flowerSteps().removeThornsStep())
				.from(flowerSteps().selectFlowersStep())
					.on("NO_TRIM_REQUIRED").to(flowerSteps().arrangeFlowersStep())
				.end()
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(LinkedinBatchApplication.class, args);
	}

}
