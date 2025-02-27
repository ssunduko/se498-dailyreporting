package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.*;

/**
 * Outdoor activity focused assessment strategy
 * Emphasizes factors that affect outdoor activities
 */
public class OutdoorActivityWeatherAssessmentStrategy implements WeatherAssessmentStrategy {
    @Override
    public boolean isDangerous(WeatherRecord record) {
        // For outdoor activities, focus on immediate dangers
        return record.hasSevereConditions();
    }

    @Override
    public boolean isGoodForOutdoor(WeatherRecord record) {
        // More detailed assessment than standard
        if (record.hasSevereConditions()) {
            return false;
        }

        Temperature temp = record.getTemperature();
        if (!temp.isModerate()) {
            return false;
        }

        Humidity humidity = record.getHumidity();
        if (humidity.isHigh()) {
            return false;
        }

        WindSpeed wind = record.getWindSpeed();
        if (wind.isStrong()) {
            return false;
        }

        WeatherCondition condition = record.getCondition();
        return condition.isGoodForOutdoorActivities();
    }

    @Override
    public int getComfortRating(WeatherRecord record) {
        // Start with a neutral rating
        int rating = 5;

        // Check temperature - outdoor activities prefer cooler temperatures
        Temperature temp = record.getTemperature();
        double fahrenheit = temp.getFahrenheit();
        if (fahrenheit >= 60 && fahrenheit <= 75) {
            rating += 3; // Ideal for outdoor activities
        } else if (fahrenheit > 75 && fahrenheit <= 85) {
            rating += 1; // Good but getting warm
        } else if (fahrenheit > 85) {
            rating -= 2; // Too hot for comfortable activities
        } else if (fahrenheit < 60 && fahrenheit >= 45) {
            rating += 1; // Good but getting cool
        } else if (fahrenheit < 45) {
            rating -= 2; // Too cold for most activities
        }

        // Check humidity - very important for outdoor comfort
        Humidity humidity = record.getHumidity();
        int humidityPercent = humidity.getPercentage();
        if (humidityPercent >= 30 && humidityPercent <= 50) {
            rating += 2; // Ideal humidity
        } else if (humidityPercent > 50 && humidityPercent <= 70) {
            rating += 0; // Acceptable humidity
        } else if (humidityPercent > 70) {
            rating -= 3; // Too humid for comfortable activity
        } else if (humidityPercent < 30) {
            rating -= 1; // Too dry
        }

        // Check wind - light breeze can be nice, strong winds are not
        WindSpeed wind = record.getWindSpeed();
        double windSpeed = wind.getMph();
        if (windSpeed >= 3 && windSpeed <= 10) {
            rating += 1; // Pleasant breeze
        } else if (windSpeed > 10 && windSpeed <= 15) {
            rating += 0; // Noticeable but not problematic
        } else if (windSpeed > 15) {
            rating -= 2; // Too windy for most activities
        }

        // Check weather condition
        WeatherCondition condition = record.getCondition();
        if (condition.isGoodForOutdoorActivities()) {
            rating += 2; // Good weather condition
        } else {
            rating -= 3; // Bad for outdoor activities
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
            description.append("Excellent conditions for outdoor activities. ");
        } else if (rating >= 6) {
            description.append("Good conditions for outdoor activities. ");
        } else if (rating >= 4) {
            description.append("Acceptable conditions for outdoor activities. ");
        } else {
            description.append("Poor conditions for outdoor activities. ");
        }

        description.append(String.format("Weather is %s with %.1f°F, ",
                record.getTemperature().isModerate() ? "moderate" :
                        (record.getTemperature().isHot() ? "hot" : "cold"),
                record.getTemperature().getFahrenheit()));

        description.append(String.format("humidity at %d%%, ",
                record.getHumidity().getPercentage()));

        description.append(String.format("and wind at %.1f mph.",
                record.getWindSpeed().getMph()));

        return description.toString();
    }

    @Override
    public String getStrategyName() {
        return "Outdoor Activity Assessment";
    }
}