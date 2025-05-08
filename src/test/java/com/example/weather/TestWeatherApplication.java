package com.example.weather;

import org.springframework.boot.SpringApplication;

public class TestWeatherApplication {

	public static void main(String[] args) {
		SpringApplication.from(WeatherApplication::main).run(args);
	}

}
