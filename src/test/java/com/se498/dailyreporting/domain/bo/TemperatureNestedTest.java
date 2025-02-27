package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Temperature Test Suite")
class TemperatureNestedTest {

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Create from Fahrenheit")
        void testFromFahrenheit() {
            Temperature temp = Temperature.fromFahrenheit(72.0);
            assertEquals(72.0, temp.getFahrenheit());
            assertEquals(22.22, temp.getCelsius(), 0.01);
        }

        @Test
        @DisplayName("Create from Celsius")
        void testFromCelsius() {
            Temperature temp = Temperature.fromCelsius(25.0);
            assertEquals(77.0, temp.getFahrenheit(), 0.01);
            assertEquals(25.0, temp.getCelsius());
        }

        @Test
        @DisplayName("Handle null Fahrenheit")
        void testFromFahrenheitNull() {
            assertThrows(NullPointerException.class, () ->
                    Temperature.fromFahrenheit(null));
        }

        @Test
        @DisplayName("Handle null Celsius")
        void testFromCelsiusNull() {
            assertThrows(NullPointerException.class, () ->
                    Temperature.fromCelsius(null));
        }
    }

    @Nested
    @DisplayName("Temperature Classification Tests")
    class TemperatureClassificationTests {

        @Nested
        @DisplayName("Hot Temperature Tests")
        class HotTemperatureTests {

            @Test
            @DisplayName("Above hot threshold")
            void testIsHotAboveThreshold() {
                Temperature temp = Temperature.fromFahrenheit(86.0);
                assertTrue(temp.isHot());
            }

            @Test
            @DisplayName("At hot threshold")
            void testIsHotAtThreshold() {
                Temperature temp = Temperature.fromFahrenheit(85.0);
                assertFalse(temp.isHot());
            }

            @Test
            @DisplayName("Below hot threshold")
            void testIsHotBelowThreshold() {
                Temperature temp = Temperature.fromFahrenheit(84.9);
                assertFalse(temp.isHot());
            }
        }

        @Nested
        @DisplayName("Cold Temperature Tests")
        class ColdTemperatureTests {

            @Test
            @DisplayName("Below cold threshold")
            void testIsColdBelowThreshold() {
                Temperature temp = Temperature.fromFahrenheit(31.9);
                assertTrue(temp.isCold());
            }

            @Test
            @DisplayName("At cold threshold")
            void testIsColdAtThreshold() {
                Temperature temp = Temperature.fromFahrenheit(32.1);
                assertFalse(temp.isCold());
            }

            @Test
            @DisplayName("Above cold threshold")
            void testIsColdAboveThreshold() {
                Temperature temp = Temperature.fromFahrenheit(32.1);
                assertFalse(temp.isCold());
            }
        }

        @Nested
        @DisplayName("Moderate Temperature Tests")
        class ModerateTemperatureTests {

            @Test
            @DisplayName("Between thresholds")
            void testIsModerateInRange() {
                Temperature temp = Temperature.fromFahrenheit(70.0);
                assertTrue(temp.isModerate());
            }

            @Test
            @DisplayName("At cold boundary")
            void testIsModerateAtColdBoundary() {
                Temperature temp = Temperature.fromFahrenheit(32.1);
                assertTrue(temp.isModerate());
            }

            @Test
            @DisplayName("At hot boundary")
            void testIsModerateAtHotBoundary() {
                Temperature temp = Temperature.fromFahrenheit(85.0);
                assertTrue(temp.isModerate());
            }
        }
    }

    @Nested
    @DisplayName("Temperature Conversion Tests")
    class TemperatureConversionTests {

        @Nested
        @DisplayName("Standard Temperature Points")
        class StandardTemperaturePoints {

            @Test
            @DisplayName("Freezing point")
            void testFreezingPoint() {
                Temperature temp = Temperature.fromFahrenheit(32.0);
                assertEquals(0.0, temp.getCelsius(), 0.01);
            }

            @Test
            @DisplayName("Boiling point")
            void testBoilingPoint() {
                Temperature temp = Temperature.fromFahrenheit(212.0);
                assertEquals(100.0, temp.getCelsius(), 0.01);
            }

            @Test
            @DisplayName("Body temperature")
            void testBodyTemperature() {
                Temperature temp = Temperature.fromFahrenheit(98.6);
                assertEquals(37.0, temp.getCelsius(), 0.01);
            }
        }

        @Nested
        @DisplayName("Extreme Temperature Points")
        class ExtremeTemperaturePoints {

            @Test
            @DisplayName("Very hot temperature")
            void testVeryHot() {
                Temperature temp = Temperature.fromFahrenheit(1000.0);
                assertTrue(temp.isHot());
            }

            @Test
            @DisplayName("Very cold temperature")
            void testVeryCold() {
                Temperature temp = Temperature.fromFahrenheit(-100.0);
                assertTrue(temp.isCold());
            }

            @Test
            @DisplayName("Temperature where F equals C")
            void testFahrenheitEqualsCelsius() {
                Temperature temp = Temperature.fromFahrenheit(-40.0);
                assertEquals(temp.getFahrenheit(), temp.getCelsius(), 0.01);
            }
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Format positive temperatures")
        void testToStringPositive() {
            Temperature temp = Temperature.fromFahrenheit(72.0);
            assertEquals("72.0°F (22.2°C)", temp.toString());
        }

        @Test
        @DisplayName("Format negative temperatures")
        void testToStringNegative() {
            Temperature temp = Temperature.fromFahrenheit(-40.0);
            assertEquals("-40.0°F (-40.0°C)", temp.toString());
        }

        @Test
        @DisplayName("Format zero temperature")
        void testToStringZero() {
            Temperature temp = Temperature.fromCelsius(0.0);
            assertEquals("32.0°F (0.0°C)", temp.toString());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Handle maximum double value")
        void testMaxDoubleValue() {
            assertDoesNotThrow(() ->
                    Temperature.fromFahrenheit(Double.MAX_VALUE));
        }

        @Test
        @DisplayName("Handle minimum double value")
        void testMinDoubleValue() {
            assertDoesNotThrow(() ->
                    Temperature.fromFahrenheit(Double.MIN_VALUE));
        }

        @Test
        @DisplayName("Handle positive infinity")
        void testPositiveInfinity() {
            assertDoesNotThrow(() ->
                    Temperature.fromFahrenheit(Double.POSITIVE_INFINITY));
        }

        @Test
        @DisplayName("Handle negative infinity")
        void testNegativeInfinity() {
            assertDoesNotThrow(() ->
                    Temperature.fromFahrenheit(Double.NEGATIVE_INFINITY));
        }

        @Test
        @DisplayName("Handle NaN")
        void testNaN() {
            assertDoesNotThrow(() ->
                    Temperature.fromFahrenheit(Double.NaN));
        }
    }
}