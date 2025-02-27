package com.se498.dailyreporting.domain.bo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * WindSpeed value object
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WindSpeed {
    private Double mph;

    public static WindSpeed fromMph(Double mph) {
        if (mph == null) {
            throw new IllegalArgumentException("Wind speed cannot be null");
        }
        if (mph < 0) {
            throw new IllegalArgumentException("Wind speed cannot be negative: " + mph);
        }
        return new WindSpeed(mph);
    }

    public static WindSpeed fromKph(Double kph) {
        if (kph == null) {
            throw new IllegalArgumentException("Wind speed cannot be null");
        }
        if (kph < 0) {
            throw new IllegalArgumentException("Wind speed cannot be negative: " + kph);
        }
        return new WindSpeed(kph / 1.60934);
    }

    public static WindSpeed fromMetersPerSecond(Double mps) {
        if (mps == null) {
            throw new IllegalArgumentException("Wind speed cannot be null");
        }
        if (mps < 0) {
            throw new IllegalArgumentException("Wind speed cannot be negative: " + mps);
        }
        return new WindSpeed(mps * 2.23694);
    }

    public static WindSpeed fromKnots(Double knots) {
        if (knots == null) {
            throw new IllegalArgumentException("Wind speed cannot be null");
        }
        if (knots < 0) {
            throw new IllegalArgumentException("Wind speed cannot be negative: " + knots);
        }
        return new WindSpeed(knots * 1.15078);
    }

    /**
     * Get wind speed in kilometers per hour
     */
    public Double getKph() {
        return mph * 1.60934;
    }

    /**
     * Get wind speed in meters per second
     */
    public Double getMps() {
        return mph / 2.23694;
    }

    /**
     * Get wind speed in knots
     */
    public Double getKnots() {
        return mph / 1.15078;
    }

    /**
     * Check if wind is calm (< 5 mph)
     */
    public boolean isCalm() {
        return mph < 5;
    }

    /**
     * Check if wind is moderate (5-15 mph)
     */
    public boolean isModerate() {
        return mph >= 5 && mph <= 15;
    }

    /**
     * Check if wind is strong (15-30 mph)
     */
    public boolean isStrong() {
        return mph > 15 && mph <= 30;
    }

    /**
     * Check if wind is at dangerous/gale levels (> 30 mph)
     */
    public boolean isDangerous() {
        return mph > 30;
    }

    /**
     * Get the Beaufort scale number (0-12) for this wind speed
     */
    public int getBeaufortScale() {
        if (mph < 1) return 0;      // Calm
        if (mph < 4) return 1;      // Light air
        if (mph < 8) return 2;      // Light breeze
        if (mph < 13) return 3;     // Gentle breeze
        if (mph < 19) return 4;     // Moderate breeze
        if (mph < 25) return 5;     // Fresh breeze
        if (mph < 32) return 6;     // Strong breeze
        if (mph < 39) return 7;     // Near gale
        if (mph < 47) return 8;     // Gale
        if (mph < 55) return 9;     // Strong gale
        if (mph < 64) return 10;    // Storm
        if (mph < 73) return 11;    // Violent storm
        return 12;                  // Hurricane
    }

    @Override
    public String toString() {
        return String.format("%.1f mph (%.1f km/h)", mph, getKph());
    }
}