package com.se498.dailyreporting.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import lombok.Data;

/**
 * Configuration for Spring Cache without Redis support
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    public static final String WEATHER_CACHE = "weatherCache";

    @Value("${weather.cache.ttl-minutes:30}")
    private int cacheTtlMinutes;

    @Value("${weather.cache.maximum-size:10000}")
    private int cacheMaximumSize;

    /**
     * Primary cache manager using Caffeine for high-performance caching
     */
    @Bean
    @Primary
    public CacheManager cacheManager(
            CaffeineCacheManager caffeineCacheManager,
            ConcurrentMapCacheManager fallbackCacheManager) {

        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();
        compositeCacheManager.setCacheManagers(Arrays.asList(
                caffeineCacheManager,
                fallbackCacheManager));
        compositeCacheManager.setFallbackToNoOpCache(true);
        return compositeCacheManager;
    }

    /**
     * Caffeine cache manager configuration
     */
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();

        // Cache settings
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(cacheTtlMinutes, TimeUnit.MINUTES)
                .maximumSize(cacheMaximumSize)
                .recordStats();

        caffeineCacheManager.setCaffeine(caffeine);
        caffeineCacheManager.setCacheNames(List.of(WEATHER_CACHE));

        return caffeineCacheManager;
    }

    /**
     * Fallback in-memory cache manager using ConcurrentMap
     */
    @Bean
    public ConcurrentMapCacheManager fallbackCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager() {
            @NotNull
            @Override
            protected ConcurrentMapCache createConcurrentMapCache(String name) {
                return new ConcurrentMapCache(name);
            }
        };

        cacheManager.setCacheNames(List.of(WEATHER_CACHE));

        return cacheManager;
    }

    /**
     * Cache properties configuration class
     */
    @Data
    @Configuration
    @ConfigurationProperties(prefix = "weather.cache")
    public static class CacheProperties {
        private int ttlMinutes = 30;
        private int maximumSize = 10000;
    }
}