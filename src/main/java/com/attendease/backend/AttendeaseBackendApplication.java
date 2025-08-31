package com.attendease.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Attendease Backend.
 */
@EnableScheduling
@SpringBootApplication
public class AttendeaseBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttendeaseBackendApplication.class, args);
	}

}
