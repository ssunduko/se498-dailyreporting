package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Humidity Tests")
class HumidityTest {

    @Nested
    @DisplayName("Humidity Creation")
    class CreationTests {

        @Test
        @DisplayName("Create valid humidity")
        void testValidHumidity() {
            Humidity humidity = Humidity.of(50);
            assertEquals(50, humidity.getPercentage());
        }

        @Test
        @DisplayName("Handle invalid input")
        void testInvalidInput() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> Humidity.of(null)),
                    () -> assertThrows(IllegalArgumentException.class, () -> Humidity.of(-1)),
                    () -> assertThrows(IllegalArgumentException.class, () -> Humidity.of(101))
            );
        }
    }

    @Nested
    @DisplayName("Humidity Classification")
    class ClassificationTests {

        @Test
        @DisplayName("High humidity checks")
        void testHighHumidity() {
            Humidity high = Humidity.of(85);
            assertTrue(high.isHigh());
            assertFalse(high.isLow());
            assertFalse(high.isComfortable());
        }

        @Test
        @DisplayName("Low humidity checks")
        void testLowHumidity() {
            Humidity low = Humidity.of(15);
            assertTrue(low.isLow());
            assertFalse(low.isHigh());
            assertFalse(low.isComfortable());
        }

        @Test
        @DisplayName("Comfortable humidity checks")
        void testComfortableHumidity() {
            Humidity comfortable = Humidity.of(45);
            assertTrue(comfortable.isComfortable());
            assertFalse(comfortable.isHigh());
            assertFalse(comfortable.isLow());
        }
    }

    @Nested
    @DisplayName("Comfort Categories")
    class ComfortCategoryTests {

        @Test
        @DisplayName("Test all comfort categories")
        void testComfortCategories() {
            assertAll(
                    () -> assertEquals(Humidity.HumidityComfort.TOO_DRY,
                            Humidity.of(10).getComfortCategory()),
                    () -> assertEquals(Humidity.HumidityComfort.DRY,
                            Humidity.of(25).getComfortCategory()),
                    () -> assertEquals(Humidity.HumidityComfort.COMFORTABLE,
                            Humidity.of(45).getComfortCategory()),
                    () -> assertEquals(Humidity.HumidityComfort.HUMID,
                            Humidity.of(70).getComfortCategory()),
                    () -> assertEquals(Humidity.HumidityComfort.TOO_HUMID,
                            Humidity.of(90).getComfortCategory())
            );
        }
    }
}