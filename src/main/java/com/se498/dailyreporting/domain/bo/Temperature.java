package com.se498.dailyreporting.domain.bo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Temperature value object that handles conversions
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Temperature {
    private Double fahrenheit;
    private Double celsius;

    public static Temperature fromFahrenheit(Double fahrenheit) {
        Double celsius = (fahrenheit - 32) * 5 / 9;
        return new Temperature(fahrenheit, celsius);
    }

    public static Temperature fromCelsius(Double celsius) {
        Double fahrenheit = celsius * 9 / 5 + 32;
        return new Temperature(fahrenheit, celsius);
    }

    public boolean isHot() {
        return fahrenheit > 85;
    }

    public boolean isCold() {
        return fahrenheit <= 32;
    }

    public boolean isModerate() {
        return !isHot() && !isCold();
    }

    @Override
    public String toString() {
        return String.format("%.1f°F (%.1f°C)", fahrenheit, celsius);
    }
}