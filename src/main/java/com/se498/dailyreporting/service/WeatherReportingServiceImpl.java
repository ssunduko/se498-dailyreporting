package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.WeatherRecord;

import java.time.LocalDateTime;

import com.se498.dailyreporting.repository.WeatherRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the WeatherReportingService interface
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherReportingServiceImpl implements WeatherReportingService {

    @Autowired
    private final WeatherRecordRepository weatherRecordRepository;
    @Autowired
    private final WeatherApiClient weatherApiClient;

    @Value("${weather.cache.maxAge:30}")
    private int maxCacheAgeMinutes;

    @Value("${weather.alert.enabled:true}")
    private boolean alertsEnabled;

    /**
     * {@inheritDoc}
     */
    @Override
    public WeatherRecord getCurrentWeather(Location location) {
        if (!location.isValid()) {
            throw new IllegalArgumentException("Invalid location provided");
        }

        log.debug("Requesting current weather for {}", location);

        Optional<WeatherRecord> cachedRecord = weatherRecordRepository
                .findMostRecentByZip(location.getZipCode())
                .filter(this::isFreshEnough);

        log.debug("Returning record is {}", cachedRecord);

        if (cachedRecord.isPresent()) {
            log.debug("Using cached weather data for {}", location);
            return cachedRecord.get();
        }

        // Otherwise fetch new data from external API
        log.info("Fetching fresh weather data for {}", location);
        WeatherRecord freshRecord = weatherApiClient.fetchWeatherData(location);

        // Save to repository
        return weatherRecordRepository.save(freshRecord);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WeatherRecord> getHistoricalWeather(Location location, LocalDateTime start, LocalDateTime end) {
        if (!location.isValid()) {
            throw new IllegalArgumentException("Invalid location provided");
        }

        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates must be provided");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        log.debug("Retrieving historical weather for {} from {} to {}", location, start, end);
        return weatherRecordRepository.findByLocationZipAndFetchedAtBetween(
                location.getZipCode(), start, end);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WeatherRecord> getRecentWeather(Location location, int limit) {
        if (!location.isValid()) {
            throw new IllegalArgumentException("Invalid location provided");
        }

        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }

        log.debug("Retrieving {} most recent weather records for {}", limit, location);
        return weatherRecordRepository.findRecentByZip(location.getZipCode(), limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> analyzeForAlerts(WeatherRecord weather) {
        if (!alertsEnabled) {
            return List.of();
        }

        List<String> alerts = new ArrayList<>();

        // Check for severe conditions
        if (weather.getCondition().isSevere()) {
            alerts.add("SEVERE WEATHER ALERT: " + weather.getCondition().getDescription());
        }

        // Check for extreme temperatures
        if (weather.getTemperature().getFahrenheit() > 100) {
            alerts.add("EXTREME HEAT WARNING: Temperature above 100°F");
        } else if (weather.getTemperature().getFahrenheit() > 90) {
            alerts.add("HEAT ADVISORY: Temperature above 90°F");
        } else if (weather.getTemperature().getFahrenheit() < 0) {
            alerts.add("EXTREME COLD WARNING: Temperature below 0°F");
        } else if (weather.getTemperature().getFahrenheit() < 20) {
            alerts.add("COLD ADVISORY: Temperature below 20°F");
        }

        // Check for strong winds
        if (weather.getWindSpeed().getMph() > 50) {
            alerts.add("HIGH WIND WARNING: Wind speeds above 50 mph");
        } else if (weather.getWindSpeed().getMph() > 30) {
            alerts.add("WIND ADVISORY: Wind speeds above 30 mph");
        }

        // Check for poor visibility
        if (weather.getVisibilityMiles() != null && weather.getVisibilityMiles() < 1) {
            alerts.add("LOW VISIBILITY WARNING: Visibility below 1 mile");
        }

        // Check for high UV index
        if (weather.getUvIndex() != null && weather.getUvIndex() >= 8) {
            alerts.add("UV ADVISORY: High UV index of " + weather.getUvIndex());
        }

        return alerts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double convertFahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double convertCelsiusToFahrenheit(double celsius) {
        return celsius * 9 / 5 + 32;
    }

    /**
     * Check if weather data is still fresh enough to use
     */
    private boolean isFreshEnough(WeatherRecord record) {
        return record.getAgeInMinutes() <= maxCacheAgeMinutes;
    }
}

