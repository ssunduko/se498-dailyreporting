package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureTest {

    @Test
    void fromFahrenheit_ShouldConvertCorrectly() {
        // Given
        double fahrenheit = 68.0;

        // When
        Temperature temperature = Temperature.fromFahrenheit(fahrenheit);

        // Then
        assertEquals(fahrenheit, temperature.getFahrenheit());
        assertEquals(20.0, temperature.getCelsius(), 0.001);
    }

    @Test
    void fromCelsius_ShouldConvertCorrectly() {
        // Given
        double celsius = 25.0;

        // When
        Temperature temperature = Temperature.fromCelsius(celsius);

        // Then
        assertEquals(celsius, temperature.getCelsius());
        assertEquals(77.0, temperature.getFahrenheit(), 0.001);
    }

    @ParameterizedTest
    @ValueSource(doubles = {86.0, 90.5, 100.0})
    void isHot_ShouldReturnTrue_WhenTemperatureAbove85F(double fahrenheit) {
        // Given
        Temperature temperature = Temperature.fromFahrenheit(fahrenheit);

        // Then
        assertTrue(temperature.isHot(), "Temperature " + fahrenheit + "°F should be considered hot");
    }

    @ParameterizedTest
    @ValueSource(doubles = {85.0, 70.0, 40.0})
    void isHot_ShouldReturnFalse_WhenTemperatureBelow85F(double fahrenheit) {
        // Given
        Temperature temperature = Temperature.fromFahrenheit(fahrenheit);

        // Then
        assertFalse(temperature.isHot(), "Temperature " + fahrenheit + "°F should not be considered hot");
    }

    @ParameterizedTest
    @ValueSource(doubles = {31.9, 15.0, 0.0, -10.0})
    void isCold_ShouldReturnTrue_WhenTemperatureBelowFreezingPoint(double fahrenheit) {
        // Given
        Temperature temperature = Temperature.fromFahrenheit(fahrenheit);

        // Then
        assertTrue(temperature.isCold(), "Temperature " + fahrenheit + "°F should be considered cold");
    }

    @ParameterizedTest
    @ValueSource(doubles = {32.1, 40.0, 60.0})
    void isCold_ShouldReturnFalse_WhenTemperatureAboveFreezingPoint(double fahrenheit) {
        // Given
        Temperature temperature = Temperature.fromFahrenheit(fahrenheit);

        // Then
        assertFalse(temperature.isCold(), "Temperature " + fahrenheit + "°F should not be considered cold");
    }

    @ParameterizedTest
    @ValueSource(doubles = {32.1, 60.0, 85.0})
    void isModerate_ShouldReturnTrue_WhenTemperatureBetween32And85(double fahrenheit) {
        // Given
        Temperature temperature = Temperature.fromFahrenheit(fahrenheit);

        // Then
        assertTrue(temperature.isModerate(), "Temperature " + fahrenheit + "°F should be considered moderate");
    }

    @ParameterizedTest
    @ValueSource(doubles = {31.9, 85.1, 0.0, 100.0})
    void isModerate_ShouldReturnFalse_WhenTemperatureNotBetween32And85(double fahrenheit) {
        // Given
        Temperature temperature = Temperature.fromFahrenheit(fahrenheit);

        // Then
        assertFalse(temperature.isModerate(), "Temperature " + fahrenheit + "°F should not be considered moderate");
    }

    @ParameterizedTest
    @CsvSource({
            "32.0, 0.0",
            "212.0, 100.0",
            "68.0, 20.0",
            "-40.0, -40.0"
    })
    void temperatureConversion_ShouldBeAccurate(double fahrenheit, double celsius) {
        // Test fahrenheit to celsius
        Temperature tempFromF = Temperature.fromFahrenheit(fahrenheit);
        assertEquals(celsius, tempFromF.getCelsius(), 0.01, "Fahrenheit to Celsius conversion is incorrect");

        // Test celsius to fahrenheit
        Temperature tempFromC = Temperature.fromCelsius(celsius);
        assertEquals(fahrenheit, tempFromC.getFahrenheit(), 0.01, "Celsius to Fahrenheit conversion is incorrect");
    }

    @Test
    void toString_ShouldReturnFormattedString() {
        // Given
        Temperature temperature = Temperature.fromFahrenheit(75.6);

        // Then
        assertEquals("75.6°F (24.2°C)", temperature.toString());
    }

    @Test
    void constructor_ShouldCreateObjectWithCorrectValues() {
        // Given
        double fahrenheit = 77.0;
        double celsius = 25.0;

        // When
        Temperature temperature = new Temperature(fahrenheit, celsius);

        // Then
        assertEquals(fahrenheit, temperature.getFahrenheit());
        assertEquals(celsius, temperature.getCelsius());
    }

    @Test
    void extremeValues_ShouldHandleCorrectly() {
        // Test very high temperature
        Temperature highTemp = Temperature.fromFahrenheit(212.0); // Boiling point
        assertEquals(100.0, highTemp.getCelsius(), 0.01);
        assertTrue(highTemp.isHot());
        assertFalse(highTemp.isCold());

        // Test very low temperature
        Temperature lowTemp = Temperature.fromFahrenheit(-40.0);
        assertEquals(-40.0, lowTemp.getCelsius(), 0.01); // -40 is where F and C scales meet
        assertFalse(lowTemp.isHot());
        assertTrue(lowTemp.isCold());
    }
}