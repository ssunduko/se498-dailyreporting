package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@DisplayName("Temperature Contract Tests")
class TemperatureContractTest {

    /**
     * Pre-condition Tests
     * Verify conditions that must be true before method execution using assumptions
     */
    @Nested
    @DisplayName("Pre-condition Tests")
    class PreConditionTests {

        @Test
        @DisplayName("fromFahrenheit should have valid input")
        void testFromFahrenheitPrecondition() {
            Double fahrenheit = 72.0;
            assumeTrue(fahrenheit != null, "Fahrenheit value should not be null");
            assumeTrue(!fahrenheit.isInfinite(), "Fahrenheit value should be finite");
            assumeTrue(!Double.isNaN(fahrenheit), "Fahrenheit value should not be NaN");

            Temperature temp = Temperature.fromFahrenheit(fahrenheit);
            assertNotNull(temp);
        }

        @Test
        @DisplayName("fromCelsius should have valid input")
        void testFromCelsiusPrecondition() {
            Double celsius = 25.0;
            assumeTrue(celsius != null, "Celsius value should not be null");
            assumeTrue(!celsius.isInfinite(), "Celsius value should be finite");
            assumeTrue(!Double.isNaN(celsius), "Celsius value should not be NaN");

            Temperature temp = Temperature.fromCelsius(celsius);
            assertNotNull(temp);
        }

        @Test
        @DisplayName("Temperature values should be within physical limits")
        void testPhysicalLimitsPrecondition() {
            Double[] temperatures = {-459.67, 0.0, 72.0, 212.0, 1000.0};

            for (Double fahrenheit : temperatures) {
                assumeTrue(fahrenheit >= -459.67, "Temperature should not be below absolute zero");
                Temperature temp = Temperature.fromFahrenheit(fahrenheit);
                assertNotNull(temp);
            }
        }

        @Test
        @DisplayName("Temperature constructor should have valid inputs")
        void testConstructorPrecondition() {
            Double fahrenheit = 72.0;
            Double celsius = 22.22;

            assumeTrue(fahrenheit != null && celsius != null,
                    "Both temperature values should not be null");
            assumeTrue(!fahrenheit.isInfinite() && !celsius.isInfinite(),
                    "Temperature values should be finite");

            Temperature temp = new Temperature(fahrenheit, celsius);
            assertNotNull(temp);
        }
    }

    /**
     * Post-condition Tests
     * Verify conditions that must be true after method execution
     */
    @Nested
    @DisplayName("Post-condition Tests")
    class PostConditionTests {

        @Test
        @DisplayName("Temperature conversion should be accurate")
        void testConversionPostCondition() {
            double[] testPoints = {32.0, 72.0, 212.0};

            for (double fahrenheit : testPoints) {
                Temperature temp = Temperature.fromFahrenheit(fahrenheit);
                double celsius = temp.getCelsius();
                double backToFahrenheit = (celsius * 9/5) + 32;

                assertEquals(fahrenheit, backToFahrenheit, 0.01,
                        "F->C->F conversion should be accurate");
            }
        }

        @Test
        @DisplayName("Temperature classification should be exclusive")
        void testClassificationPostCondition() {
            double[] testPoints = {20.0, 50.0, 72.0, 90.0};

            for (double fahrenheit : testPoints) {
                Temperature temp = Temperature.fromFahrenheit(fahrenheit);
                int classificationCount = 0;

                if (temp.isHot()) classificationCount++;
                if (temp.isCold()) classificationCount++;
                if (temp.isModerate()) classificationCount++;

                assertEquals(1, classificationCount,
                        "Temperature should have exactly one classification");
            }
        }

        @Test
        @DisplayName("String representation should be complete")
        void testStringPostCondition() {
            Temperature temp = Temperature.fromFahrenheit(72.0);
            String result = temp.toString();

            assertTrue(result.contains("°F") && result.contains("°C"),
                    "String should contain both temperature scales");
            assertTrue(result.contains("72.0") && result.contains("22.2"),
                    "String should contain both temperature values");
        }
    }

    /**
     * Invariant Tests
     * Verify conditions that must always be true
     */
    @Nested
    @DisplayName("Invariant Tests")
    class InvariantTests {

        @Test
        @DisplayName("Temperature scale relationship must be maintained")
        void testScaleRelationshipInvariant() {
            double[] testPoints = {-40.0, 0.0, 32.0, 72.0, 212.0};

            for (double fahrenheit : testPoints) {
                Temperature temp = Temperature.fromFahrenheit(fahrenheit);
                assertEquals(fahrenheit, (temp.getCelsius() * 9/5) + 32, 0.01,
                        "F/C relationship must hold");
            }
        }

