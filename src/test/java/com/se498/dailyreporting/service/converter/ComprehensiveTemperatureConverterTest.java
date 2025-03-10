package com.se498.dailyreporting.service.converter;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class demonstrating various JUnit 5 testing techniques
 */
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Temperature Converter Comprehensive Test Suite")
public class ComprehensiveTemperatureConverterTest {

    // Static resources for all tests
    private static Map<String, List<Double>> testData;
    private static Properties testProperties;

    // Test instance variables
    private AbstractTemperatureConverter converter;
    private List<Double> testTemperatures;

    @Mock
    private ConversionStrategy mockStrategy;

    @Spy
    private CelsiusToFahrenheitStrategy spyStrategy;

    // ----- LIFECYCLE METHODS -----

    @BeforeAll
    static void setupBeforeAllTests() {
        System.out.println("Setting up before all tests - JUnit Lifecycle Demo");

        // Initialize shared test data (expensive operation done once)
        testData = new HashMap<>();
        testData.put("celsius", Arrays.asList(-40.0, 0.0, 20.0, 37.0, 100.0));
        testData.put("fahrenheit", Arrays.asList(-40.0, 32.0, 68.0, 98.6, 212.0));
        testData.put("kelvin", Arrays.asList(233.15, 273.15, 293.15, 310.15, 373.15));

        // Load test properties
        testProperties = new Properties();
        testProperties.setProperty("test.environment", "development");
        testProperties.setProperty("conversion.precision", "0.01");
        testProperties.setProperty("conversion.default", "celsius-to-fahrenheit");

        System.out.println("✓ Test data initialized with " + testData.get("celsius").size() + " temperature points");
        System.out.println("✓ Test properties loaded: " + testProperties.size() + " properties");
    }

    @BeforeEach
    void setupBeforeEachTest() {
        System.out.println("\nSetting up before test - JUnit Lifecycle Demo");

        // Get converter instance
        converter = UniversalTemperatureConverter.getInstance();

        // Create a copy of test data for this test
        testTemperatures = new ArrayList<>(testData.get("celsius"));

        System.out.println("✓ Converter initialized");
        System.out.println("✓ Test temperatures prepared: " + testTemperatures);
    }

    @AfterEach
    void cleanupAfterEachTest() {
        System.out.println("Cleaning up after test - JUnit Lifecycle Demo");

        // Clear any per-test data
        testTemperatures.clear();

        System.out.println("✓ Test data cleaned up");
    }

    @AfterAll
    static void cleanupAfterAllTests() {
        System.out.println("Cleaning up after all tests - JUnit Lifecycle Demo");

        // Release shared resources
        testData.clear();
        testProperties.clear();

        System.out.println("✓ All test resources released");
    }

    // ----- BASIC TESTS -----

    @Test
    @DisplayName("Default conversion strategy should be Celsius to Fahrenheit")
    void defaultStrategyTest() {
        Double result = converter.convert(0);
        assertEquals(32.0, result, "0°C should convert to 32°F");
    }

    // ----- NESTED TESTS -----

    @Nested
    @DisplayName("Celsius to Fahrenheit Conversion Tests")
    class CelsiusToFahrenheitTests {

        @Test
        @DisplayName("Freezing point conversion")
        void freezingPointTest() {
            assertEquals(32.0, converter.convert(0, "Celsius to Fahrenheit"),
                    "0°C should convert to 32°F");
        }

        @Test
        @DisplayName("Boiling point conversion")
        void boilingPointTest() {
            assertEquals(212.0, converter.convert(100, "Celsius to Fahrenheit"),
                    "100°C should convert to 212°F");
        }
    }

    @Nested
    @DisplayName("Celsius to Kelvin Conversion Tests")
    class CelsiusToKelvinTests {

        @Test
        @DisplayName("Freezing point conversion")
        void freezingPointTest() {
            assertEquals(273.15, converter.convert(0, "Celsius to Kelvin"),
                    "0°C should convert to 273.15K");
        }

        @Test
        @DisplayName("Boiling point conversion")
        void boilingPointTest() {
            assertEquals(373.15, converter.convert(100, "Celsius to Kelvin"),
                    "100°C should convert to 373.15K");
        }
    }

