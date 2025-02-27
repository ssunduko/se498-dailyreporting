package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Humidity;
import com.se498.dailyreporting.domain.bo.Temperature;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Weather Comfort Tests")
class WeatherComfortTest {

    @Nested
    @DisplayName("Combined Comfort Tests")
    class CombinedComfortTests {

        @Test
        @DisplayName("Ideal conditions")
        void testIdealConditions() {
            Temperature temp = Temperature.fromFahrenheit(72.0);
            Humidity humidity = Humidity.of(45);

            assertTrue(temp.isModerate());
            assertTrue(humidity.isComfortable());
        }

        @Test
        @DisplayName("Uncomfortable hot and humid")
        void testHotAndHumid() {
            Temperature temp = Temperature.fromFahrenheit(90.0);
            Humidity humidity = Humidity.of(85);

            assertTrue(temp.isHot());
            assertTrue(humidity.isHigh());
            assertEquals(Humidity.HumidityComfort.TOO_HUMID, humidity.getComfortCategory());
        }

        @Test
        @DisplayName("Uncomfortable cold and dry")
        void testColdAndDry() {
            Temperature temp = Temperature.fromFahrenheit(20.0);
            Humidity humidity = Humidity.of(15);

            assertTrue(temp.isCold());
            assertTrue(humidity.isLow());
            assertEquals(Humidity.HumidityComfort.TOO_DRY, humidity.getComfortCategory());
        }
    }

    @Nested
    @DisplayName("Temperature Range Tests")
    class TemperatureRangeTests {

        @Test
        @DisplayName("Test all temperature ranges")
        void testTemperatureRanges() {
            assertAll(
                    () -> assertTrue(Temperature.fromFahrenheit(90.0).isHot()),
                    () -> assertTrue(Temperature.fromFahrenheit(72.0).isModerate()),
                    () -> assertTrue(Temperature.fromFahrenheit(20.0).isCold())
            );
        }
    }

    @Nested
    @DisplayName("Humidity Range Tests")
    class HumidityRangeTests {

        @Test
        @DisplayName("Test all humidity ranges")
        void testHumidityRanges() {
            assertAll(
                    () -> assertTrue(Humidity.of(85).isHigh()),
                    () -> assertTrue(Humidity.of(45).isComfortable()),
                    () -> assertTrue(Humidity.of(15).isLow())
            );
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Test temperature string format")
        void testTemperatureString() {
            Temperature temp = Temperature.fromFahrenheit(72.0);
            assertEquals("72.0°F (22.2°C)", temp.toString());
        }

        @Test
        @DisplayName("Test humidity string format")
        void testHumidityString() {
            Humidity humidity = Humidity.of(45);
            assertEquals("45%", humidity.toString());
        }
    }

    @Nested
    @DisplayName("Boundary Tests")
    class BoundaryTests {

        @Test
        @DisplayName("Temperature boundaries")
        void testTemperatureBoundaries() {
            assertAll(
                    () -> assertFalse(Temperature.fromFahrenheit(85.0).isHot()),
                    () -> assertTrue(Temperature.fromFahrenheit(85.1).isHot()),
                    () -> assertFalse(Temperature.fromFahrenheit(32.1).isCold()),
                    () -> assertTrue(Temperature.fromFahrenheit(31.9).isCold())
            );
        }

        @Test
        @DisplayName("Humidity boundaries")
        void testHumidityBoundaries() {
            assertAll(
                    () -> assertEquals(Humidity.HumidityComfort.TOO_DRY,
                            Humidity.of(19).getComfortCategory()),
                    () -> assertEquals(Humidity.HumidityComfort.DRY,
                            Humidity.of(20).getComfortCategory()),
                    () -> assertEquals(Humidity.HumidityComfort.COMFORTABLE,
                            Humidity.of(30).getComfortCategory()),
                    () -> assertEquals(Humidity.HumidityComfort.HUMID,
                            Humidity.of(61).getComfortCategory()),
                    () -> assertEquals(Humidity.HumidityComfort.TOO_HUMID,
                            Humidity.of(81).getComfortCategory())
            );
        }
    }
}