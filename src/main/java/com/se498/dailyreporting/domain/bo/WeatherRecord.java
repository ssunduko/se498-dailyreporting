package com.se498.dailyreporting.domain.bo;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Weather record aggregate root
 */
@Getter
@Builder
public class WeatherRecord {
    private final String id;
    private final Location location;
    private final Temperature temperature;
    private final Humidity humidity;
    private final WindSpeed windSpeed;
    private final WeatherCondition condition;
    private final Double pressureInHg;
    private final Double visibilityMiles;
    private final Integer uvIndex;
    private final LocalDateTime recordedAt;
    private final LocalDateTime fetchedAt;
    private final String dataSource;

    /**
     * Business rule: Determines if weather data is considered fresh (fetched within last 30 minutes)
     */
    public boolean isFresh() {
        return fetchedAt.isAfter(LocalDateTime.now().minusMinutes(30));
    }

    /**
     * Business rule: Get the age of this weather record in minutes
     */
    public long getAgeInMinutes() {
        return ChronoUnit.MINUTES.between(fetchedAt, LocalDateTime.now());
    }

    /**
     * Business rule: Determines if weather indicates severe conditions
     */
    public boolean hasSevereConditions() {
        return condition.isSevere() ||
                temperature.getFahrenheit() > 100 ||
                temperature.getFahrenheit() < 0 ||
                windSpeed.getMph() > 30;
    }

    /**
     * Business rule: Determines heat index (feels-like temperature)
     * Using simplified Steadman's formula
     */
    public double getHeatIndex() {
        double t = temperature.getFahrenheit();
        double h = humidity.getPercentage();

        // Only valid for temperatures above 80°F
        if (t < 80) return t;

        return -42.379 +
                2.04901523 * t +
                10.14333127 * h -
                0.22475541 * t * h -
                0.00683783 * t * t -
                0.05481717 * h * h +
                0.00122874 * t * t * h +
                0.00085282 * t * h * h -
                0.00000199 * t * t * h * h;
    }

    /**
     * Business rule: Determines wind chill (feels-like temperature in cold, windy conditions)
     * Using the North American and UK formula
     */
    public double getWindChill() {
        double t = temperature.getFahrenheit();
        double v = windSpeed.getMph();

        // Only valid for temperatures at or below 50°F and wind speeds above 3 mph
        if (t > 50 || v <= 3) return t;

        return 35.74 + 0.6215 * t - 35.75 * Math.pow(v, 0.16) + 0.4275 * t * Math.pow(v, 0.16);
    }

    /**
     * Business rule: Get the "feels like" temperature considering both heat index and wind chill
     */
    public double getFeelsLikeTemperature() {
        double t = temperature.getFahrenheit();
        if (t > 80) return getHeatIndex();
        if (t <= 50 && windSpeed.getMph() > 3) return getWindChill();
        return t;
    }

    /**
     * Business rule: Determine if conditions are favorable for outdoor activities
     */
    public boolean isFavorableForOutdoorActivities() {
        return !hasSevereConditions() &&
                condition.isGoodForOutdoorActivities() &&
                temperature.isModerate() &&
                !humidity.isHigh() &&
                !windSpeed.isStrong();
    }
}