    // ----- DYNAMIC TESTS -----

    @TestFactory
    @DisplayName("Dynamic tests for temperature conversion")
    Stream<DynamicTest> dynamicTestsForConversion() {
        return Stream.of(
                // Test case: temperature, expected result, strategy
                new Object[]{0.0, 32.0, "Celsius to Fahrenheit"},
                new Object[]{100.0, 212.0, "Celsius to Fahrenheit"},
                new Object[]{0.0, 273.15, "Celsius to Kelvin"},
                new Object[]{100.0, 373.15, "Celsius to Kelvin"}
        ).map(testCase -> {
            double input = (double) testCase[0];
            double expected = (double) testCase[1];
            String strategy = (String) testCase[2];

            return DynamicTest.dynamicTest(
                    String.format("Converting %.1f°C using %s", input, strategy),
                    () -> assertEquals(expected, converter.convert(input, strategy), 0.01)
            );
        });
    }

    // ----- CONDITIONAL TESTS -----

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @DisplayName("Test that only runs on Windows")
    void windowsOnlyTest() {
        // This test will only run on Windows
        assertNotNull(converter, "Converter should not be null on Windows");
    }

    @Test
    @EnabledOnJre({JRE.JAVA_11, JRE.JAVA_17, JRE.JAVA_21})
    @DisplayName("Test that only runs on Java 11, 17, or 21")
    void javaVersionTest() {
        // This test will only run on Java 11, 17, or 21
        assertNotNull(converter, "Converter should work on Java 11, 17, and 21");
    }

    @Test
    @DisabledOnJre(JRE.JAVA_8)
    @DisplayName("Test that doesn't run on Java 8")
    void notOnJava8Test() {
        // This test will not run on Java 8
        assertTrue(true, "This test should not run on Java 8");
    }

    @Test
    @EnabledIfSystemProperty(named = "os.arch", matches = ".*64.*")
    @DisplayName("Test that only runs on 64-bit architectures")
    void only64BitTest() {
        // This test will only run on 64-bit architectures
        assertTrue(true, "This test should only run on 64-bit architectures");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "TEST_ENV", matches = "development")
    @DisplayName("Test that only runs in development environment")
    void developmentEnvTest() {
        // This test will only run if TEST_ENV=development
        assertTrue(true, "This test should only run in development environment");
    }

    // ----- ORDERED TESTS -----

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Ordered test sequence")
    class OrderedTests {

        // Static list to maintain state across test executions
        private static final List<String> executionSequence = new ArrayList<>();

        @BeforeAll
        static void initSequence() {
            // Clear the list before the test sequence starts
            executionSequence.clear();
        }

        @Test
        @Order(1)
        @DisplayName("First test in sequence")
        void firstTest() {
            executionSequence.add("first");
            assertEquals(1, executionSequence.size());
            assertEquals("first", executionSequence.get(0));
        }

        @Test
        @Order(2)
        @DisplayName("Second test in sequence")
        void secondTest() {
            executionSequence.add("second");
            assertEquals(2, executionSequence.size());
            assertEquals("first", executionSequence.get(0));
            assertEquals("second", executionSequence.get(1));
        }

        @Test
        @Order(3)
        @DisplayName("Third test in sequence")
        void thirdTest() {
            executionSequence.add("third");
            assertEquals(3, executionSequence.size());
            assertEquals("first", executionSequence.get(0));
            assertEquals("second", executionSequence.get(1));
            assertEquals("third", executionSequence.get(2));
        }
    }

    // Another way to order tests - using a custom MethodOrderer
    @Nested
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    @DisplayName("Ordered tests by display name")
    class OrderedByNameTests {

        @Test
        @DisplayName("1. First alphabetical test")
        void firstAlphabeticalTest() {
            // This will run first due to display name ordering
            assertTrue(true);
        }

        @Test
        @DisplayName("2. Second alphabetical test")
        void secondAlphabeticalTest() {
            // This will run second due to display name ordering
            assertTrue(true);
        }

