package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WindSpeed Tests")
class WindSpeedTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Create from MPH")
        void testFromMph() {
            WindSpeed speed = WindSpeed.fromMph(10.0);
            assertEquals(10.0, speed.getMph());
        }

        @Test
        @DisplayName("Create from KPH")
        void testFromKph() {
            WindSpeed speed = WindSpeed.fromKph(16.0934);
            assertEquals(10.0, speed.getMph(), 0.001);
        }

        @Test
        @DisplayName("Create from MPS")
        void testFromMps() {
            WindSpeed speed = WindSpeed.fromMetersPerSecond(4.4704);
            assertEquals(10.0, speed.getMph(), 0.001);
        }

        @Test
        @DisplayName("Create from Knots")
        void testFromKnots() {
            WindSpeed speed = WindSpeed.fromKnots(8.689);
            assertEquals(10.0, speed.getMph(), 0.001);
        }

        @Test
        @DisplayName("Reject null values")
        void testNullValues() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> WindSpeed.fromMph(null)),
                    () -> assertThrows(IllegalArgumentException.class, () -> WindSpeed.fromKph(null)),
                    () -> assertThrows(IllegalArgumentException.class, () -> WindSpeed.fromMetersPerSecond(null)),
                    () -> assertThrows(IllegalArgumentException.class, () -> WindSpeed.fromKnots(null))
            );
        }

        @Test
        @DisplayName("Reject negative values")
        void testNegativeValues() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> WindSpeed.fromMph(-1.0)),
                    () -> assertThrows(IllegalArgumentException.class, () -> WindSpeed.fromKph(-1.0)),
                    () -> assertThrows(IllegalArgumentException.class, () -> WindSpeed.fromMetersPerSecond(-1.0)),
                    () -> assertThrows(IllegalArgumentException.class, () -> WindSpeed.fromKnots(-1.0))
            );
        }
    }

    @Nested
    @DisplayName("Conversion Tests")
    class ConversionTests {

        @ParameterizedTest
        @CsvSource({
                "10.0, 16.0934, 4.4704, 8.689",  // Standard conversion
                "0.0, 0.0, 0.0, 0.0",            // Zero
                "100.0, 160.934, 44.704, 86.89"  // High speed
        })
        @DisplayName("Test conversions")
        void testConversions(double mph, double kph, double mps, double knots) {
            WindSpeed speed = WindSpeed.fromMph(mph);
            assertAll(
                    () -> assertEquals(mph, speed.getMph(), 0.01),
                    () -> assertEquals(kph, speed.getKph(), 0.01),
                    () -> assertEquals(mps, speed.getMps(), 0.01),
                    () -> assertEquals(knots, speed.getKnots(), 0.01)
            );
        }
    }

    @Nested
    @DisplayName("Classification Tests")
    class ClassificationTests {

        @ParameterizedTest
        @ValueSource(doubles = {0.0, 2.5, 4.9})
        @DisplayName("Test calm wind")
        void testCalmWind(double mph) {
            WindSpeed speed = WindSpeed.fromMph(mph);
            assertTrue(speed.isCalm());
            assertFalse(speed.isModerate());
            assertFalse(speed.isStrong());
            assertFalse(speed.isDangerous());
        }

        @ParameterizedTest
        @ValueSource(doubles = {5.0, 10.0, 15.0})
        @DisplayName("Test moderate wind")
        void testModerateWind(double mph) {
            WindSpeed speed = WindSpeed.fromMph(mph);
            assertFalse(speed.isCalm());
            assertTrue(speed.isModerate());
            assertFalse(speed.isStrong());
            assertFalse(speed.isDangerous());
        }

        @ParameterizedTest
        @ValueSource(doubles = {15.1, 20.0, 30.0})
        @DisplayName("Test strong wind")
        void testStrongWind(double mph) {
            WindSpeed speed = WindSpeed.fromMph(mph);
            assertFalse(speed.isCalm());
            assertFalse(speed.isModerate());
            assertTrue(speed.isStrong());
            assertFalse(speed.isDangerous());
        }

        @ParameterizedTest
        @ValueSource(doubles = {30.1, 40.0, 50.0})
        @DisplayName("Test dangerous wind")
        void testDangerousWind(double mph) {
            WindSpeed speed = WindSpeed.fromMph(mph);
            assertFalse(speed.isCalm());
            assertFalse(speed.isModerate());
            assertFalse(speed.isStrong());
            assertTrue(speed.isDangerous());
        }
    }

    @Nested
    @DisplayName("Beaufort Scale Tests")
    class BeaufortScaleTests {

        @ParameterizedTest
        @CsvSource({
                "0.5, 0",     // Calm
                "2.0, 1",     // Light air
                "5.0, 2",     // Light breeze
                "10.0, 3",    // Gentle breeze
                "15.0, 4",    // Moderate breeze
                "20.0, 5",    // Fresh breeze
                "28.0, 6",    // Strong breeze
                "35.0, 7",    // Near gale
                "42.0, 8",    // Gale
                "50.0, 9",    // Strong gale
                "60.0, 10",   // Storm
                "68.0, 11",   // Violent storm
                "75.0, 12"    // Hurricane
        })
        @DisplayName("Test Beaufort scale")
        void testBeaufortScale(double mph, int expectedScale) {
            WindSpeed speed = WindSpeed.fromMph(mph);
            assertEquals(expectedScale, speed.getBeaufortScale());
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringTests {

        @Test
        @DisplayName("Test toString output")
        void testToString() {
            WindSpeed speed = WindSpeed.fromMph(10.0);
            assertEquals("10.0 mph (16.1 km/h)", speed.toString());
        }

        @Test
        @DisplayName("Test toString with zero")
        void testToStringWithZero() {
            WindSpeed speed = WindSpeed.fromMph(0.0);
            assertEquals("0.0 mph (0.0 km/h)", speed.toString());
        }

        @Test
        @DisplayName("Test toString with high value")
        void testToStringWithHighValue() {
            WindSpeed speed = WindSpeed.fromMph(100.0);
            assertEquals("100.0 mph (160.9 km/h)", speed.toString());
        }
    }
}