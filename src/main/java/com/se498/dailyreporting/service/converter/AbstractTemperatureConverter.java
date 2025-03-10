package com.se498.dailyreporting.service.converter;

import java.util.Map;

/**
 * Abstract class defining core functionality for temperature converters
 * with Celsius to Fahrenheit as the default conversion strategy
 */
public abstract class AbstractTemperatureConverter {

    /**
     * Get the default conversion strategy (Celsius to Fahrenheit)
     * @return Default conversion strategy
     */
    protected ConversionStrategy getDefaultStrategy() {
        return new CelsiusToFahrenheitStrategy();
    }

    /**
     * Convert temperature using the default strategy (Celsius to Fahrenheit)
     * @param value Temperature value to convert
     * @return Converted temperature value
     */
    public Double convert(double value) {
        return convert(value, getDefaultStrategy());
    }

    /**
     * Convert temperature using a specific strategy
     * @param value Temperature value to convert
     * @param strategy Conversion strategy to use
     * @return Converted temperature value
     */
    public Double convert(double value, ConversionStrategy strategy) {
        ConversionCommand command = createConversionCommand(value, strategy);
        return command.execute();
    }

    /**
     * Convert temperature using a strategy name
     * @param value Temperature value to convert
     * @param strategyName Name of the conversion strategy
     * @return Converted temperature value
     */
    public Double convert(double value, String strategyName) {
        ConversionStrategy strategy = getStrategyByName(strategyName);
        if (strategy == null) {
            System.out.println("Invalid conversion strategy: " + strategyName + ". Using default strategy.");
            strategy = getDefaultStrategy();
        }
        return convert(value, strategy);
    }

    /**
     * Create a command for the conversion operation
     * @param value Value to convert
     * @param strategy Strategy to use
     * @return Command object
     */
    protected ConversionCommand createConversionCommand(double value, ConversionStrategy strategy) {
        return new TemperatureConversionCommand(value, strategy);
    }

    /**
     * Get a conversion strategy by name
     * @param strategyName Name of the strategy
     * @return The strategy, or null if not found
     */
    protected abstract ConversionStrategy getStrategyByName(String strategyName);

    /**
     * Get all available conversion strategies
     * @return Map of strategy names to strategies
     */
    public abstract Map<String, ConversionStrategy> getAvailableStrategies();
}