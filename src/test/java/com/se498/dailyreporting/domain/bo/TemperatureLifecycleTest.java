package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@DisplayName("Temperature Lifecycle Tests")
class TemperatureLifecycleTest {

    private static final Logger logger = Logger.getLogger(TemperatureLifecycleTest.class.getName());
    private static List<Temperature> testTemperatures;
    private Temperature roomTemp;
    private Temperature freezingTemp;
    private Temperature boilingTemp;

    @BeforeAll
    static void initializeTestData() {
        logger.info("Initializing test data - @BeforeAll");
        testTemperatures = new ArrayList<>();
    }

    @AfterAll
    static void cleanupTestData() {
        logger.info("Cleaning up test data - @AfterAll");
        testTemperatures.clear();
        testTemperatures = null;
    }

    @BeforeEach
    void setUp() {
        logger.info("Setting up test - @BeforeEach");
        roomTemp = Temperature.fromFahrenheit(72.0);
        freezingTemp = Temperature.fromFahrenheit(32.0);
        boilingTemp = Temperature.fromFahrenheit(212.0);
        testTemperatures.add(roomTemp);
        testTemperatures.add(freezingTemp);
        testTemperatures.add(boilingTemp);
    }

    @AfterEach
    void tearDown() {
        logger.info("Tearing down test - @AfterEach");
        testTemperatures.clear();
        roomTemp = null;
        freezingTemp = null;
        boilingTemp = null;
    }

    @Test
    @DisplayName("Test room temperature classification")
    void testRoomTemperature() {
        logger.info("Testing room temperature");
        assertTrue(roomTemp.isModerate());
        assertFalse(roomTemp.isHot());
        assertFalse(roomTemp.isCold());
    }

    @Test
    @DisplayName("Test freezing temperature classification")
    void testFreezingTemperature() {
        logger.info("Testing freezing temperature");
        assertTrue(freezingTemp.isCold());
        assertFalse(freezingTemp.isHot());
        assertFalse(freezingTemp.isModerate());
    }

    @Test
    @DisplayName("Test boiling temperature classification")
    void testBoilingTemperature() {
        logger.info("Testing boiling temperature");
        assertTrue(boilingTemp.isHot());
        assertFalse(boilingTemp.isCold());
        assertFalse(boilingTemp.isModerate());
    }

    @Nested
    @DisplayName("Nested Temperature Conversion Tests")
    class TemperatureConversionTests {
        private Temperature testTemp;

        @BeforeEach
        void setUpConversion() {
            logger.info("Setting up conversion test - Nested @BeforeEach");
            testTemp = Temperature.fromFahrenheit(98.6);
            testTemperatures.add(testTemp);
        }

        @AfterEach
        void tearDownConversion() {
            logger.info("Tearing down conversion test - Nested @AfterEach");
            testTemperatures.remove(testTemp);
            testTemp = null;
        }

        @Test
        @DisplayName("Test Fahrenheit to Celsius conversion")
        void testFahrenheitToCelsius() {
            logger.info("Testing F to C conversion");
            assertEquals(37.0, testTemp.getCelsius(), 0.01);
        }
    }

    @Nested
    @DisplayName("Nested Temperature String Tests")
    class TemperatureStringTests {
        private List<Temperature> stringTestTemps;

        @BeforeEach
        void setUpStringTests() {
            logger.info("Setting up string tests - Nested @BeforeEach");
            stringTestTemps = new ArrayList<>();
            stringTestTemps.add(Temperature.fromFahrenheit(0.0));
            stringTestTemps.add(Temperature.fromFahrenheit(100.0));
        }

        @AfterEach
        void tearDownStringTests() {
            logger.info("Tearing down string tests - Nested @AfterEach");
            stringTestTemps.clear();
            stringTestTemps = null;
        }

        @Test
        @DisplayName("Test temperature string format")
        void testToString() {
            logger.info("Testing toString implementation");
            Temperature temp = stringTestTemps.get(0);
            String expected = String.format("%.1f°F (%.1f°C)",
                    temp.getFahrenheit(), temp.getCelsius());
            assertEquals(expected, temp.toString());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Temperature Edge Cases")
    class TemperatureEdgeCaseTests {
        private List<Temperature> edgeCases;

        @BeforeAll
        void setUpEdgeCases() {
            logger.info("Setting up edge cases - Nested @BeforeAll");
            edgeCases = new ArrayList<>();
        }

        @AfterAll
        void tearDownEdgeCases() {
            logger.info("Tearing down edge cases - Nested @AfterAll");
            edgeCases.clear();
            edgeCases = null;
        }

        @BeforeEach
        void setUpEdgeCase() {
            logger.info("Setting up edge case test - Nested @BeforeEach");
            edgeCases.add(Temperature.fromFahrenheit(-40.0));
            edgeCases.add(Temperature.fromFahrenheit(85.0));
        }

        @AfterEach
        void tearDownEdgeCase() {
            logger.info("Tearing down edge case test - Nested @AfterEach");
            edgeCases.clear();
        }

        @Test
        @DisplayName("Test temperature boundaries")
        void testTemperatureBoundaries() {
            logger.info("Testing temperature boundaries");
            Temperature minusForty = edgeCases.get(0);
            Temperature eightyFive = edgeCases.get(1);

            assertEquals(minusForty.getFahrenheit(), minusForty.getCelsius());
            assertTrue(eightyFive.isModerate());
        }
    }

    @Test
    @DisplayName("Test temperature list state")
    void testTemperatureListState() {
        logger.info("Testing temperature list state");
        assertEquals(3, testTemperatures.size());
        assertTrue(testTemperatures.contains(roomTemp));
        assertTrue(testTemperatures.contains(freezingTemp));
        assertTrue(testTemperatures.contains(boilingTemp));
    }
}