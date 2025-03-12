package com.se498.dailyreporting.service.solid;

/**
 * Simple temperature converter implementation that extends the generic converter.
 *
 * SOLID PRINCIPLES:
 * - [SRP] Single Responsibility Principle: This class has the single responsibility
 *   of implementing formula-based temperature conversions.
 * - [LSP] Liskov Substitution Principle: This subclass can be used anywhere a
 *   GenericTemperatureConverter is expected without affecting the correctness of the program.
 * - [OCP] Open/Closed Principle: Uses composition (ConversionFormula) to extend
 *   functionality without modifying core behavior.
 */
public class SimpleTemperatureConverter extends GenericTemperatureConverter {

    private final ConversionFormula formula;

    /**
     * A functional interface for conversion formulas
     */
    @FunctionalInterface
    public interface ConversionFormula {
        double apply(double value);
    }

    /**
     * Create a simple converter with a conversion formula
     *
     * @param sourceUnit Source unit name
     * @param targetUnit Target unit name
     * @param formula The conversion formula to use
     */
    public SimpleTemperatureConverter(String sourceUnit, String targetUnit, ConversionFormula formula) {
        super(sourceUnit, targetUnit);
        this.formula = formula;
    }

    /**
     * Implementation of the conversion using the formula
     */
    @Override
    protected double doConversion(double value) {
        return formula.apply(value);
    }

    /**
     * Factory method to create a Celsius to Fahrenheit converter
     * @return A new converter
     */
    public static SimpleTemperatureConverter celsiusToFahrenheit() {
        return new SimpleTemperatureConverter(
                "Celsius",
                "Fahrenheit",
                celsius -> celsius * 9/5 + 32
        );
    }

    /**
     * Factory method to create a Celsius to Kelvin converter
     * @return A new converter
     */
    public static SimpleTemperatureConverter celsiusToKelvin() {
        return new SimpleTemperatureConverter(
                "Celsius",
                "Kelvin",
                celsius -> celsius + 273.15
        );
    }

    /**
     * Factory method to create a Fahrenheit to Celsius converter
     * @return A new converter
     */
    public static SimpleTemperatureConverter fahrenheitToCelsius() {
        return new SimpleTemperatureConverter(
                "Fahrenheit",
                "Celsius",
                fahrenheit -> (fahrenheit - 32) * 5/9
        );
    }
}