package com.example.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WeatherApplication {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(WeatherApplication.class, args)));
	}
}
