package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@DisplayName("Temperature Split Contract Tests")
class TemperatureSplitContractTest {

    private static class TestData {
        final double fahrenheit;
        final double expectedCelsius;
        final Temperature temperature;

        TestData(double fahrenheit, double expectedCelsius) {
            this.fahrenheit = fahrenheit;
            this.expectedCelsius = expectedCelsius;
            this.temperature = null;
        }

        TestData(double fahrenheit, double expectedCelsius, Temperature temperature) {
            this.fahrenheit = fahrenheit;
            this.expectedCelsius = expectedCelsius;
            this.temperature = temperature;
        }
    }

    @Nested
    @DisplayName("Temperature Conversion Contract")
    class TemperatureConversionContract {

        private TestData testData;

        @BeforeEach
        void setUp() {
            testData = new TestData(72.0, 22.22);
        }

        @Test
        @DisplayName("Test temperature conversion contract")
        void testConversionContract() {
            assumeValidInput();
            Temperature temp = Temperature.fromFahrenheit(testData.fahrenheit);
            testData = new TestData(testData.fahrenheit, testData.expectedCelsius, temp);
            verifyConversion();
        }

        private void assumeValidInput() {
            assumeTrue(testData.fahrenheit != Double.NEGATIVE_INFINITY,
                    "Fahrenheit should not be negative infinity");
            assumeTrue(testData.fahrenheit != Double.POSITIVE_INFINITY,
                    "Fahrenheit should not be positive infinity");
            assumeTrue(!Double.isNaN(testData.fahrenheit),
                    "Fahrenheit should not be NaN");
            assumeTrue(testData.fahrenheit >= -459.67,
                    "Fahrenheit should not be below absolute zero");
        }

        private void verifyConversion() {
            assertNotNull(testData.temperature, "Temperature object should be created");
            assertEquals(testData.expectedCelsius, testData.temperature.getCelsius(), 0.01,
                    "Celsius conversion should be accurate");
        }
    }

    @Nested
    @DisplayName("Temperature Classification Contract")
    class TemperatureClassificationContract {

        private TestData hotTemp;
        private TestData coldTemp;
        private TestData moderateTemp;

        @BeforeEach
        void setUp() {
            hotTemp = new TestData(90.0, 32.22);
            coldTemp = new TestData(20.0, -6.67);
            moderateTemp = new TestData(70.0, 21.11);
        }

        @Test
        @DisplayName("Test hot temperature classification")
        void testHotClassification() {
            assumeValidHotTemp();
            Temperature temp = Temperature.fromFahrenheit(hotTemp.fahrenheit);
            hotTemp = new TestData(hotTemp.fahrenheit, hotTemp.expectedCelsius, temp);
            verifyHotClassification();
        }

        private void assumeValidHotTemp() {
            assumeTrue(hotTemp.fahrenheit > 85.0,
                    "Temperature should be above hot threshold");
        }

        private void verifyHotClassification() {
            assertTrue(hotTemp.temperature.isHot(),
                    "Temperature should be classified as hot");
            assertFalse(hotTemp.temperature.isCold(),
                    "Hot temperature should not be cold");
            assertFalse(hotTemp.temperature.isModerate(),
                    "Hot temperature should not be moderate");
        }

        @Test
        @DisplayName("Test cold temperature classification")
        void testColdClassification() {
            assumeValidColdTemp();
            Temperature temp = Temperature.fromFahrenheit(coldTemp.fahrenheit);
            coldTemp = new TestData(coldTemp.fahrenheit, coldTemp.expectedCelsius, temp);
            verifyColdClassification();
        }

        private void assumeValidColdTemp() {
            assumeTrue(coldTemp.fahrenheit < 32.0,
                    "Temperature should be below cold threshold");
        }

        private void verifyColdClassification() {
            assertTrue(coldTemp.temperature.isCold(),
                    "Temperature should be classified as cold");
            assertFalse(coldTemp.temperature.isHot(),
                    "Cold temperature should not be hot");
            assertFalse(coldTemp.temperature.isModerate(),
                    "Cold temperature should not be moderate");
        }
    }

    @Nested
    @DisplayName("Temperature Boundary Contract")
    class TemperatureBoundaryContract {

        private TestData boundaryTemp;

        @BeforeEach
        void setUp() {
            boundaryTemp = new TestData(32.0, 0.0);
        }

        @Test
        @DisplayName("Test freezing point boundary")
        void testFreezingPointBoundary() {
            assumeValidBoundary();
            Temperature temp = Temperature.fromFahrenheit(boundaryTemp.fahrenheit);
            boundaryTemp = new TestData(boundaryTemp.fahrenheit, boundaryTemp.expectedCelsius, temp);
            verifyBoundaryBehavior();
        }

        private void assumeValidBoundary() {
            assumeTrue(boundaryTemp.fahrenheit == 32.0,
                    "Temperature should be at freezing point");
        }

        private void verifyBoundaryBehavior() {
            assertTrue(boundaryTemp.temperature.isCold(),
                    "Freezing point should be cold");
            assertFalse(boundaryTemp.temperature.isModerate(),
                    "Freezing point should not be moderate");
            assertEquals(0.0, boundaryTemp.temperature.getCelsius(), 0.01,
                    "Freezing point should be 0°C");
        }
    }

    @Nested
    @DisplayName("Temperature String Representation Contract")
    class TemperatureStringContract {

        private TestData stringTemp;

        @BeforeEach
        void setUp() {
            stringTemp = new TestData(72.0, 22.22);
        }

        @Test
        @DisplayName("Test string representation")
        void testStringRepresentation() {
            assumeValidStringInput();
            Temperature temp = Temperature.fromFahrenheit(stringTemp.fahrenheit);
            stringTemp = new TestData(stringTemp.fahrenheit, stringTemp.expectedCelsius, temp);
            verifyStringFormat();
        }

        private void assumeValidStringInput() {
            assumeTrue(!Double.isNaN(stringTemp.fahrenheit),
                    "Temperature should be a valid number");
            assumeTrue(Double.isFinite(stringTemp.fahrenheit),
                    "Temperature should be finite");
        }

        private void verifyStringFormat() {
            String result = stringTemp.temperature.toString();
            assertTrue(result.contains("°F"),
                    "String should contain Fahrenheit symbol");
            assertTrue(result.contains("°C"),
                    "String should contain Celsius symbol");
            assertTrue(result.contains(String.format("%.1f", stringTemp.fahrenheit)),
                    "String should contain Fahrenheit value");
            assertTrue(result.contains(String.format("%.1f", stringTemp.expectedCelsius)),
                    "String should contain Celsius value");
        }
    }
}