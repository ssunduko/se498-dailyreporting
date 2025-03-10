package com.se498.dailyreporting.service.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(SpringExtension.class)
public class CompleteTemperatureConverterTest {

    private AbstractTemperatureConverter temperatureConverter;

    @BeforeEach
    void setUp() {
        temperatureConverter = UniversalTemperatureConverter.getInstance();
    }

    @Test
    void testSingletonInstance() {
        UniversalTemperatureConverter instance1 = UniversalTemperatureConverter.getInstance();
        UniversalTemperatureConverter instance2 = UniversalTemperatureConverter.getInstance();

        assertSame(instance1, instance2, "Singleton should return the same instance");
    }

    @ParameterizedTest
    @CsvSource({"0,32", "1,33.8", "2,35.6", "3,37.4", "4,39.2"})
    void convertCelsiusToFahrenheitTest(double numberInCelsius, double expectedNumberInFahrenheit) {
        double convertedTemperature = temperatureConverter.convert(
                numberInCelsius, "Celsius to Fahrenheit");
        assertEquals(expectedNumberInFahrenheit, convertedTemperature, 0.01);
    }

    @ParameterizedTest
    @CsvSource({"0,273.15", "1,274.15", "2,275.15", "3,276.15", "4,277.15"})
    void convertCelsiusToKelvinTest(double numberInCelsius, double expectedNumberInKelvin) {
        double convertedTemperature = temperatureConverter.convert(
                numberInCelsius, "Celsius to Kelvin");
        assertEquals(expectedNumberInKelvin, convertedTemperature, 0.01);
    }

    @Test
    void testAvailableStrategies() {
        assertNotNull(temperatureConverter.getAvailableStrategies());
        assertEquals(2, temperatureConverter.getAvailableStrategies().size(),
                "Should have two conversion strategies");
    }
}