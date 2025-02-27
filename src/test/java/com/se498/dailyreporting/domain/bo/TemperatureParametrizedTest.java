package com.se498.dailyreporting.domain.bo;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class TemperatureParametrizedTest {

    @ParameterizedTest
    @ValueSource(doubles = {32.1, 50.0, 68.0, 77.0, 85.0})
    void testModerateTemperatures(double fahrenheit) {
        Temperature temp = Temperature.fromFahrenheit(fahrenheit);
        assertTrue(temp.isModerate(),
                () -> fahrenheit + "°F should be considered moderate");
    }

    @ParameterizedTest
    @ValueSource(doubles = {86.0, 90.0, 95.0, 100.0, 212.0})
    void testHotTemperatures(double fahrenheit) {
        Temperature temp = Temperature.fromFahrenheit(fahrenheit);
        assertTrue(temp.isHot(),
                () -> fahrenheit + "°F should be considered hot");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-40.0, -10.0, 0.0, 20.0, 31.9})
    void testColdTemperatures(double fahrenheit) {
        Temperature temp = Temperature.fromFahrenheit(fahrenheit);
        assertTrue(temp.isCold(),
                () -> fahrenheit + "°F should be considered cold");
    }

    @ParameterizedTest
    @CsvSource({
            "32.0, 0.0",
            "212.0, 100.0",
            "98.6, 37.0",
            "-40.0, -40.0",
            "68.0, 20.0"
    })
    void testTemperatureConversions(double fahrenheit, double celsius) {
        Temperature temp = Temperature.fromFahrenheit(fahrenheit);
        assertEquals(celsius, temp.getCelsius(), 0.1,
                () -> fahrenheit + "°F should convert to " + celsius + "°C");
    }

    @ParameterizedTest
    @CsvSource({
            "Freezing Point, 32.0, false, true, false",
            "Room Temperature, 72.0, false, false, true",
            "Hot Day, 90.0, true, false, false",
            "Boiling Point, 212.0, true, false, false",
            "Winter Day, 20.0, false, true, false"
    })
    void testTemperatureCategories(String description, double fahrenheit,
                                   boolean isHot, boolean isCold, boolean isModerate) {
        Temperature temp = Temperature.fromFahrenheit(fahrenheit);
        assertAll(
                () -> assertEquals(isHot, temp.isHot(),
                        description + " hot check failed"),
                () -> assertEquals(isCold, temp.isCold(),
                        description + " cold check failed"),
                () -> assertEquals(isModerate, temp.isModerate(),
                        description + " moderate check failed")
        );
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/temperature-test-data.csv", numLinesToSkip = 1,
            useHeadersInDisplayName = true)
    void testTemperatureFromCsvFile(double fahrenheit, double celsius) {
        Temperature temp = Temperature.fromFahrenheit(fahrenheit);
        assertEquals(celsius, temp.getCelsius(), 0.01,
                () -> String.format("%.1f°F should convert to %.1f°C", fahrenheit, celsius));

    }

    @ParameterizedTest
    @MethodSource("temperatureProvider")
    void testTemperatureRanges(TemperatureTestData testData) {
        Temperature temp = Temperature.fromFahrenheit(testData.fahrenheit);
        assertAll(
                () -> assertEquals(testData.isHot, temp.isHot()),
                () -> assertEquals(testData.isCold, temp.isCold()),
                () -> assertEquals(testData.isModerate, temp.isModerate())
        );
    }

    static Stream<TemperatureTestData> temperatureProvider() {
        return Stream.of(
                new TemperatureTestData(0.0, false, true, false),
                new TemperatureTestData(75.0, false, false, true),
                new TemperatureTestData(95.0, true, false, false)
        );
    }

    static class TemperatureTestData {
        double fahrenheit;
        boolean isHot;
        boolean isCold;
        boolean isModerate;

        TemperatureTestData(double fahrenheit, boolean isHot,
                            boolean isCold, boolean isModerate) {
            this.fahrenheit = fahrenheit;
            this.isHot = isHot;
            this.isCold = isCold;
            this.isModerate = isModerate;
        }
    }

    @ParameterizedTest
    @EnumSource(TemperatureRange.class)
    void testTemperatureRangeClassification(TemperatureRange range) {
        Temperature temp = Temperature.fromFahrenheit(range.getSampleTemperature());
        assertTrue(range.isInRange(temp),
                () -> temp.getFahrenheit() + "°F should be in range " + range);
    }

    enum TemperatureRange {
        COLD(20.0) {
            boolean isInRange(Temperature t) { return t.isCold(); }
        },
        MODERATE(70.0) {
            boolean isInRange(Temperature t) { return t.isModerate(); }
        },
        HOT(95.0) {
            boolean isInRange(Temperature t) { return t.isHot(); }
        };

        private final double sampleTemperature;

        TemperatureRange(double sampleTemperature) {
            this.sampleTemperature = sampleTemperature;
        }

        double getSampleTemperature() {
            return sampleTemperature;
        }

        abstract boolean isInRange(Temperature t);
    }

    @ParameterizedTest
    @MethodSource("extremeTemperatureProvider")
    void testExtremeTemperatures(double fahrenheit, String expectedCategory) {
        Temperature temp = Temperature.fromFahrenheit(fahrenheit);
        switch (expectedCategory) {
            case "HOT":
                assertTrue(temp.isHot());
                break;
            case "COLD":
                assertTrue(temp.isCold());
                break;
            default:
                assertTrue(temp.isModerate());
        }
    }

    static Stream<Arguments> extremeTemperatureProvider() {
        return Stream.of(
                Arguments.of(-50.0, "COLD"),
                Arguments.of(120.0, "HOT"),
                Arguments.of(Double.MAX_VALUE, "HOT"),
                Arguments.of(Double.MIN_VALUE, "COLD")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"32.0,0.0", "212.0,100.0", "98.6,37.0"})
    void testTemperatureFromString(String input) {
        String[] parts = input.split(",");
        double fahrenheit = Double.parseDouble(parts[0]);
        double celsius = Double.parseDouble(parts[1]);

        Temperature temp = Temperature.fromFahrenheit(fahrenheit);
        assertEquals(celsius, temp.getCelsius(), 0.1);
    }
}