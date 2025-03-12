package com.se498.dailyreporting.service.solid;

/**
 * Specialized converter for Celsius to Fahrenheit conversion.
 *
 * SOLID PRINCIPLES:
 * - [SRP] Single Responsibility Principle: This class has the single responsibility
 *   of handling only Celsius to Fahrenheit conversion with domain-specific rules.
 * - [LSP] Liskov Substitution Principle: This subclass extends behavior without
 *   breaking the contract of parent classes. It can be used anywhere a SimpleTemperatureConverter
 *   or GenericTemperatureConverter is expected.
 * - [OCP] Open/Closed Principle: Adds functionality by extension rather than modification.
 * - [ISP] Interface Segregation Principle: Specialized methods (isFreezing) aren't forced
 *   on the base interface or parent classes.
 */
public class CelsiusToFahrenheitConverter extends SimpleTemperatureConverter {

    /**
     * Constants for conversion formula components
     */
    private static final double MULTIPLIER = 9.0/5.0;
    private static final double OFFSET = 32.0;

    /**
     * Create a Celsius to Fahrenheit converter
     */
    public CelsiusToFahrenheitConverter() {
        super("Celsius", "Fahrenheit", value -> value * MULTIPLIER + OFFSET);
    }

    /**
     * Override validation to add temperature-specific checks.
     * Note that this doesn't violate LSP because it doesn't change
     * the expected behavior - it just adds a constraint.
     */
    @Override
    protected void validateInputValue(double value) {
        super.validateInputValue(value);

        // Add specific validation for Celsius values
        // Absolute zero in Celsius is -273.15°C
        if (value < -273.15) {
            throw new IllegalArgumentException(
                    "Temperature cannot be below absolute zero (-273.15°C)");
        }
    }

    /**
     * Additional method specific to this converter.
     * This doesn't affect LSP because it doesn't change the behavior
     * of methods defined in the parent class.
     *
     * @param celsius Temperature in Celsius
     * @return True if the temperature is freezing or below
     */
    public boolean isFreezing(double celsius) {
        return celsius <= 0;
    }

    /**
     * Get a description of freezing status
     *
     * @param celsius Temperature in Celsius
     * @return Description of freezing status
     */
    public String getFreezingDescription(double celsius) {
        if (celsius <= 0) {
            return String.format("%.2f°C is at or below freezing (0°C)", celsius);
        } else {
            return String.format("%.2f°C is above freezing (0°C)", celsius);
        }
    }
}
