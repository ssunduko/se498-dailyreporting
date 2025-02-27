package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates different test execution orders using @TestMethodOrder
 */
class TemperatureOrderTest {

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Tests ordered by @Order annotation")
    class OrderAnnotationTest {

        private Temperature temp;

        @Test
        @Order(1)
        @DisplayName("1. Create temperature")
        void createTemperature() {
            temp = Temperature.fromFahrenheit(72.0);
            assertNotNull(temp);
        }

        @Test
        @Order(2)
        @DisplayName("2. Check Fahrenheit value")
        void checkFahrenheit() {
            temp = Temperature.fromFahrenheit(72.0);
            assertEquals(72.0, temp.getFahrenheit());
        }

        @Test
        @Order(3)
        @DisplayName("3. Check Celsius conversion")
        void checkCelsius() {
            temp = Temperature.fromFahrenheit(72.0);
            assertEquals(22.22, temp.getCelsius(), 0.01);
        }

        @Test
        @Order(4)
        @DisplayName("4. Verify temperature classification")
        void checkClassification() {
            temp = Temperature.fromFahrenheit(72.0);
            assertTrue(temp.isModerate());
        }

        @Test
        @Order(5)
        @DisplayName("5. Check string representation")
        void checkToString() {
            temp = Temperature.fromFahrenheit(72.0);
            assertEquals("72.0°F (22.2°C)", temp.toString());
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    @DisplayName("Tests ordered by display name")
    class DisplayNameOrderTest {

        @Test
        @DisplayName("1. Boiling point test")
        void testBoilingPoint() {
            Temperature temp = Temperature.fromFahrenheit(212.0);
            assertTrue(temp.isHot());
        }

        @Test
        @DisplayName("2. Freezing point test")
        void testFreezingPoint() {
            Temperature temp = Temperature.fromFahrenheit(32.0);
            assertTrue(temp.isCold());
        }

        @Test
        @DisplayName("3. Room temperature test")
        void testRoomTemperature() {
            Temperature temp = Temperature.fromFahrenheit(72.0);
            assertTrue(temp.isModerate());
        }

        @Test
        @DisplayName("4. Zero Celsius test")
        void testZeroCelsius() {
            Temperature temp = Temperature.fromCelsius(0.0);
            assertEquals(32.0, temp.getFahrenheit());
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.MethodName.class)
    @DisplayName("Tests ordered by method name")
    class MethodNameOrderTest {

        @Test
        void test1_CreateFromCelsius() {
            Temperature temp = Temperature.fromCelsius(100.0);
            assertEquals(212.0, temp.getFahrenheit(), 0.01);
        }

        @Test
        void test2_CreateFromFahrenheit() {
            Temperature temp = Temperature.fromFahrenheit(32.0);
            assertEquals(0.0, temp.getCelsius(), 0.01);
        }

        @Test
        void test3_VerifyHotTemperature() {
            Temperature temp = Temperature.fromFahrenheit(90.0);
            assertTrue(temp.isHot());
        }

        @Test
        void test4_VerifyColdTemperature() {
            Temperature temp = Temperature.fromFahrenheit(20.0);
            assertTrue(temp.isCold());
        }

        @Test
        void test5_VerifyModerateTemperature() {
            Temperature temp = Temperature.fromFahrenheit(70.0);
            assertTrue(temp.isModerate());
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.Random.class)
    @DisplayName("Tests in random order")
    class RandomOrderTest {

        @Test
        void testExtremeHeat() {
            Temperature temp = Temperature.fromFahrenheit(120.0);
            assertTrue(temp.isHot());
        }

        @Test
        void testExtremeCold() {
            Temperature temp = Temperature.fromFahrenheit(-40.0);
            assertTrue(temp.isCold());
        }

        @Test
        void testModerateRange() {
            Temperature temp = Temperature.fromFahrenheit(75.0);
            assertTrue(temp.isModerate());
        }

        @RepeatedTest(3)
        void testConversion() {
            Temperature temp = Temperature.fromFahrenheit(98.6);
            assertEquals(37.0, temp.getCelsius(), 0.01);
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Tests with mixed priorities")
    class MixedPriorityTest {

        @Test
        @Order(Ordering.HIGH)
        void highPriorityTest() {
            Temperature temp = Temperature.fromFahrenheit(212.0);
            assertTrue(temp.isHot());
        }

        @Test
        @Order(Ordering.MEDIUM)
        void mediumPriorityTest() {
            Temperature temp = Temperature.fromFahrenheit(72.0);
            assertTrue(temp.isModerate());
        }

        @Test
        @Order(Ordering.LOW)
        void lowPriorityTest() {
            Temperature temp = Temperature.fromFahrenheit(32.0);
            assertTrue(temp.isCold());
        }

        private static class Ordering {
            static final int HIGH = 1;
            static final int MEDIUM = 2;
            static final int LOW = 3;
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Tests with dependencies")
    class DependencyOrderTest {

        private static Temperature sharedTemp;

        @Test
        @Order(1)
        void step1_CreateTemperature() {
            sharedTemp = Temperature.fromFahrenheit(72.0);
            assertNotNull(sharedTemp);
        }

        @Test
        @Order(2)
        void step2_VerifyFahrenheit() {
            assertEquals(72.0, sharedTemp.getFahrenheit());
        }

        @Test
        @Order(3)
        void step3_VerifyCelsius() {
            assertEquals(22.22, sharedTemp.getCelsius(), 0.01);
        }

        @Test
        @Order(4)
        void step4_VerifyClassification() {
            assertTrue(sharedTemp.isModerate());
        }
    }
}