        @Test
        @DisplayName("3. Third alphabetical test")
        void thirdAlphabeticalTest() {
            // This will run third due to display name ordering
            assertTrue(true);
        }
    }

    // ----- ASSUMPTIONS -----

    @Test
    @DisplayName("Test with assumptions")
    void testWithAssumptions() {
        // Only continue if this condition is true
        assumeTrue(System.getProperty("os.name").contains("Windows"),
                "Skipping test because it's not running on Windows");

        // This code only runs if the assumption is true
        assertNotNull(converter, "Converter should not be null");
    }

    // ----- ASSERTIONS -----

    @Test
    @DisplayName("Demonstrate different assertions")
    void differentAssertions() {
        // Simple assertions
        assertEquals(32.0, converter.convert(0), "Basic assertEquals");
        assertNotEquals(0.0, converter.convert(0), "Basic assertNotEquals");

        // Boolean assertions
        assertTrue(converter.convert(0) > 30.0, "Basic assertTrue");
        assertFalse(converter.convert(0) < 30.0, "Basic assertFalse");

        // Null assertions
        assertNotNull(converter, "Object should not be null");

        // Same instance assertions
        AbstractTemperatureConverter sameConverter = UniversalTemperatureConverter.getInstance();
        assertSame(converter, sameConverter, "Should be the same singleton instance");

        // Group assertions - all assertions are executed and failures are reported together
        assertAll("Group of assertions",
                () -> assertEquals(32.0, converter.convert(0), 0.01),
                () -> assertEquals(33.8, converter.convert(1), 0.01),
                () -> assertEquals(35.6, converter.convert(2), 0.01)
        );
    }

    // ----- EXCEPTION TESTING -----

    @Test
    @DisplayName("Test exception handling")
    void exceptionTesting() {
        // Create a mock strategy that throws an exception
        ConversionStrategy exceptionStrategy = Mockito.mock(ConversionStrategy.class);
        when(exceptionStrategy.convert(anyDouble())).thenThrow(new ArithmeticException("Division by zero"));

        // Test that the exception is thrown and properly handled
        Exception exception = assertThrows(Exception.class, () -> {
            ConversionCommand command = new TemperatureConversionCommand(100, exceptionStrategy);
            command.execute();
        });

        // Verify the exception message
        assertTrue(exception.getMessage().contains("Division by zero"));
    }

    // ----- MOCK TESTING -----

    @Test
    @DisplayName("Test with mocks")
    void mockTesting() {
        // Configure the mock strategy
        when(mockStrategy.convert(0)).thenReturn(100.0);
        when(mockStrategy.getDescription()).thenReturn("Mock Strategy");

        // Create a custom command with the mock strategy
        ConversionCommand command = new TemperatureConversionCommand(0, mockStrategy);
        Double result = command.execute();

        // Verify the result and interactions
        assertEquals(100.0, result, "Mock should return 100.0");
        verify(mockStrategy, times(1)).convert(0);
    }

    // ----- SPY TESTING -----

    @Test
    @DisplayName("Test with spies")
    void spyTesting() {
        // A spy is a partial mock - real methods are called, but we can still track and override

        // Override specific method calls on the spy
        doReturn(100.0).when(spyStrategy).convert(0);

        // Create a command using the spy
        ConversionCommand command = new TemperatureConversionCommand(0, spyStrategy);
        Double result = command.execute();

        // Verify the result and interactions
        assertEquals(100.0, result, "Spy should return overridden value 100.0");
        verify(spyStrategy, times(1)).convert(0);

        // For other values, the real method is called
        ConversionCommand command2 = new TemperatureConversionCommand(10, spyStrategy);
        Double result2 = command2.execute();
        assertEquals(50.0, result2, "Spy should call real method for non-overridden values");
    }

    // ----- REPEATED TESTS -----

    @RepeatedTest(value = 5, name = "Repeated test {currentRepetition} of {totalRepetitions}")
    @DisplayName("Demonstrate repeated test execution")
    void repeatedTest() {
        assertNotNull(converter.convert(0));
    }

    // ----- PARAMETERIZED TESTS -----

