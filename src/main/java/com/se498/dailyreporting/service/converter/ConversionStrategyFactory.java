package com.se498.dailyreporting.service.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating and managing conversion strategies
 */
public class ConversionStrategyFactory {
    private static final Map<String, ConversionStrategy> strategies = new HashMap<>();

    // Initialize available strategies
    static {
        strategies.put("celsius to fahrenheit", new CelsiusToFahrenheitStrategy());
        strategies.put("celsius to kelvin", new CelsiusToKelvinStrategy());
    }

    /**
     * Get a strategy by name
     * @param strategyType Name of the strategy
     * @return The strategy, or null if not found
     */
    public static ConversionStrategy getStrategy(String strategyType) {
        return strategies.get(strategyType.toLowerCase());
    }

    /**
     * Get all available strategies
     * @return Map of strategy names to strategies
     */
    public static Map<String, ConversionStrategy> getAllStrategies() {
        return new HashMap<>(strategies);
    }
}