package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;

@DisplayName("Temperature Test Suite with Annotations")
class TemperatureAnnotatedTest {

    private Temperature roomTemp;
    private Temperature freezingTemp;
    private Temperature boilingTemp;

    @BeforeEach
    void setUp() {
        roomTemp = Temperature.fromFahrenheit(72.0);
        freezingTemp = Temperature.fromFahrenheit(32.0);
        boilingTemp = Temperature.fromFahrenheit(212.0);
    }

    @Test
    @DisplayName("Room temperature should be moderate")
    @Tag("classification")
    @Timeout(1)
    void testRoomTemperatureClassification() {
        assertTrue(roomTemp.isModerate(), "Room temperature should be moderate");
        assertFalse(roomTemp.isHot(), "Room temperature should not be hot");
        assertFalse(roomTemp.isCold(), "Room temperature should not be cold");
    }

    @Test
    @DisplayName("Freezing point temperature should be cold")
    @Tag("classification")
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    void testFreezingTemperatureClassification() {
        assertTrue(freezingTemp.isCold(), "Freezing temperature should be cold");
        assertFalse(freezingTemp.isHot(), "Freezing temperature should not be hot");
        assertFalse(freezingTemp.isModerate(), "Freezing temperature should not be moderate");
    }

    @Test
    @DisplayName("Boiling point temperature should be hot")
    @Tag("classification")
    @Timeout(1000)
    void testBoilingTemperatureClassification() {
        assertTrue(boilingTemp.isHot(), "Boiling temperature should be hot");
        assertFalse(boilingTemp.isCold(), "Boiling temperature should not be cold");
        assertFalse(boilingTemp.isModerate(), "Boiling temperature should not be moderate");
    }

    @Test
    @DisplayName("Test Fahrenheit to Celsius conversion")
    @Tag("conversion")
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    void testFahrenheitToCelsiusConversion() {
        assertEquals(22.22, roomTemp.getCelsius(), 0.01, "Fahrenheit to Celsius conversion error");
        assertEquals(0.0, freezingTemp.getCelsius(), 0.01, "Freezing point conversion error");
        assertEquals(100.0, boilingTemp.getCelsius(), 0.01, "Boiling point conversion error");
    }

    @Test
    @DisplayName("Test temperature string representation")
    @Tag("formatting")
    @Timeout(1)
    void testTemperatureToString() {
        assertEquals("72.0°F (22.2°C)", roomTemp.toString(), "Room temperature string format error");
        assertEquals("32.0°F (0.0°C)", freezingTemp.toString(), "Freezing point string format error");
        assertEquals("212.0°F (100.0°C)", boilingTemp.toString(), "Boiling point string format error");
    }

    @Test
    @DisplayName("Test extreme temperature handling")
    @Tag("edge-cases")
    @Disabled("Not ready for production - needs performance optimization")
    @Timeout(2)
    void testExtremeTemperatures() {
        Temperature extremeHot = Temperature.fromFahrenheit(1000.0);
        Temperature extremeCold = Temperature.fromFahrenheit(-100.0);

        assertTrue(extremeHot.isHot(), "Extreme hot should be classified as hot");
        assertTrue(extremeCold.isCold(), "Extreme cold should be classified as cold");
    }

    @Test
    @DisplayName("Test temperature at -40 degrees (F/C intersection)")
    @Tag("edge-cases")
    @Tag("conversion")
    @Timeout(value = 1500, unit = TimeUnit.MILLISECONDS)
    void testMinusFortyIntersection() {
        Temperature minusForty = Temperature.fromFahrenheit(-40.0);
        assertEquals(minusForty.getFahrenheit(), minusForty.getCelsius(),
                "At -40 degrees, Fahrenheit and Celsius should be equal");
    }

    @Test
    @DisplayName("Test invalid temperature handling")
    @Tag("error-handling")
    @Disabled("Implementation of validation pending")
    @Timeout(1)
    void testInvalidTemperature() {
        assertThrows(NullPointerException.class, () ->
                Temperature.fromFahrenheit(null), "Should throw NullPointerException for null input");
    }

    @Test
    @DisplayName("Test moderate temperature range boundaries")
    @Tag("classification")
    @Tag("edge-cases")
    void testModerateTemperatureBoundaries() {
        Temperature lowerBound = Temperature.fromFahrenheit(32.1);
        Temperature upperBound = Temperature.fromFahrenheit(85.0);

        assertTrue(lowerBound.isModerate(), "32°F should be moderate");
        assertTrue(upperBound.isModerate(), "85°F should be moderate");
    }

    @Test
    @DisplayName("Performance test for multiple conversions")
    @Tag("performance")
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    void testConversionPerformance() {
        for (int i = -100; i <= 212; i++) {
            Temperature temp = Temperature.fromFahrenheit((double) i);
            assertNotNull(temp.getCelsius(), "Celsius conversion should not be null");
        }
    }

    @Test
    @DisplayName("Test temperature classification changes")
    @Tag("classification")
    @Tag("integration")
    @Disabled("Integration test environment not ready")
    @Timeout(2)
    void testTemperatureClassificationChanges() {
        Temperature changingTemp = Temperature.fromFahrenheit(80.0);
        assertTrue(changingTemp.isModerate(), "80°F should start as moderate");

        changingTemp = Temperature.fromFahrenheit(90.0);
        assertTrue(changingTemp.isHot(), "90°F should be hot");

        changingTemp = Temperature.fromFahrenheit(20.0);
        assertTrue(changingTemp.isCold(), "20°F should be cold");
    }

    @Test
    @DisplayName("Verify temperature object immutability")
    @Tag("security")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testTemperatureImmutability() {
        Temperature initial = Temperature.fromFahrenheit(72.0);
        double initialFahrenheit = initial.getFahrenheit();
        double initialCelsius = initial.getCelsius();

        // Create new temperature object
        Temperature another = Temperature.fromFahrenheit(100.0);

        // Verify original temperature remains unchanged
        assertEquals(initialFahrenheit, initial.getFahrenheit(),
                "Fahrenheit value should remain unchanged");
        assertEquals(initialCelsius, initial.getCelsius(),
                "Celsius value should remain unchanged");
    }
}