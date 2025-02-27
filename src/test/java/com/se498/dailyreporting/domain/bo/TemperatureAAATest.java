package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Temperature AAA Pattern Tests")
class TemperatureAAATest {

    @Test
    @DisplayName("Test Fahrenheit to Celsius conversion")
    void testFahrenheitToCelsius() {
        // Arrange
        double initialFahrenheit = 212.0;

        // Act
        Temperature temperature = Temperature.fromFahrenheit(initialFahrenheit);
        double celsius = temperature.getCelsius();

        // Assert
        assertEquals(100.0, celsius, 0.01);
    }

    @Test
    @DisplayName("Test hot temperature classification")
    void testHotTemperature() {
        // Arrange
        double hotFahrenheit = 90.0;

        // Act
        Temperature temperature = Temperature.fromFahrenheit(hotFahrenheit);
        boolean isHot = temperature.isHot();

        // Assert
        assertTrue(isHot);
    }

    @Test
    @DisplayName("Test cold temperature classification")
    void testColdTemperature() {
        // Arrange
        double coldFahrenheit = 20.0;

        // Act
        Temperature temperature = Temperature.fromFahrenheit(coldFahrenheit);
        boolean isCold = temperature.isCold();

        // Assert
        assertTrue(isCold);
    }

    @Test
    @DisplayName("Test moderate temperature classification")
    void testModerateTemperature() {
        // Arrange
        double moderateFahrenheit = 70.0;

        // Act
        Temperature temperature = Temperature.fromFahrenheit(moderateFahrenheit);
        boolean isModerate = temperature.isModerate();

        // Assert
        assertTrue(isModerate);
    }

    @Test
    @DisplayName("Test temperature string representation")
    void testTemperatureString() {
        // Arrange
        double fahrenheit = 72.0;
        Temperature temperature = Temperature.fromFahrenheit(fahrenheit);
        String expected = "72.0°F (22.2°C)";

        // Act
        String result = temperature.toString();

        // Assert
        assertEquals(expected, result);
    }
}