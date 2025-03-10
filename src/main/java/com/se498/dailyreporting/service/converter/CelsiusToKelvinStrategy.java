package com.se498.dailyreporting.service.converter;

/**
 * Strategy implementation for Celsius to Kelvin conversion
 */
public class CelsiusToKelvinStrategy implements ConversionStrategy {
    @Override
    public Double convert(double celsius) {
        return celsius + 273.15;
    }

    @Override
    public String getDescription() {
        return "Celsius to Kelvin";
    }
}