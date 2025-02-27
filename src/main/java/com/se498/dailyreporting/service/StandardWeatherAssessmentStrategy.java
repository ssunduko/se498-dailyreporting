package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.*;

/**
 * Standard assessment strategy
 * Provides a balanced approach to weather assessment
 */
public class StandardWeatherAssessmentStrategy implements WeatherAssessmentStrategy {
    @Override
    public boolean isDangerous(WeatherRecord record) {
        // Check for severe weather conditions
        if (record.hasSevereConditions()) {
            return true;
        }

        // Check for extreme heat index (feels like temperature)
        double heatIndex = record.getHeatIndex();
        if (heatIndex > 105.0) {
            return true;
        }

        // Check for extreme wind chill
        double windChill = record.getWindChill();
        if (windChill < -10.0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isGoodForOutdoor(WeatherRecord record) {
        // Use the built-in method from WeatherRecord
        return record.isFavorableForOutdoorActivities();
    }

    @Override
    public int getComfortRating(WeatherRecord record) {
        // Start with a neutral rating
        int rating = 5;

        // Check temperature comfort
        Temperature temp = record.getTemperature();
        if (temp.isModerate()) {
            // Ideal temperature range (not too hot, not too cold)
            double fahrenheit = temp.getFahrenheit();
            if (fahrenheit >= 65 && fahrenheit <= 80) {
                rating += 2; // Perfect temperature
            } else {
                rating += 1; // Good temperature
            }
        } else if (temp.isHot() || temp.isCold()) {
            rating -= 2; // Too hot or too cold
        }

        // Check humidity comfort
        Humidity humidity = record.getHumidity();
        if (humidity.isComfortable()) {
            rating += 1; // Comfortable humidity
        } else if (humidity.isHigh()) {
            rating -= 1; // Too humid
        } else if (humidity.isLow()) {
            rating -= 1; // Too dry
        }

        // Check wind conditions
        WindSpeed wind = record.getWindSpeed();
        if (wind.isStrong()) {
            rating -= 1; // Too windy
        }

        // Check weather conditions
        WeatherCondition condition = record.getCondition();
        if (condition.isGoodForOutdoorActivities()) {
            rating += 1; // Good weather condition
        }
        if (condition.isSevere()) {
            rating -= 3; // Severe weather condition
        }

        // Ensure rating is within bounds
        if (rating > 10) rating = 10;
        if (rating < 1) rating = 1;

        return rating;
    }

    @Override
    public String getWeatherDescription(WeatherRecord record) {
        StringBuilder description = new StringBuilder();

        // Add temperature description
        Temperature temp = record.getTemperature();
        double feelsLike = record.getFeelsLikeTemperature();

        if (temp.isHot()) {
            description.append("Hot");
        } else if (temp.isCold()) {
            description.append("Cold");
        } else {
            description.append("Moderate temperature");
        }

        // Add feels like information if different from actual
        if (Math.abs(feelsLike - temp.getFahrenheit()) > 5.0) {
            description.append(String.format(" (feels like %.1f°F)", feelsLike));
        }

        // Add humidity description
        Humidity humidity = record.getHumidity();
        if (humidity.isHigh()) {
            description.append(", humid");
        } else if (humidity.isLow()) {
            description.append(", dry");
        }

        // Add wind description
        WindSpeed wind = record.getWindSpeed();
        if (wind.isStrong()) {
            description.append(", windy");
        }

        // Add comfort rating
        int comfort = getComfortRating(record);
        description.append(String.format(". Comfort rating: %d/10", comfort));

        return description.toString();
    }

    @Override
    public String getStrategyName() {
        return "Standard Assessment";
    }
}
