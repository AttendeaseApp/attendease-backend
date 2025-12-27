package com.attendease.backend;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Main application class for Attendease Backend.
 */
@EnableScheduling
@EnableMethodSecurity()
@SpringBootApplication
@EnableMongoAuditing
@Slf4j
public class AttendeaseBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendeaseBackendApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Manila"));
        log.info("Application timezone set to: {}", TimeZone.getDefault().getID());
        log.info("Current time: {}", LocalDateTime.now());
        log.info("System default timezone (before override): {}", System.getProperty("user.timezone"));
    }
}
