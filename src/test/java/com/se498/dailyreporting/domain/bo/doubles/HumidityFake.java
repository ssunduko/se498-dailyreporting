package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Humidity;

/**
 * Fake Humidity class with simplified working implementation
 *
 * Fakes are working implementations with simplified functionality
 * that are suitable for testing but not for production.
 */
public class HumidityFake extends Humidity {
    private int percentage;

    public HumidityFake(int percentage) {
        super(percentage);
        this.percentage = percentage;
    }

    /**
     * Test-only method to change humidity during test
     */
    public void setPercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Humidity must be between 0 and 100 percent");
        }
        this.percentage = percentage;
    }

    @Override
    public Integer getPercentage() {
        return percentage;
    }

    @Override
    public boolean isHigh() {
        return percentage > 80; // Real implementation
    }

    @Override
    public boolean isLow() {
        return percentage < 20; // Real implementation
    }

    @Override
    public boolean isComfortable() {
        return percentage >= 30 && percentage <= 60; // Real implementation
    }

    @Override
    public HumidityComfort getComfortCategory() {
        // Real implementation
        if (percentage < 20) return HumidityComfort.TOO_DRY;
        if (percentage < 30) return HumidityComfort.DRY;
        if (percentage <= 60) return HumidityComfort.COMFORTABLE;
        if (percentage <= 80) return HumidityComfort.HUMID;
        return HumidityComfort.TOO_HUMID;
    }

    @Override
    public String toString() {
        return percentage + "%";
    }

}