package com.attendease.backend.schedulers.cache.stats;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheStatsMonitor {

	private final CacheManager cacheManager;

	@Scheduled(fixedRate = 300000)
	public void logCacheStats() {
		cacheManager.getCacheNames().forEach(cacheName -> {
			org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
			if (cache instanceof CaffeineCache caffeineCache) {
				Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
				CacheStats stats = nativeCache.stats();

				log.info("Cache '{}' stats - Hits: {}, Misses: {}, Hit Rate: {}, Evictions: {}, Size: {}",
						cacheName,
						stats.hitCount(),
						stats.missCount(),
						stats.hitRate() * 100,
						stats.evictionCount(),
						nativeCache.estimatedSize()
				);
			}
		});
	}
}
