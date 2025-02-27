package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Temperature;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Dummy Temperature class that doesn't do anything
 * Will fail if any method is actually called
 *
 * Dummies are objects that are passed around but never actually used.
 * They are just used to fill parameter lists.
 */
public class TemperatureDummy extends Temperature {
    public TemperatureDummy() {
        super(0.0, 0.0); // Values don't matter as they won't be used
    }

    @Override
    public Double getFahrenheit() {
        fail("getFahrenheit should not be called on TemperatureDummy");
        return null;
    }

    @Override
    public Double getCelsius() {
        fail("getCelsius should not be called on TemperatureDummy");
        return null;
    }

    @Override
    public boolean isHot() {
        fail("isHot should not be called on TemperatureDummy");
        return false;
    }

    @Override
    public boolean isCold() {
        fail("isCold should not be called on TemperatureDummy");
        return false;
    }

    @Override
    public boolean isModerate() {
        fail("isModerate should not be called on TemperatureDummy");
        return false;
    }

    @Override
    public String toString() {
        fail("toString should not be called on TemperatureDummy");
        return null;
    }
}