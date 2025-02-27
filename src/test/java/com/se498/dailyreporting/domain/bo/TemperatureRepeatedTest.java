package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Temperature Repeated Tests")
class TemperatureRepeatedTest {

    @RepeatedTest(5)
    @DisplayName("Test room temperature conversion")
    void testRoomTemperature(RepetitionInfo repetitionInfo) {
        // Given
        double roomTemp = 72.0;

        // When
        Temperature temp = Temperature.fromFahrenheit(roomTemp);

        // Then
        assertEquals(22.22, temp.getCelsius(), 0.01,
                String.format("Room temperature conversion failed on repetition %d",
                        repetitionInfo.getCurrentRepetition()));
    }

    @RepeatedTest(value = 3, name = "Testing freezing point {currentRepetition}/{totalRepetitions}")
    void testFreezingPoint(TestInfo testInfo, RepetitionInfo repetitionInfo) {
        // Given
        Temperature temp = Temperature.fromFahrenheit(32.0);

        // Then
        assertEquals(0.0, temp.getCelsius(), 0.01,
                String.format("Freezing point test failed on repetition %d of %d",
                        repetitionInfo.getCurrentRepetition(),
                        repetitionInfo.getTotalRepetitions()));
    }

    @RepeatedTest(value = 4, name = RepeatedTest.LONG_DISPLAY_NAME)
    @DisplayName("Boiling point test")
    void testBoilingPoint() {
        // Given
        Temperature temp = Temperature.fromFahrenheit(212.0);

        // Then
        assertEquals(100.0, temp.getCelsius(), 0.01,
                "Boiling point conversion should be accurate");
    }

    @RepeatedTest(value = 3, name = RepeatedTest.SHORT_DISPLAY_NAME)
    void testBodyTemperature() {
        // Given
        Temperature temp = Temperature.fromFahrenheit(98.6);

        // Then
        assertEquals(37.0, temp.getCelsius(), 0.01,
                "Body temperature conversion should be accurate");
    }

    @RepeatedTest(value = 10)
    void testRandomTemperatures(RepetitionInfo repetitionInfo) {
        // Generate random temperature between -50 and 120
        double fahrenheit = -50 + (Math.random() * 170);
        Temperature temp = Temperature.fromFahrenheit(fahrenheit);

        // Verify conversion
        double expectedCelsius = (fahrenheit - 32) * 5 / 9;
        assertEquals(expectedCelsius, temp.getCelsius(), 0.01,
                String.format("Random temperature conversion failed for %.2f°F on repetition %d",
                        fahrenheit, repetitionInfo.getCurrentRepetition()));
    }

    @RepeatedTest(value = 5)
    void testTemperatureClassification(RepetitionInfo repetitionInfo) {
        // Create temperatures based on repetition number
        double fahrenheit = switch(repetitionInfo.getCurrentRepetition()) {
            case 1 -> 20.0;  // Cold
            case 2 -> 32.1;  // Moderate (boundary)
            case 3 -> 72.0;  // Moderate
            case 4 -> 85.0;  // Moderate (boundary)
            case 5 -> 90.0;  // Hot
            default -> 72.0;
        };

        Temperature temp = Temperature.fromFahrenheit(fahrenheit);

        if (fahrenheit < 32.0) {
            assertTrue(temp.isCold(), "Temperature should be cold: " + fahrenheit + "°F");
        } else if (fahrenheit > 85.0) {
            assertTrue(temp.isHot(), "Temperature should be hot: " + fahrenheit + "°F");
        } else {
            assertTrue(temp.isModerate(), "Temperature should be moderate: " + fahrenheit + "°F");
        }
    }

    @RepeatedTest(value = 3)
    void testToString(TestInfo testInfo, RepetitionInfo repetitionInfo) {
        // Test different temperatures based on repetition
        double fahrenheit = switch(repetitionInfo.getCurrentRepetition()) {
            case 1 -> 32.0;
            case 2 -> 72.0;
            case 3 -> 212.0;
            default -> 72.0;
        };

        Temperature temp = Temperature.fromFahrenheit(fahrenheit);
        double celsius = (fahrenheit - 32) * 5 / 9;
        String expected = String.format("%.1f°F (%.1f°C)", fahrenheit, celsius);

        assertEquals(expected, temp.toString(),
                String.format("String representation failed for %.1f°F on repetition %d",
                        fahrenheit, repetitionInfo.getCurrentRepetition()));
    }

    @RepeatedTest(value = 5)
    void testTemperatureRanges(RepetitionInfo repetitionInfo) {
        // Test different ranges in each repetition
        int rangeIndex = repetitionInfo.getCurrentRepetition();
        double[] ranges = {20.0, 50.0, 72.0, 85.0, 90.0};
        boolean[] expectedHot = {false, false, false, false, true};
        boolean[] expectedCold = {true, false, false, false, false};
        boolean[] expectedModerate = {false, true, true, true, false};

        Temperature temp = Temperature.fromFahrenheit(ranges[rangeIndex - 1]);

        assertAll(
                () -> assertEquals(expectedHot[rangeIndex - 1], temp.isHot(),
                        String.format("Hot classification failed for %.1f°F", ranges[rangeIndex - 1])),
                () -> assertEquals(expectedCold[rangeIndex - 1], temp.isCold(),
                        String.format("Cold classification failed for %.1f°F", ranges[rangeIndex - 1])),
                () -> assertEquals(expectedModerate[rangeIndex - 1], temp.isModerate(),
                        String.format("Moderate classification failed for %.1f°F", ranges[rangeIndex - 1]))
        );
    }

    @RepeatedTest(value = 2)
    void testBoundaryValues(RepetitionInfo repetitionInfo) {
        // Test boundary values with small variations
        double[] boundaryValues = {31.9, 32.1}; // Cold boundary
        Temperature temp = Temperature.fromFahrenheit(boundaryValues[repetitionInfo.getCurrentRepetition() - 1]);

        if (repetitionInfo.getCurrentRepetition() == 1) {
            assertTrue(temp.isCold(), "Temperature below 32°F should be cold");
        } else {
            assertFalse(temp.isCold(), "Temperature at or above 32°F should not be cold");
        }
    }
}
