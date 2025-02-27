package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DisplayName;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

@DisplayName("Temperature Dynamic Tests")
class TemperatureDynamicTest {

    @TestFactory
    @DisplayName("Dynamic tests for temperature conversion")
    Collection<DynamicTest> dynamicTestsFromCollection() {
        return Arrays.asList(
                dynamicTest("Test freezing point", () -> {
                    Temperature temp = Temperature.fromFahrenheit(32.0);
                    assertEquals(0.0, temp.getCelsius(), 0.01);
                }),
                dynamicTest("Test boiling point", () -> {
                    Temperature temp = Temperature.fromFahrenheit(212.0);
                    assertEquals(100.0, temp.getCelsius(), 0.01);
                }),
                dynamicTest("Test room temperature", () -> {
                    Temperature temp = Temperature.fromFahrenheit(72.0);
                    assertEquals(22.22, temp.getCelsius(), 0.01);
                })
        );
    }

    @TestFactory
    @DisplayName("Dynamic tests for temperature ranges")
    Stream<DynamicTest> dynamicTestsFromStream() {
        return Stream.of(15, 32, 50, 75, 85, 90, 100)
                .map(temp -> dynamicTest("Testing temperature " + temp + "°F", () -> {
                    Temperature temperature = Temperature.fromFahrenheit(temp.doubleValue());
                    if (temp <= 32) assertTrue(temperature.isCold());
                    else if (temp > 85) assertTrue(temperature.isHot());
                    else assertTrue(temperature.isModerate());
                }));
    }

    @TestFactory
    @DisplayName("Dynamic tests with custom input")
    Stream<DynamicTest> dynamicTestsWithCustomInput() {
        // Input data: fahrenheit, celsius
        double[][] temperatures = {
                {32.0, 0.0},
                {212.0, 100.0},
                {98.6, 37.0},
                {-40.0, -40.0}
        };

        return Stream.of(temperatures)
                .map(data -> {
                    String testName = String.format("Convert %.1f°F to %.1f°C", data[0], data[1]);
                    return dynamicTest(testName, () -> {
                        Temperature temp = Temperature.fromFahrenheit(data[0]);
                        assertEquals(data[1], temp.getCelsius(), 0.1);
                    });
                });
    }

    @TestFactory
    @DisplayName("Nested dynamic tests for temperature classifications")
    Stream<DynamicContainer> dynamicContainerTests() {
        return Stream.of(
                dynamicContainer("Cold temperatures",
                        IntStream.range(-40, 32)
                                .mapToObj(temp -> dynamicTest(
                                        temp + "°F is cold",
                                        () -> assertTrue(Temperature.fromFahrenheit((double) temp).isCold())
                                ))
                ),
                dynamicContainer("Moderate temperatures",
                        IntStream.range(33, 86)
                                .mapToObj(temp -> dynamicTest(
                                        temp + "°F is moderate",
                                        () -> assertTrue(Temperature.fromFahrenheit((double) temp).isModerate())
                                ))
                ),
                dynamicContainer("Hot temperatures",
                        IntStream.range(86, 120)
                                .mapToObj(temp -> dynamicTest(
                                        temp + "°F is hot",
                                        () -> assertTrue(Temperature.fromFahrenheit((double) temp).isHot())
                                ))
                )
        );
    }

    @TestFactory
    @DisplayName("Dynamic tests with generated test data")
    Stream<DynamicTest> dynamicTestsWithGeneratedData() {
        return Stream.iterate(0, n -> n + 10)
                .limit(25)
                .map(f -> dynamicTest(
                        String.format("Temperature at %d°F", f),
                        () -> {
                            Temperature temp = Temperature.fromFahrenheit(f.doubleValue());
                            assertNotNull(temp);
                            assertEquals(f, temp.getFahrenheit().intValue());
                        }
                ));
    }

    @TestFactory
    @DisplayName("Dynamic tests for string representation")
    Iterator<DynamicTest> dynamicTestsFromIterator() {
        List<Temperature> temperatures = Arrays.asList(
                Temperature.fromFahrenheit(32.0),
                Temperature.fromFahrenheit(212.0),
                Temperature.fromFahrenheit(98.6)
        );

        return temperatures.stream()
                .map(temp -> dynamicTest(
                        String.format("String format for %.1f°F", temp.getFahrenheit()),
                        () -> {
                            String expected = String.format("%.1f°F (%.1f°C)",
                                    temp.getFahrenheit(), temp.getCelsius());
                            assertEquals(expected, temp.toString());
                        }
                ))
                .iterator();
    }

    @TestFactory
    @DisplayName("Dynamic tests with multiple assertions")
    List<DynamicTest> dynamicTestsWithMultipleAssertions() {
        record TestData(double fahrenheit, double celsius, boolean isHot, boolean isCold) {}

        List<TestData> testData = Arrays.asList(
                new TestData(32.0, 0.0, false, true),
                new TestData(72.0, 22.22, false, false),
                new TestData(95.0, 35.0, true, false)
        );

        return testData.stream()
                .map(data -> dynamicTest(
                        String.format("Complete test for %.1f°F", data.fahrenheit),
                        () -> {
                            Temperature temp = Temperature.fromFahrenheit(data.fahrenheit);
                            assertAll(
                                    () -> assertEquals(data.fahrenheit, temp.getFahrenheit(), 0.01),
                                    () -> assertEquals(data.celsius, temp.getCelsius(), 0.01),
                                    () -> assertEquals(data.isHot, temp.isHot()),
                                    () -> assertEquals(data.isCold, temp.isCold())
                            );
                        }
                ))
                .toList();
    }

    @TestFactory
    @DisplayName("Dynamic tests with error cases")
    Stream<DynamicTest> dynamicTestsWithErrorCases() {
        return Stream.of(
                dynamicTest("Test null Fahrenheit input", () ->
                        assertThrows(NullPointerException.class, () ->
                                Temperature.fromFahrenheit(null))),
                dynamicTest("Test null Celsius input", () ->
                        assertThrows(NullPointerException.class, () ->
                                Temperature.fromCelsius(null)))
        );
    }

    @TestFactory
    @DisplayName("Dynamic tests with test lifecycle")
    Stream<DynamicTest> dynamicTestsWithLifecycle() {
        List<Temperature> testTemperatures = new ArrayList<>();

        ThrowingConsumer<Temperature> setUp = temp ->
                testTemperatures.add(temp);

        ThrowingConsumer<Temperature> tearDown = temp ->
                testTemperatures.remove(temp);

        return Stream.of(32.0, 72.0, 98.6)
                .map(f -> {
                    Temperature temp = Temperature.fromFahrenheit(f);
                    return dynamicTest(
                            String.format("Lifecycle test for %.1f°F", f),
                            () -> {
                                setUp.accept(temp);
                                try {
                                    assertNotNull(temp);
                                    assertTrue(testTemperatures.contains(temp));
                                } finally {
                                    tearDown.accept(temp);
                                }
                            }
                    );
                });
    }
}