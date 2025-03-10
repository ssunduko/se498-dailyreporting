package com.se498.dailyreporting.service.converter;

/**
 * Command implementation for temperature conversion
 */
public class TemperatureConversionCommand implements ConversionCommand {
    private final double value;
    private final ConversionStrategy strategy;

    /**
     * Create a new temperature conversion command
     * @param value Value to convert
     * @param strategy Strategy to use for conversion
     */
    public TemperatureConversionCommand(double value, ConversionStrategy strategy) {
        this.value = value;
        this.strategy = strategy;
    }

    @Override
    public Double execute() {
        return strategy.convert(value);
    }
}