package com.se498.dailyreporting.domain.bo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Humidity value object with validation
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Humidity {
    private Integer percentage;

    public static Humidity of(Integer percentage) {
        if (percentage == null) {
            throw new IllegalArgumentException("Humidity cannot be null");
        }
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Humidity must be between 0 and 100 percent, got: " + percentage);
        }
        return new Humidity(percentage);
    }

    /**
     * Determines if humidity is considered high (above 80%)
     */
    public boolean isHigh() {
        return percentage > 80;
    }

    /**
     * Determines if humidity is considered low (below 20%)
     */
    public boolean isLow() {
        return percentage < 20;
    }

    /**
     * Determines if humidity is in the comfortable range (30-60%)
     */
    public boolean isComfortable() {
        return percentage >= 30 && percentage <= 60;
    }

    /**
     * Get comfort category of current humidity
     */
    public HumidityComfort getComfortCategory() {
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

    /**
     * Enumeration of humidity comfort levels
     */
    public enum HumidityComfort {
        TOO_DRY,
        DRY,
        COMFORTABLE,
        HUMID,
        TOO_HUMID
    }
}