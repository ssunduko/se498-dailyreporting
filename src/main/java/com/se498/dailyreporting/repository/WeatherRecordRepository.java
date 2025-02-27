package com.se498.dailyreporting.repository;

import com.se498.dailyreporting.domain.bo.WeatherRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WeatherRecord entities
 */
public interface WeatherRecordRepository {

    /**
     * Save a weather record
     *
     * @param weatherRecord the record to save
     * @return the saved record
     */
    WeatherRecord save(WeatherRecord weatherRecord);

    /**
     * Find the most recent weather record for a city
     *
     * @param zip the zip name
     * @return optional containing the most recent record, or empty if none exists
     */
    Optional<WeatherRecord> findMostRecentByZip(String zip);

    /**
     * Find weather records for a city within a date range
     *
     * @param zip the city name
     * @param start the start date/time
     * @param end the end date/time
     * @return list of matching weather records
     */
    List<WeatherRecord> findByLocationZipAndFetchedAtBetween(String zip, LocalDateTime start, LocalDateTime end);

    /**
     * Find the most recent n weather records for a city
     *
     * @param zip the city name
     * @param limit maximum number of records to return
     * @return list of recent weather records
     */
    List<WeatherRecord> findRecentByZip(String zip, int limit);

    /**
     * Clear all cached weather data
     */
    void clearCache();

    /**
     * Clear cached weather data for a specific zip
     *
     * @param zip the zip number
     */
    void clearCacheForZip(String zip);
}