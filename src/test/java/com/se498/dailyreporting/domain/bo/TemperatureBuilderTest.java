package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Temperature Builder Pattern Tests")
class TemperatureBuilderTest {

    static class TemperatureTestBuilder {
        private Double fahrenheit;
        private Double celsius;

        private TemperatureTestBuilder() {}
        public static TemperatureTestBuilder aTemperature() {
            return new TemperatureTestBuilder();
        }

        public TemperatureTestBuilder withFahrenheit(double fahrenheit) {
            this.fahrenheit = fahrenheit;
            return this;
        }

        public TemperatureTestBuilder withCelsius(double celsius) {
            this.celsius = celsius;
            return this;
        }

        public Temperature build() {
            if (fahrenheit != null) {
                return Temperature.fromFahrenheit(fahrenheit);
            } else if (celsius != null) {
                return Temperature.fromCelsius(celsius);
            }
            throw new IllegalStateException("Either Fahrenheit or Celsius must be set");
        }
    }

    @Test
    @DisplayName("Build temperature from Fahrenheit")
    void testBuildFromFahrenheit() {
        Temperature temperature = TemperatureTestBuilder.aTemperature()
                .withFahrenheit(212.0)
                .build();

        assertEquals(212.0, temperature.getFahrenheit());
        assertEquals(100.0, temperature.getCelsius(), 0.01);
    }

    @Test
    @DisplayName("Build temperature from Celsius")
    void testBuildFromCelsius() {
        Temperature temperature = TemperatureTestBuilder.aTemperature()
                .withCelsius(100.0)
                .build();

        assertEquals(212.0, temperature.getFahrenheit(), 0.01);
        assertEquals(100.0, temperature.getCelsius());
    }

    @Test
    @DisplayName("Build hot temperature")
    void testBuildHotTemperature() {
        Temperature temperature = TemperatureTestBuilder.aTemperature()
                .withFahrenheit(90.0)
                .build();

        assertTrue(temperature.isHot());
    }

    @Test
    @DisplayName("Build cold temperature")
    void testBuildColdTemperature() {
        Temperature temperature = TemperatureTestBuilder.aTemperature()
                .withFahrenheit(20.0)
                .build();

        assertTrue(temperature.isCold());
    }

    @Test
    @DisplayName("Build moderate temperature")
    void testBuildModerateTemperature() {
        Temperature temperature = TemperatureTestBuilder.aTemperature()
                .withFahrenheit(70.0)
                .build();

        assertTrue(temperature.isModerate());
    }

    @Test
    @DisplayName("Test builder validation")
    void testBuilderValidation() {
        assertThrows(IllegalStateException.class, () -> {
            TemperatureTestBuilder.aTemperature().build();
        });
    }
}