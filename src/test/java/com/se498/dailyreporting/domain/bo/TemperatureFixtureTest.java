package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Temperature Fixture Pattern Tests")
class TemperatureFixtureTest {

    static class TemperatureTestFixture {
        final Temperature freezingPoint;
        final Temperature roomTemperature;
        final Temperature bodyTemperature;
        final Temperature boilingPoint;
        final Temperature hotTemperature;
        final Temperature coldTemperature;
        final Temperature moderateTemperature;

        TemperatureTestFixture() {
            freezingPoint = Temperature.fromFahrenheit(32.0);
            roomTemperature = Temperature.fromFahrenheit(72.0);
            bodyTemperature = Temperature.fromFahrenheit(98.6);
            boilingPoint = Temperature.fromFahrenheit(212.0);
            hotTemperature = Temperature.fromFahrenheit(90.0);
            coldTemperature = Temperature.fromFahrenheit(20.0);
            moderateTemperature = Temperature.fromFahrenheit(75.0);
        }
    }

    private TemperatureTestFixture fixture;

    @BeforeEach
    void setUp() {
        fixture = new TemperatureTestFixture();
    }

    @Test
    @DisplayName("Test temperature conversion using fixture")
    void testTemperatureConversion() {
        assertEquals(0.0, fixture.freezingPoint.getCelsius(), 0.01);
        assertEquals(100.0, fixture.boilingPoint.getCelsius(), 0.01);
        assertEquals(37.0, fixture.bodyTemperature.getCelsius(), 0.01);
        assertEquals(22.22, fixture.roomTemperature.getCelsius(), 0.01);
    }

    @Test
    @DisplayName("Test temperature classifications using fixture")
    void testTemperatureClassifications() {
        assertTrue(fixture.hotTemperature.isHot());
        assertTrue(fixture.coldTemperature.isCold());
        assertTrue(fixture.moderateTemperature.isModerate());
    }

    @Test
    @DisplayName("Test standard points classifications")
    void testStandardPointsClassifications() {
        assertTrue(fixture.freezingPoint.isCold());
        assertTrue(fixture.roomTemperature.isModerate());
        assertTrue(fixture.boilingPoint.isHot());
    }

    @Test
    @DisplayName("Test temperature string representations")
    void testTemperatureStrings() {
        assertEquals("32.0°F (0.0°C)", fixture.freezingPoint.toString());
        assertEquals("72.0°F (22.2°C)", fixture.roomTemperature.toString());
        assertEquals("212.0°F (100.0°C)", fixture.boilingPoint.toString());
    }

    @Test
    @DisplayName("Test multiple temperature properties")
    void testMultipleProperties() {
        assertAll("Temperature properties",
                () -> assertEquals(32.0, fixture.freezingPoint.getFahrenheit()),
                () -> assertEquals(72.0, fixture.roomTemperature.getFahrenheit()),
                () -> assertEquals(212.0, fixture.boilingPoint.getFahrenheit()),
                () -> assertTrue(fixture.hotTemperature.isHot()),
                () -> assertTrue(fixture.coldTemperature.isCold()),
                () -> assertTrue(fixture.moderateTemperature.isModerate())
        );
    }

    @Test
    @DisplayName("Test temperature relationships")
    void testTemperatureRelationships() {
        assertTrue(fixture.boilingPoint.getFahrenheit() > fixture.roomTemperature.getFahrenheit());
        assertTrue(fixture.roomTemperature.getFahrenheit() > fixture.freezingPoint.getFahrenheit());
        assertTrue(fixture.hotTemperature.getFahrenheit() > fixture.moderateTemperature.getFahrenheit());
        assertTrue(fixture.moderateTemperature.getFahrenheit() > fixture.coldTemperature.getFahrenheit());
    }

    @Test
    @DisplayName("Test temperature boundary conditions")
    void testTemperatureBoundaries() {
        assertAll("Temperature boundaries",
                () -> assertFalse(fixture.freezingPoint.isHot()),
                () -> assertFalse(fixture.boilingPoint.isCold()),
                () -> assertFalse(fixture.hotTemperature.isModerate()),
                () -> assertFalse(fixture.coldTemperature.isModerate())
        );
    }

    @Test
    @DisplayName("Test composite temperature behaviors")
    void testCompositeBehaviors() {
        Temperature[] moderateTemps = {
                fixture.roomTemperature,
                fixture.moderateTemperature
        };

        Temperature[] extremeTemps = {
                fixture.hotTemperature,
                fixture.coldTemperature,
                fixture.boilingPoint,
                fixture.freezingPoint
        };

        for (Temperature temp : moderateTemps) {
            assertTrue(temp.isModerate(),
                    "Temperature " + temp.getFahrenheit() + "°F should be moderate");
        }

        for (Temperature temp : extremeTemps) {
            assertFalse(temp.isModerate(),
                    "Temperature " + temp.getFahrenheit() + "°F should not be moderate");
        }
    }
}