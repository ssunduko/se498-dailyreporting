package com.se498.dailyreporting.service.converter;

/**
 * Strategy implementation for Celsius to Fahrenheit conversion
 */
public class CelsiusToFahrenheitStrategy implements ConversionStrategy {
    @Override
    public Double convert(double celsius) {
        return celsius * 9/5 + 32;
    }

    @Override
    public String getDescription() {
        return "Celsius to Fahrenheit";
    }
}