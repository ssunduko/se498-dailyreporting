package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Temperature Given-When-Then Pattern Tests")
class TemperatureGivenWhenThenTest {

    @Test
    @DisplayName("Test hot temperature classification")
    void testHotTemperature() {
        // Given
        double hotTemperature = 90.0;
        Temperature temperature = Temperature.fromFahrenheit(hotTemperature);

        // When
        boolean isHot = temperature.isHot();

        // Then
        assertTrue(isHot, "Temperature should be classified as hot");
        assertFalse(temperature.isCold(), "Hot temperature should not be cold");
        assertFalse(temperature.isModerate(), "Hot temperature should not be moderate");
    }

    @Test
    @DisplayName("Test temperature conversion")
    void testTemperatureConversion() {
        // Given
        double fahrenheit = 32.0;

        // When
        Temperature temperature = Temperature.fromFahrenheit(fahrenheit);

        // Then
        assertEquals(0.0, temperature.getCelsius(), 0.01,
                "Freezing point conversion should be accurate");
    }

    @Test
    @DisplayName("Test moderate temperature range")
    void testModerateTemperature() {
        // Given
        double moderateTemp = 75.0;
        Temperature temperature = Temperature.fromFahrenheit(moderateTemp);

        // When
        boolean isModerate = temperature.isModerate();

        // Then
        assertTrue(isModerate, "Temperature should be moderate");
        assertEquals(23.89, temperature.getCelsius(), 0.01,
                "Celsius conversion should be accurate");
    }

    @Test
    @DisplayName("Test extreme cold temperature")
    void testExtremeColdTemperature() {
        // Given
        double extremeCold = -40.0;

        // When
        Temperature temperature = Temperature.fromFahrenheit(extremeCold);

        // Then
        assertTrue(temperature.isCold(), "Temperature should be cold");
        assertEquals(-40.0, temperature.getCelsius(), 0.01,
                "F and C should be equal at -40");
    }

    @Test
    @DisplayName("Test temperature string formatting")
    void testTemperatureFormatting() {
        // Given
        Temperature temperature = Temperature.fromFahrenheit(98.6);

        // When
        String formatted = temperature.toString();

        // Then
        assertEquals("98.6°F (37.0°C)", formatted,
                "String representation should be correctly formatted");
    }
}