package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.WeatherRecord;

/**
 * Interface for weather service functionality
 * Provides methods to evaluate different aspects of weather conditions
 */
public interface WeatherService {
    /**
     * Determines if weather conditions are dangerous
     * @param record The weather record to assess
     * @return true if weather conditions are dangerous, false otherwise
     */
    boolean isDangerous(WeatherRecord record);

    /**
     * Determines if weather conditions are dangerous for specific purpose
     * @param record The weather record to assess
     * @param strategyType The specific purpose
     * @return true if weather conditions are dangerous, false otherwise
     */
    boolean isDangerous(WeatherRecord record, WeatherStrategyFactory.StrategyType strategyType);

    /**
     * Determines if weather is suitable for outdoor activities
     * @param record The weather record to assess
     * @return true if weather is good for outdoor activities, false otherwise
     */
    boolean isGoodForOutdoor(WeatherRecord record);

    /**
     * Gets an overall comfort rating for the weather on a scale of 1-10
     * @param record The weather record to assess
     * @return comfort rating from 1 (extremely uncomfortable) to 10 (perfect)
     */
    int getComfortRating(WeatherRecord record);

    /**
     * Gets a human-readable description of the current weather conditions
     * @param record The weather record to assess
     * @return A textual description of the weather conditions
     */
    String getWeatherDescription(WeatherRecord record);
}