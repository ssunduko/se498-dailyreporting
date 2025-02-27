package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.Humidity;
import com.se498.dailyreporting.domain.bo.Temperature;
import com.se498.dailyreporting.domain.bo.WeatherRecord;

/**
 * Health impact focused assessment strategy
 * Emphasizes factors that affect human health
 */
public class HealthImpactWeatherAssessmentStrategy implements WeatherAssessmentStrategy {
    @Override
    public boolean isDangerous(WeatherRecord record) {
        // For health, extreme temperatures and heat index are primary concerns
        Temperature temp = record.getTemperature();
        double heatIndex = record.getHeatIndex();
        double windChill = record.getWindChill();

        // Check for extreme heat stress
        if (heatIndex > 103.0) {
            return true;
        }

        // Check for extreme cold stress
        if (windChill < 0.0) {
            return true;
        }

        // Check for UV danger
        int uvIndex = record.getUvIndex();
        if (uvIndex >= 8) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isGoodForOutdoor(WeatherRecord record) {
        // For health perspective
        if (isDangerous(record)) {
            return false;
        }

        Temperature temp = record.getTemperature();
        if (!temp.isModerate()) {
            return false;
        }

        Humidity humidity = record.getHumidity();
        if (humidity.isHigh() || humidity.isLow()) {
            return false;
        }

        // Check UV level - moderate UV is acceptable
        int uvIndex = record.getUvIndex();
        if (uvIndex > 5) {
            return false;
        }

        return true;
    }

    @Override
    public int getComfortRating(WeatherRecord record) {
        // For health, "comfort" means minimal health impact
        int rating = 10; // Start with no health concerns

        // Check heat index (combines temperature and humidity)
        double heatIndex = record.getHeatIndex();
        if (heatIndex > 103.0) {
            rating -= 5; // Dangerous heat
        } else if (heatIndex > 90.0) {
            rating -= 3; // Hot and uncomfortable
        } else if (heatIndex > 80.0) {
            rating -= 1; // Warm
        }

        // Check wind chill
        double windChill = record.getWindChill();
        if (windChill < 0.0) {
            rating -= 5; // Dangerously cold
        } else if (windChill < 32.0) {
            rating -= 3; // Cold stress possible
        } else if (windChill < 45.0) {
            rating -= 1; // Cool
        }

        // Check UV index
        int uvIndex = record.getUvIndex();
        if (uvIndex >= 11) {
            rating -= 5; // Extreme UV
        } else if (uvIndex >= 8) {
            rating -= 3; // Very high UV
        } else if (uvIndex >= 6) {
            rating -= 2; // High UV
        } else if (uvIndex >= 3) {
            rating -= 1; // Moderate UV
        }

        // Check air pressure (some people are sensitive)
        double pressureInHg = record.getPressureInHg();
        boolean pressureChange = (pressureInHg < 29.8 || pressureInHg > 30.2);
        if (pressureChange) {
            rating -= 1; // Pressure outside normal range
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
            description.append("Minimal health concerns. ");
        } else if (rating >= 6) {
            description.append("Some health concerns for sensitive individuals. ");
        } else if (rating >= 4) {
            description.append("Moderate health concerns. Take precautions. ");
        } else {
            description.append("Significant health risks. Avoid extended outdoor exposure. ");
        }

        // Add specific health concerns
        double heatIndex = record.getHeatIndex();
        if (heatIndex > 90.0) {
            description.append(String.format("Heat stress risk with heat index of %.1f°F. ", heatIndex));
        }

        double windChill = record.getWindChill();
        if (windChill < 32.0) {
            description.append(String.format("Cold stress risk with wind chill of %.1f°F. ", windChill));
        }

        int uvIndex = record.getUvIndex();
        if (uvIndex >= 3) {
            description.append(String.format("UV index of %d - sun protection advised. ", uvIndex));
        }

        return description.toString();
    }

    @Override
    public String getStrategyName() {
        return "Health Impact Assessment";
    }
}