package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TemperatureBasicAssertionsTest {

    private Temperature roomTemp;
    private Temperature freezingTemp;
    private Temperature boilingTemp;
    private Temperature nullTemp;

    @BeforeEach
    void setUp() {
        roomTemp = Temperature.fromFahrenheit(72.0);
        freezingTemp = Temperature.fromFahrenheit(32.0);
        boilingTemp = Temperature.fromFahrenheit(212.0);
        nullTemp = null;
    }

    @Test
    void testAssertEquals() {
        Temperature expected = Temperature.fromFahrenheit(72.0);
        assertEquals(expected.getFahrenheit(), roomTemp.getFahrenheit(),
                "Fahrenheit temperatures should be equal");
        assertEquals(expected.getCelsius(), roomTemp.getCelsius(),
                "Celsius temperatures should be equal");
    }

    @Test
    void testAssertNotEquals() {
        assertNotEquals(freezingTemp.getFahrenheit(), boilingTemp.getFahrenheit(),
                "Freezing and boiling temperatures should not be equal");
        assertNotEquals(freezingTemp.getCelsius(), boilingTemp.getCelsius(),
                "Freezing and boiling temperatures in Celsius should not be equal");
    }

    @Test
    void testAssertTrue() {
        assertTrue(boilingTemp.isHot(),
                "Boiling temperature should be considered hot");
        assertTrue(freezingTemp.isCold(),
                "Freezing temperature should be considered cold");
    }

    @Test
    void testAssertFalse() {
        assertFalse(roomTemp.isHot(),
                "Room temperature should not be considered hot");
        assertFalse(roomTemp.isCold(),
                "Room temperature should not be considered cold");
    }

    @Test
    void testAssertNull() {
        assertNull(nullTemp,
                "Null temperature object should be null");
    }

    @Test
    void testAssertNotNull() {
        assertNotNull(roomTemp,
                "Room temperature object should not be null");
    }

    @Test
    void testAssertSame() {
        Temperature temp1 = Temperature.fromFahrenheit(72.0);
        Temperature temp2 = temp1;
        assertSame(temp1, temp2,
                "References should point to the same object");
    }

    @Test
    void testAssertNotSame() {
        Temperature temp1 = Temperature.fromFahrenheit(72.0);
        Temperature temp2 = Temperature.fromFahrenheit(72.0);
        assertNotSame(temp1, temp2,
                "Different temperature objects with same values should not be the same reference");
    }

    @Test
    void testAssertAll() {
        assertAll("Temperature validations",
                () -> assertEquals(72.0, roomTemp.getFahrenheit(), "Fahrenheit value should match"),
                () -> assertEquals(22.22, roomTemp.getCelsius(), 0.01, "Celsius value should match"),
                () -> assertTrue(roomTemp.isModerate(), "Should be moderate temperature"),
                () -> assertFalse(roomTemp.isHot(), "Should not be hot"),
                () -> assertFalse(roomTemp.isCold(), "Should not be cold")
        );
    }

    @Test
    void testAssertThrows() {
        assertThrows(NullPointerException.class, () -> {
            Temperature temp = null;
            temp.getFahrenheit();
        }, "Should throw NullPointerException when accessing null object");
    }

    @Test
    void testAssertDoesNotThrow() {
        assertDoesNotThrow(() -> {
            Temperature.fromFahrenheit(72.0);
        }, "Should not throw exception for valid temperature creation");
    }

    @Test
    void testAssertArrayEquals() {
        Temperature[] temps1 = {roomTemp, freezingTemp, boilingTemp};
        Temperature[] temps2 = {roomTemp, freezingTemp, boilingTemp};
        assertArrayEquals(temps1, temps2,
                "Temperature arrays should be equal");
    }

    @Test
    void testAssertWithDelta() {
        double expectedCelsius = 22.22;
        assertEquals(expectedCelsius, roomTemp.getCelsius(), 0.01,
                "Celsius conversion should be within 0.01 of expected value");
    }

    @Test
    void testAssertWithMessage() {
        assertTrue(boilingTemp.isHot(), () ->
                "Temperature of " + boilingTemp.getFahrenheit() + "°F should be considered hot");
    }
}
