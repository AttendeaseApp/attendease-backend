package com.attendease.backend.configurations;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableMongoRepositories(basePackages = "com.attendease.backend.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.database:attendease_db}")
    private String databaseName;

    @Value("${spring.data.mongodb.host:localhost}")
    private String mongoHost;

    @Value("${spring.data.mongodb.port:27017}")
    private int mongoPort;

    @Value("${spring.data.mongodb.username:#{null}}")
    private String username;

    @Value("${spring.data.mongodb.password:#{null}}")
    private String password;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        String connectionString;

        if (username != null && password != null) {
            connectionString = String.format("mongodb://%s:%s@%s:%d/%s",
                    username, password, mongoHost, mongoPort, databaseName);
        } else {
            connectionString = String.format("mongodb://%s:%d/%s",
                    mongoHost, mongoPort, databaseName);
        }

        log.info("Connecting to MongoDB at: {}", connectionString.replaceAll("://.*@", "://***:***@"));

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .applyToConnectionPoolSettings(builder ->
                        builder.maxSize(10)
                                .minSize(5)
                                .maxWaitTime(120, TimeUnit.SECONDS)
                                .maxConnectionIdleTime(600, TimeUnit.SECONDS)
                )
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(30, TimeUnit.SECONDS)
                )
                .applyToServerSettings(builder ->
                        builder.heartbeatFrequency(30, TimeUnit.SECONDS)
                )
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}
