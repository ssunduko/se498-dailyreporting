package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.WeatherRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for the core weather reporting service
 */
public interface WeatherReportingService {

    /**
     * Gets the current weather for a location, using cached data if available and fresh
     *
     * @param location the location to get weather for
     * @return the current weather record
     * @throws IllegalArgumentException if location is invalid
     */
    WeatherRecord getCurrentWeather(Location location);

    /**
     * Fetches historical weather data for a location
     *
     * @param location the location to get weather for
     * @param start the start date/time
     * @param end the end date/time
     * @return list of weather records within the specified time range
     * @throws IllegalArgumentException if parameters are invalid
     */
    List<WeatherRecord> getHistoricalWeather(Location location, LocalDateTime start, LocalDateTime end);

    /**
     * Gets most recent weather records for a location
     *
     * @param location the location to get weather for
     * @param limit maximum number of records to retrieve
     * @return list of recent weather records
     * @throws IllegalArgumentException if parameters are invalid
     */
    List<WeatherRecord> getRecentWeather(Location location, int limit);

    /**
     * Analyzes weather data for potential alerts
     *
     * @param weather the weather record to analyze
     * @return list of alert messages, empty if no alerts
     */
    List<String> analyzeForAlerts(WeatherRecord weather);

    /**
     * Convert temperature from Fahrenheit to Celsius
     *
     * @param fahrenheit temperature in Fahrenheit
     * @return temperature in Celsius
     */
    double convertFahrenheitToCelsius(double fahrenheit);

    /**
     * Convert temperature from Celsius to Fahrenheit
     *
     * @param celsius temperature in Celsius
     * @return temperature in Fahrenheit
     */
    double convertCelsiusToFahrenheit(double celsius);
}