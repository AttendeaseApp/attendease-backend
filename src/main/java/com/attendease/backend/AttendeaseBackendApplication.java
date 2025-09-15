package com.attendease.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.TimeZone;

/**
 * Main application class for Attendease Backend.
 */
@EnableScheduling
@EnableMethodSecurity(prePostEnabled = true)
@SpringBootApplication
public class AttendeaseBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttendeaseBackendApplication.class, args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Manila"));
	}
}
