package com.attendease.backend;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Main application class for Attendease Backend.
 */
@EnableScheduling
@EnableMethodSecurity(prePostEnabled = true)
@SpringBootApplication
@EnableMongoAuditing
public class AttendeaseBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendeaseBackendApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Manila"));
    }
}
