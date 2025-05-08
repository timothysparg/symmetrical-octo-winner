package com.example.weather;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class WeatherApplication {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job rainfallSyncJob;

	public static void main(String[] args) {
		SpringApplication.run(WeatherApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner() {
		return args -> {
			System.out.println("Starting rainfallSyncJob...");
			JobParameters jobParameters = new JobParametersBuilder()
					.addLong("time", System.currentTimeMillis())
					.toJobParameters();

			try {
				jobLauncher.run(rainfallSyncJob, jobParameters);
				System.out.println("rainfallSyncJob completed successfully");
			} catch (Exception e) {
				System.out.println("Error running rainfallSyncJob: " + e.getMessage());
				e.printStackTrace();
			}
		};
	}
}
