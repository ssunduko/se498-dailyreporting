package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Humidity;

/**
 * Humidity stub with predefined behavior
 *
 * Stubs provide canned answers to calls made during the test.
 * They don't respond to anything outside what's programmed for the test.
 */
public class HumidityStub extends Humidity {
    private final int percentage;
    private final boolean isHigh;
    private final boolean isLow;
    private final boolean isComfortable;
    private final HumidityComfort comfortCategory;

    /**
     * Create Humidity stub with specific hardcoded responses
     */
    public HumidityStub(int percentage, boolean isHigh, boolean isLow,
                        boolean isComfortable, HumidityComfort comfortCategory) {
        super(percentage);
        this.percentage = percentage;
        this.isHigh = isHigh;
        this.isLow = isLow;
        this.isComfortable = isComfortable;
        this.comfortCategory = comfortCategory;
    }

    /**
     * Simple constructor that calculates humidity states automatically
     */
    public static HumidityStub withValue(int percentage) {
        boolean isHigh = percentage > 80;
        boolean isLow = percentage < 20;
        boolean isComfortable = percentage >= 30 && percentage <= 60;

        HumidityComfort category;
        if (percentage < 20) category = HumidityComfort.TOO_DRY;
        else if (percentage < 30) category = HumidityComfort.DRY;
        else if (percentage <= 60) category = HumidityComfort.COMFORTABLE;
        else if (percentage <= 80) category = HumidityComfort.HUMID;
        else category = HumidityComfort.TOO_HUMID;

        return new HumidityStub(percentage, isHigh, isLow, isComfortable, category);
    }

    @Override
    public Integer getPercentage() {
        return percentage;
    }

    @Override
    public boolean isHigh() {
        return isHigh;
    }

    @Override
    public boolean isLow() {
        return isLow;
    }

    @Override
    public boolean isComfortable() {
        return isComfortable;
    }

    @Override
    public HumidityComfort getComfortCategory() {
        return comfortCategory;
    }
}
