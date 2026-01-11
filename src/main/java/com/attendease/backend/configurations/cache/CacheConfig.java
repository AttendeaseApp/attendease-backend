package com.attendease.backend.configurations.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager("events", "homepage-events");
		cacheManager.setCaffeine(caffeineCacheBuilder());
		return cacheManager;
	}

	private Caffeine<Object, Object> caffeineCacheBuilder() {
		return Caffeine.newBuilder()
				.maximumSize(1000)
				.expireAfterWrite(1, TimeUnit.MINUTES)
				.expireAfterAccess(5, TimeUnit.MINUTES)
				.recordStats()
				.evictionListener((key, value, cause) -> log.debug("Cache eviction - Key: {}, Cause: {}", key, cause));
	}
}