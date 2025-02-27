package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Temperature;

/**
 * Fake Temperature class with simplified working implementation
 *
 * Fakes are working implementations with simplified functionality
 * that are suitable for testing but not for production.
 */
public class TemperatureFake extends Temperature {
    private double fahrenheit;
    private double celsius;

    /**
     * Test-only method to change temperature values during test
     */
    public void setFahrenheit(double fahrenheit) {
        this.fahrenheit = fahrenheit;
        this.celsius = (fahrenheit - 32) * 5 / 9;
    }

    /**
     * Test-only method to change temperature in Celsius
     */
    public void setCelsius(double celsius) {
        this.celsius = celsius;
        this.fahrenheit = celsius * 9 / 5 + 32;
    }

    public TemperatureFake(double fahrenheit) {
        // Initialize with proper conversion
        super(fahrenheit, (fahrenheit - 32) * 5 / 9);
        this.fahrenheit = fahrenheit;
        this.celsius = (fahrenheit - 32) * 5 / 9;
    }

    @Override
    public Double getFahrenheit() {
        return fahrenheit;
    }

    @Override
    public Double getCelsius() {
        return celsius;
    }

    @Override
    public boolean isHot() {
        return fahrenheit > 85; // Real implementation
    }

    @Override
    public boolean isCold() {
        return fahrenheit <= 32; // Real implementation
    }

    @Override
    public boolean isModerate() {
        return !isHot() && !isCold(); // Real implementation
    }

    @Override
    public String toString() {
        return String.format("%.1f°F (%.1f°C)", fahrenheit, celsius);
    }
}