    @ParameterizedTest
    @DisplayName("Celsius to Fahrenheit parameterized test")
    @CsvSource({
            "0, 32.0",
            "1, 33.8",
            "2, 35.6",
            "10, 50.0",
            "100, 212.0"
    })
    void parameterizedConversionTest(double celsius, double expectedFahrenheit) {
        assertEquals(expectedFahrenheit, converter.convert(celsius, "Celsius to Fahrenheit"), 0.01);
    }

    @ParameterizedTest
    @DisplayName("Multiple celsius values test")
    @ValueSource(doubles = {0, 1, 2, 10, 100})
    void multipleValuesTest(double celsius) {
        double fahrenheit = converter.convert(celsius, "Celsius to Fahrenheit");
        assertTrue(fahrenheit > celsius, "Fahrenheit should be greater than Celsius");
    }

    // ----- FIXTURE PATTERN TEST -----

    /**
     * Test class demonstrating the Fixture Pattern
     */
    @Nested
    @DisplayName("Tests with Fixture Pattern")
    class FixturePatternTests {
        // The fixture - a predefined conversion environment
        private TemperatureConversionFixture fixture;

        @BeforeEach
        void setupFixture() {
            fixture = new TemperatureConversionFixture();
        }

        @Test
        @DisplayName("Test conversion using fixture")
        void testWithFixture() {
            assertEquals(fixture.expectedResults[0], fixture.converter.convert(fixture.testValues[0]),
                    "Conversion using fixture should match expected result");
        }

        // A test fixture that encapsulates test data and environment
        class TemperatureConversionFixture {
            final AbstractTemperatureConverter converter;
            final double[] testValues = {0, 10, 20, 30, 40};
            final double[] expectedResults = {32, 50, 68, 86, 104};

            TemperatureConversionFixture() {
                converter = UniversalTemperatureConverter.getInstance();
            }
        }
    }

    // ----- BUILDER PATTERN TEST -----

    @Test
    @DisplayName("Test builder pattern for conversion command")
    void testBuilderPattern() {
        // Create a test command using the builder pattern
        ConversionCommandBuilder builder = new ConversionCommandBuilder();
        ConversionCommand command = builder
                .withValue(100)
                .withStrategy(new CelsiusToFahrenheitStrategy())
                .build();

        // Execute the command and verify results
        Double result = command.execute();
        assertEquals(212.0, result, 0.01, "Converted value should be 212.0°F");
    }

    /**
     * Builder for conversion commands - demonstrates testing with the Builder Pattern
     */
    class ConversionCommandBuilder {
        private double value;
        private ConversionStrategy strategy;

        public ConversionCommandBuilder withValue(double value) {
            this.value = value;
            return this;
        }

        public ConversionCommandBuilder withStrategy(ConversionStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public ConversionCommand build() {
            return new TemperatureConversionCommand(value, strategy);
        }
    }

    // ----- CONTAINER PATTERN TEST -----

    /**
     * Test class demonstrating the Container Pattern concept
     * (Usually uses testcontainers library for real container testing,
     * this is a simplified example of the pattern)
     */
    @Nested
    @DisplayName("Tests with Container Pattern")
    class ContainerPatternTests {

        private ConverterContainer container;

        @BeforeEach
        void setupContainer() {
            container = new ConverterContainer();
            container.start();
        }

        @AfterEach
        void stopContainer() {
            container.stop();
        }

        @Test
        @DisplayName("Test conversion in container environment")
        void testWithContainer() {
            double result = container.convert(0);
            assertEquals(32.0, result, 0.01, "Container-based conversion should work correctly");
        }

        /**
         * Simulates a container that provides a controlled environment for tests
         */
        class ConverterContainer {
            private AbstractTemperatureConverter converter;
            private boolean running = false;

            public void start() {
                converter = UniversalTemperatureConverter.getInstance();
                running = true;
                System.out.println("Container started");
            }

            public void stop() {
                running = false;
                System.out.println("Container stopped");
            }

            public double convert(double value) {
                if (!running) {
                    throw new IllegalStateException("Container not running");
                }
                return converter.convert(value);
            }
        }
    }
}