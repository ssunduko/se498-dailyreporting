package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.Temperature;
import com.se498.dailyreporting.domain.bo.WeatherCondition;
import com.se498.dailyreporting.domain.bo.WeatherRecord;
import com.se498.dailyreporting.domain.bo.WindSpeed;

/**
 * Travel safety focused assessment strategy
 * Emphasizes factors that affect travel safety
 */
public class TravelSafetyWeatherAssessmentStrategy implements WeatherAssessmentStrategy {
    @Override
    public boolean isDangerous(WeatherRecord record) {
        // For travel, we're concerned with road/visibility conditions
        if (record.hasSevereConditions()) {
            return true;
        }

        // Check for freezing temperatures (icy roads)
        Temperature temp = record.getTemperature();
        if (temp.getFahrenheit() <= 32) {
            return true;
        }

        // Check for poor visibility
        double visibility = record.getVisibilityMiles();
        if (visibility < 1.0) {
            return true;
        }

        // Check for high winds (dangerous for high-profile vehicles)
        WindSpeed wind = record.getWindSpeed();
        if (wind.getMph() > 30.0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isGoodForOutdoor(WeatherRecord record) {
        // Travel-focused "good outdoor" means good road conditions
        return !isDangerous(record);
    }

    @Override
    public int getComfortRating(WeatherRecord record) {
        // For travel safety, "comfort" really means "safety"
        int rating = 10; // Start with perfect safety

        // Deduct for dangerous temperature (icy roads)
        Temperature temp = record.getTemperature();
        if (temp.getFahrenheit() <= 32) {
            rating -= 5; // Freezing temperatures
        } else if (temp.getFahrenheit() <= 36) {
            rating -= 2; // Near freezing
        }

        // Deduct for poor visibility
        double visibility = record.getVisibilityMiles();
        if (visibility < 0.25) {
            rating -= 7; // Extremely low visibility
        } else if (visibility < 0.5) {
            rating -= 5; // Very low visibility
        } else if (visibility < 1.0) {
            rating -= 3; // Low visibility
        } else if (visibility < 3.0) {
            rating -= 1; // Reduced visibility
        }

        // Deduct for high winds
        WindSpeed wind = record.getWindSpeed();
        double windSpeed = wind.getMph();
        if (windSpeed > 40.0) {
            rating -= 4; // Dangerous winds for all vehicles
        } else if (windSpeed > 30.0) {
            rating -= 3; // Dangerous for high-profile vehicles
        } else if (windSpeed > 20.0) {
            rating -= 1; // Noticeable wind effect
        }

        // Deduct for severe conditions
        WeatherCondition condition = record.getCondition();
        if (condition.isSevere()) {
            rating -= 5; // Severe weather
        }

        // Ensure rating is within bounds
        if (rating > 10) rating = 10;
        if (rating < 1) rating = 1;

        return rating;
    }

    @Override
    public String getWeatherDescription(WeatherRecord record) {
        StringBuilder description = new StringBuilder();
        int rating = getComfortRating(record);

        if (rating >= 8) {
            description.append("Safe travel conditions. ");
        } else if (rating >= 6) {
            description.append("Generally safe travel conditions with some caution advised. ");
        } else if (rating >= 4) {
            description.append("Travel with extra caution. ");
        } else {
            description.append("Dangerous travel conditions. Avoid travel if possible. ");
        }

        // Add specific concerns
        Temperature temp = record.getTemperature();
        if (temp.getFahrenheit() <= 32) {
            description.append("Risk of icy roads. ");
        }

        double visibility = record.getVisibilityMiles();
        if (visibility < 3.0) {
            description.append(String.format("Reduced visibility at %.1f miles. ", visibility));
        }

        WindSpeed wind = record.getWindSpeed();
        if (wind.getMph() > 20.0) {
            description.append(String.format("Strong winds at %.1f mph. ", wind.getMph()));
        }

        return description.toString();
    }

    @Override
    public String getStrategyName() {
        return "Travel Safety Assessment";
    }
}