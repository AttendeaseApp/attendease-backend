package com.attendease.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AttendeaseBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttendeaseBackendApplication.class, args);
	}

}
