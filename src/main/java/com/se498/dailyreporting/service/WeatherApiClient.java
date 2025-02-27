package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.WeatherRecord;
import com.se498.dailyreporting.exception.WeatherApiException;

/**
 * Interface for weather API clients that fetch external weather data
 */
public interface WeatherApiClient {

    /**
     * Fetches weather data from an external API
     *
     * @param location the location to fetch weather for
     * @return a WeatherRecord populated with the fetched data
     * @throws WeatherApiException if there's an error communicating with the API
     */
    WeatherRecord fetchWeatherData(Location location);

    /**
     * Checks if the API service is currently available
     *
     * @return true if the service is available, false otherwise
     */
    boolean isServiceAvailable();

    /**
     * Gets the name of this weather data provider
     *
     * @return the provider name
     */
    String getProviderName();
}