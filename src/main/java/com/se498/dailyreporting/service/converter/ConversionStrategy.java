package com.se498.dailyreporting.service.converter;

/**
 * Strategy interface for temperature conversion algorithms
 */
public interface ConversionStrategy {
    /**
     * Convert a temperature value
     * @param value Value to convert
     * @return Converted value
     */
    Double convert(double value);

    /**
     * Get the description of this conversion strategy
     * @return Strategy description
     */
    String getDescription();
}