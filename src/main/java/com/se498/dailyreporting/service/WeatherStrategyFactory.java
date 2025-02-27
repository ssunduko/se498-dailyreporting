package com.se498.dailyreporting.service;

/**
 * Factory for creating different assessment strategies
 * Combines Factory pattern with Strategy pattern
 */
public class WeatherStrategyFactory {

    /**
     * Enum representing different types of assessment strategies
     */
    public enum StrategyType {
        STANDARD,
        OUTDOOR_ACTIVITY,
        TRAVEL_SAFETY,
        HEALTH_IMPACT
    }

    /**
     * Creates a weather assessment strategy based on the specified type
     * @param type The type of strategy to create
     * @return A WeatherAssessmentStrategy implementation for the specified type
     */
    public static WeatherAssessmentStrategy createStrategy(StrategyType type) {
        return switch (type) {
            case STANDARD -> new StandardWeatherAssessmentStrategy();
            case OUTDOOR_ACTIVITY -> new OutdoorActivityWeatherAssessmentStrategy();
            case TRAVEL_SAFETY -> new TravelSafetyWeatherAssessmentStrategy();
            case HEALTH_IMPACT -> new HealthImpactWeatherAssessmentStrategy();
            default -> throw new IllegalArgumentException("Unknown strategy type: " + type);
        };
    }
}