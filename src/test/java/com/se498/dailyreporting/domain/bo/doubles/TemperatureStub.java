package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Temperature;

/**
 * Temperature stub with predefined behavior
 *
 * Stubs provide canned answers to calls made during the test.
 * They don't respond to anything outside what's programmed for the test.
 */
public class TemperatureStub extends Temperature {
    private final double fahrenheit;
    private final boolean isModerate;
    private final boolean isHot;
    private final boolean isCold;

    /**
     * Create Temperature stub with specific hardcoded responses
     */
    public TemperatureStub(double fahrenheit, boolean isModerate, boolean isHot, boolean isCold) {
        // Calculate celsius based on fahrenheit for consistency
        super(fahrenheit, (fahrenheit - 32) * 5 / 9);
        this.fahrenheit = fahrenheit;
        this.isModerate = isModerate;
        this.isHot = isHot;
        this.isCold = isCold;
    }

    /**
     * Simple constructor that calculates temperature states automatically
     */
    public static TemperatureStub withValue(double fahrenheit) {
        boolean isHot = fahrenheit > 85;
        boolean isCold = fahrenheit <= 32;
        boolean isModerate = !isHot && !isCold;
        return new TemperatureStub(fahrenheit, isModerate, isHot, isCold);
    }

    @Override
    public Double getFahrenheit() {
        return fahrenheit;
    }

    @Override
    public boolean isModerate() {
        return isModerate;
    }

    @Override
    public boolean isHot() {
        return isHot;
    }

    @Override
    public boolean isCold() {
        return isCold;
    }
}