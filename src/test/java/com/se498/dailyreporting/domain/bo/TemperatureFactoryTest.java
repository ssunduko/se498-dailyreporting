package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Temperature Factory Pattern Tests")
class TemperatureFactoryTest {

    static class TemperatureTestFactory {
        static Temperature createFreezingPoint() {
            return Temperature.fromFahrenheit(32.0);
        }
        static Temperature createBoilingPoint() {
            return Temperature.fromFahrenheit(212.0);
        }
        static Temperature createRoomTemperature() {
            return Temperature.fromFahrenheit(72.0);
        }
        static Temperature createBodyTemperature() {
            return Temperature.fromFahrenheit(98.6);
        }
        static Temperature createHotTemperature() {
            return Temperature.fromFahrenheit(90.0);
        }
        static Temperature createColdTemperature() {
            return Temperature.fromFahrenheit(20.0);
        }
    }

    @Test
    @DisplayName("Test freezing point creation")
    void testFreezingPoint() {
        Temperature freezingPoint = TemperatureTestFactory.createFreezingPoint();
        assertEquals(0.0, freezingPoint.getCelsius(), 0.01);
        assertTrue(freezingPoint.isCold());
    }

    @Test
    @DisplayName("Test boiling point creation")
    void testBoilingPoint() {
        Temperature boilingPoint = TemperatureTestFactory.createBoilingPoint();
        assertEquals(100.0, boilingPoint.getCelsius(), 0.01);
        assertTrue(boilingPoint.isHot());
    }

    @Test
    @DisplayName("Test room temperature creation")
    void testRoomTemperature() {
        Temperature roomTemp = TemperatureTestFactory.createRoomTemperature();
        assertTrue(roomTemp.isModerate());
        assertEquals(22.22, roomTemp.getCelsius(), 0.01);
    }

    @Test
    @DisplayName("Test body temperature creation")
    void testBodyTemperature() {
        Temperature bodyTemp = TemperatureTestFactory.createBodyTemperature();
        assertEquals(37.0, bodyTemp.getCelsius(), 0.01);
        assertTrue(bodyTemp.isHot());
    }

    @Test
    @DisplayName("Test hot temperature creation")
    void testHotTemperature() {
        Temperature hotTemp = TemperatureTestFactory.createHotTemperature();
        assertTrue(hotTemp.isHot());
        assertFalse(hotTemp.isModerate());
    }

    @Test
    @DisplayName("Test cold temperature creation")
    void testColdTemperature() {
        Temperature coldTemp = TemperatureTestFactory.createColdTemperature();
        assertTrue(coldTemp.isCold());
        assertFalse(coldTemp.isModerate());
    }
}