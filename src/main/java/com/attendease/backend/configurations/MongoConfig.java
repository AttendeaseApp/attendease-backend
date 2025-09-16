package com.attendease.backend.configurations;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableMongoRepositories(basePackages = "com.attendease.backend.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    private final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    @Value("${spring.data.mongodb.database:attendease_db}")
    private String databaseName;

    private final Environment environment;

    public MongoConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        String uri;

        if (isProdProfile()) {
            uri = dotenv.get("MONGODB_ATLAS_URI");
            if (uri == null) {
                throw new IllegalStateException("MONGODB_ATLAS_URI is not set in .env file");
            }
        } else {
            String host = environment.getProperty("spring.data.mongodb.host");
            int port = Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.data.mongodb.port")));
            uri = String.format("mongodb://%s:%d/%s", host, port, databaseName);
        }

        log.info("Connecting to MongoDB with URI: {}", uri.replaceAll("://.*@", "://***:***@"));

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
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
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }

    private boolean isProdProfile() {
        return environment.matchesProfiles("prod");
    }
}
