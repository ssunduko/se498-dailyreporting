package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;

class TemperatureExceptionTest {

    @Test
    void testFromFahrenheit_WithNull() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            Temperature.fromFahrenheit(null);
        }, "Creating temperature from null Fahrenheit should throw NullPointerException");
    }

    @Test
    void testFromCelsius_WithNull() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            Temperature.fromCelsius(null);
        }, "Creating temperature from null Celsius should throw NullPointerException");
    }

    @Test
    void testGetFahrenheit_WithNullObject() {
        Temperature nullTemp = null;
        assertThrows(NullPointerException.class, () -> {
            nullTemp.getFahrenheit();
        }, "Calling getFahrenheit on null object should throw NullPointerException");
    }

    @Test
    void testGetCelsius_WithNullObject() {
        Temperature nullTemp = null;
        assertThrows(NullPointerException.class, () -> {
            nullTemp.getCelsius();
        }, "Calling getCelsius on null object should throw NullPointerException");
    }

    @Test
    void testToString_WithNullObject() {
        Temperature nullTemp = null;
        assertThrows(NullPointerException.class, () -> {
            nullTemp.toString();
        }, "Calling toString on null object should throw NullPointerException");
    }

    @Test
    void testIsHot_WithNullObject() {
        Temperature nullTemp = null;
        assertThrows(NullPointerException.class, () -> {
            nullTemp.isHot();
        }, "Calling isHot on null object should throw NullPointerException");
    }

    @Test
    void testIsCold_WithNullObject() {
        Temperature nullTemp = null;
        assertThrows(NullPointerException.class, () -> {
            nullTemp.isCold();
        }, "Calling isCold on null object should throw NullPointerException");
    }

    @Test
    void testIsModerate_WithNullObject() {
        Temperature nullTemp = null;
        assertThrows(NullPointerException.class, () -> {
            nullTemp.isModerate();
        }, "Calling isModerate on null object should throw NullPointerException");
    }

    @Test
    void testFromFahrenheit_DoesNotThrowWithValidValue() {
        assertDoesNotThrow(() -> {
            Temperature.fromFahrenheit(72.0);
        }, "Creating temperature from valid Fahrenheit should not throw exception");
    }

    @Test
    void testFromCelsius_DoesNotThrowWithValidValue() {
        assertDoesNotThrow(() -> {
            Temperature.fromCelsius(22.0);
        }, "Creating temperature from valid Celsius should not throw exception");
    }

    @Test
    void testExtremeValues_DoesNotThrow() {
        assertAll(
                () -> assertDoesNotThrow(() -> {
                    Temperature.fromFahrenheit(Double.MAX_VALUE);
                }, "Creating temperature from maximum Fahrenheit should not throw"),

                () -> assertDoesNotThrow(() -> {
                    Temperature.fromFahrenheit(Double.MIN_VALUE);
                }, "Creating temperature from minimum Fahrenheit should not throw"),

                () -> assertDoesNotThrow(() -> {
                    Temperature.fromCelsius(Double.MAX_VALUE);
                }, "Creating temperature from maximum Celsius should not throw"),

                () -> assertDoesNotThrow(() -> {
                    Temperature.fromCelsius(Double.MIN_VALUE);
                }, "Creating temperature from minimum Celsius should not throw")
        );
    }

    @Test
    void testInfinityValues_Behavior() {
        assertAll(
                () -> assertDoesNotThrow(() -> {
                    Temperature.fromFahrenheit(Double.POSITIVE_INFINITY);
                }, "Should handle positive infinity Fahrenheit"),

                () -> assertDoesNotThrow(() -> {
                    Temperature.fromFahrenheit(Double.NEGATIVE_INFINITY);
                }, "Should handle negative infinity Fahrenheit"),

                () -> assertDoesNotThrow(() -> {
                    Temperature.fromCelsius(Double.POSITIVE_INFINITY);
                }, "Should handle positive infinity Celsius"),

                () -> assertDoesNotThrow(() -> {
                    Temperature.fromCelsius(Double.NEGATIVE_INFINITY);
                }, "Should handle negative infinity Celsius")
        );
    }

    @Test
    void testNaN_Behavior() {
        assertAll(
                () -> assertDoesNotThrow(() -> {
                    Temperature.fromFahrenheit(Double.NaN);
                }, "Should handle NaN Fahrenheit"),

                () -> assertDoesNotThrow(() -> {
                    Temperature.fromCelsius(Double.NaN);
                }, "Should handle NaN Celsius")
        );
    }
}