        @Test
        @DisplayName("Classification boundaries must be consistent")
        void testClassificationBoundariesInvariant() {
            // Test boundary points
            Temperature justBelowCold = Temperature.fromFahrenheit(31.9);
            Temperature atCold = Temperature.fromFahrenheit(32.0);
            Temperature justAboveHot = Temperature.fromFahrenheit(85.1);
            Temperature atHot = Temperature.fromFahrenheit(85.0);


            assertTrue(justBelowCold.isCold(), "31.9°F should be cold");
            assertTrue(atCold.isCold(), "32.0°F should be cold");
            assertTrue(justAboveHot.isHot(), "85.1°F should be hot");
            assertFalse(atHot.isHot(), "85.0°F should not be hot");
        }

        @Test
        @DisplayName("Object state must be immutable")
        void testImmutabilityInvariant() {
            Temperature temp = Temperature.fromFahrenheit(72.0);
            double initialF = temp.getFahrenheit();
            double initialC = temp.getCelsius();

            // Multiple operations
            temp.toString();
            temp.isHot();
            temp.isCold();
            temp.isModerate();

            assertEquals(initialF, temp.getFahrenheit(), "Fahrenheit should be immutable");
            assertEquals(initialC, temp.getCelsius(), "Celsius should be immutable");
        }

        @Test
        @DisplayName("Special temperature points must be preserved")
        void testSpecialPointsInvariant() {
            // Test -40°F = -40°C
            Temperature minusForty = Temperature.fromFahrenheit(-40.0);
            assertEquals(minusForty.getFahrenheit(), minusForty.getCelsius(),
                    "-40 should be equal in both scales");

            // Test 0°C = 32°F
            Temperature freezing = Temperature.fromCelsius(0.0);
            assertEquals(32.0, freezing.getFahrenheit(),
                    "Freezing point conversion should be accurate");

            // Test 100°C = 212°F
            Temperature boiling = Temperature.fromCelsius(100.0);
            assertEquals(212.0, boiling.getFahrenheit(),
                    "Boiling point conversion should be accurate");
        }

        @Test
        @DisplayName("State consistency must be maintained")
        void testStateConsistencyInvariant() {
            for (double f = -40.0; f <= 120.0; f += 20.0) {
                Temperature temp = Temperature.fromFahrenheit(f);
                boolean isExactlyOne = temp.isHot() ^ temp.isCold() ^ temp.isModerate();
                assertTrue(isExactlyOne,
                        "Temperature must have exactly one classification");
            }
        }
    }

    /**
     * Combined Tests
     * Verify preconditions, postconditions, and invariants together
     */
    @Nested
    @DisplayName("Combined Test")
    class CombinedTest {

        @Test
        @DisplayName("Temperature comprehensive contract test")
        void testTemperatureContract() {
            // Preconditions (assumptions)
            Double fahrenheit = 72.0;
            assumeTrue(fahrenheit != null, "Fahrenheit value should not be null");
            assumeTrue(!fahrenheit.isInfinite(), "Fahrenheit value should be finite");
            assumeTrue(!Double.isNaN(fahrenheit), "Fahrenheit value should not be NaN");
            assumeTrue(fahrenheit >= -459.67, "Temperature should not be below absolute zero");

            // Invariants before operation
            Temperature temp = Temperature.fromFahrenheit(fahrenheit);
            assertNotNull(temp);
            assertEquals(fahrenheit, (temp.getCelsius() * 9/5) + 32, 0.01,
                    "F/C relationship must hold");

            // Initial state verification
            double initialF = temp.getFahrenheit();
            double initialC = temp.getCelsius();

            // Operations and postconditions
            String stringRep = temp.toString();
            assertTrue(stringRep.contains("°F") && stringRep.contains("°C"),
                    "String should contain both temperature scales");

            // Classification check
            int classificationCount = 0;
            if (temp.isHot()) classificationCount++;
            if (temp.isCold()) classificationCount++;
            if (temp.isModerate()) classificationCount++;
            assertEquals(1, classificationCount,
                    "Temperature should have exactly one classification");

            // Conversion accuracy
            double celsius = temp.getCelsius();
            double backToFahrenheit = (celsius * 9/5) + 32;
            assertEquals(fahrenheit, backToFahrenheit, 0.01,
                    "F->C->F conversion should be accurate");

            // Invariants after operations
            assertEquals(initialF, temp.getFahrenheit(), "Fahrenheit should be immutable");
            assertEquals(initialC, temp.getCelsius(), "Celsius should be immutable");
            assertEquals(fahrenheit, (temp.getCelsius() * 9/5) + 32, 0.01,
                    "F/C relationship must hold after operations");

            // Special points verification
            Temperature freezing = Temperature.fromCelsius(0.0);
            assertEquals(32.0, freezing.getFahrenheit(),
                    "Freezing point conversion should be accurate");

            Temperature minusForty = Temperature.fromFahrenheit(-40.0);
            assertEquals(minusForty.getFahrenheit(), minusForty.getCelsius(),
                    "-40 should be equal in both scales");
        }
    }

}