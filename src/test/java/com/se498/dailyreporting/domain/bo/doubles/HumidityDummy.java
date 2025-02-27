package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Humidity;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Dummy Humidity class that doesn't do anything
 * Will fail if any method is actually called
 *
 * Dummies are objects that are passed around but never actually used.
 * They are just used to fill parameter lists.
 */
public class HumidityDummy extends Humidity {
    public HumidityDummy() {
        super(0); // Value doesn't matter as it won't be used
    }

    @Override
    public Integer getPercentage() {
        fail("getPercentage should not be called on HumidityDummy");
        return null;
    }

    @Override
    public boolean isHigh() {
        fail("isHigh should not be called on HumidityDummy");
        return false;
    }

    @Override
    public boolean isLow() {
        fail("isLow should not be called on HumidityDummy");
        return false;
    }

    @Override
    public boolean isComfortable() {
        fail("isComfortable should not be called on HumidityDummy");
        return false;
    }

    @Override
    public HumidityComfort getComfortCategory() {
        fail("getComfortCategory should not be called on HumidityDummy");
        return null;
    }

    @Override
    public String toString() {
        fail("toString should not be called on HumidityDummy");
        return null;
    }
}