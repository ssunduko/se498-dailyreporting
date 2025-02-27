package com.se498.dailyreporting.domain.bo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Weather condition value object
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherCondition {
    private static final Set<String> SEVERE_CONDITIONS = new HashSet<>(Arrays.asList(
            "thunderstorm", "hurricane", "tornado", "blizzard", "hail", "freezing rain",
            "ice storm", "dust storm", "tropical storm", "typhoon", "cyclone", "sandstorm"
    ));

    private static final Set<String> CLEAR_CONDITIONS = new HashSet<>(Arrays.asList(
            "clear", "sunny", "fair", "cloudless", "bright"
    ));

    private static final Set<String> CLOUDY_CONDITIONS = new HashSet<>(Arrays.asList(
            "cloudy", "overcast", "partly cloudy", "mostly cloudy", "broken clouds"
    ));

    private static final Set<String> RAINY_CONDITIONS = new HashSet<>(Arrays.asList(
            "rain", "drizzle", "shower", "downpour", "rainy", "precipitation"
    ));

    private static final Set<String> SNOWY_CONDITIONS = new HashSet<>(Arrays.asList(
            "snow", "flurry", "snowfall", "snowy", "blizzard", "sleet"
    ));

    private String description;
    private String iconCode;

    public WeatherCondition(String description) {
        this(description, null);
    }

    /**
     * Determine if the weather condition is considered severe
     */
    public boolean isSevere() {
        if (description == null) return false;

        String lowerCaseDesc = description.toLowerCase();
        return SEVERE_CONDITIONS.stream()
                .anyMatch(lowerCaseDesc::contains);
    }

    /**
     * Determine if the weather condition indicates clear skies
     */
    public boolean isClear() {
        if (description == null) return false;

        String lowerCaseDesc = description.toLowerCase();
        return CLEAR_CONDITIONS.stream()
                .anyMatch(lowerCaseDesc::contains);
    }

    /**
     * Determine if the weather condition indicates cloudy skies
     */
    public boolean isCloudy() {
        if (description == null) return false;

        String lowerCaseDesc = description.toLowerCase();
        return CLOUDY_CONDITIONS.stream()
                .anyMatch(lowerCaseDesc::contains);
    }

    /**
     * Determine if the weather condition indicates precipitation
     */
    public boolean isRainy() {
        if (description == null) return false;

        String lowerCaseDesc = description.toLowerCase();
        return RAINY_CONDITIONS.stream()
                .anyMatch(lowerCaseDesc::contains);
    }

    /**
     * Determine if the weather condition indicates snow
     */
    public boolean isSnowy() {
        if (description == null) return false;

        String lowerCaseDesc = description.toLowerCase();
        return SNOWY_CONDITIONS.stream()
                .anyMatch(lowerCaseDesc::contains);
    }

    /**
     * Get the general category of the weather condition
     */
    public ConditionCategory getCategory() {
        if (isSnowy()) return ConditionCategory.SNOW;
        if (isRainy()) return ConditionCategory.RAIN;
        if (isCloudy()) return ConditionCategory.CLOUDY;
        if (isClear()) return ConditionCategory.CLEAR;
        return ConditionCategory.OTHER;
    }

    /**
     * Determine if outdoor activities are recommended based on weather condition
     */
    public boolean isGoodForOutdoorActivities() {
        return isClear() ||
                (isCloudy() && !isRainy() && !isSnowy() && !isSevere());
    }

    @Override
    public String toString() {
        return description;
    }

    /**
     * General weather condition categories
     */
    public enum ConditionCategory {
        CLEAR,
        CLOUDY,
        RAIN,
        SNOW,
        OTHER
    }
}