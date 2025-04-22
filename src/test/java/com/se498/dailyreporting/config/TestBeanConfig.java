package com.se498.dailyreporting.config;

import com.se498.dailyreporting.repository.FakeWeatherRecordRepository;
import com.se498.dailyreporting.repository.WeatherRecordRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class that provides beans for testing
 */
@Configuration
@Profile("test")
public class TestBeanConfig {

    /**
     * Provides a fake implementation of WeatherRecordRepository for testing
     * This bean is marked as @Primary so it will be favored over any other
     * WeatherRecordRepository beans in the test context
     */
    @Bean
    @Primary
    public WeatherRecordRepository weatherRecordRepository() {
        return new FakeWeatherRecordRepository();
    }

    /**
     * Provides the fake repository specifically as FakeWeatherRecordRepository type
     * for tests that need access to the fake implementation's specific methods
     */
    @Bean
    public FakeWeatherRecordRepository fakeWeatherRecordRepository() {
        return new FakeWeatherRecordRepository();
    }
}