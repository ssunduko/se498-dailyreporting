package com.se498.dailyreporting.service.solid;

/**
 * Abstract superclass that defines common behavior for all converters.
 *
 * SOLID PRINCIPLES:
 * - [SRP] Single Responsibility Principle: This class handles only temperature conversion logic
 *   and nothing else (no UI, no application control).
 * - [OCP] Open/Closed Principle: This class is designed to be extended without modification.
 *   Uses template method pattern to allow subclasses to customize behavior.
 * - [LSP] Liskov Substitution Principle: Designed to ensure that all subclasses
 *   can be used interchangeably with the base class without issues.
 */
public abstract class GenericTemperatureConverter implements TemperatureConverterContract {

    private final String sourceUnit;
    private final String targetUnit;

    /**
     * Constructor for the abstract converter
     *
     * @param sourceUnit Unit to convert from
     * @param targetUnit Unit to convert to
     */
    protected GenericTemperatureConverter(String sourceUnit, String targetUnit) {
        this.sourceUnit = sourceUnit;
        this.targetUnit = targetUnit;
    }

    /**
     * Template method that performs the conversion and validation
     *
     * [LSP] Marked as final to ensure subclasses cannot override and break
     * the expected behavior of this method
     */
    @Override
    public final double convert(double value) {
        // Pre-conversion validation
        validateInputValue(value);

        // Perform the actual conversion using the implementation-specific logic
        double result = doConversion(value);

        // Post-conversion validation and processing
        return roundResult(result);
    }

    /**
     * Abstract method that each specific converter must implement
     * to provide its conversion logic
     *
     * @param value The value to convert
     * @return The converted value
     */
    protected abstract double doConversion(double value);

    /**
     * Validate input before conversion
     * Can be overridden by subclasses if needed, but must maintain the same behavior
     *
     * @param value Value to validate
     * @throws IllegalArgumentException if validation fails
     */
    protected void validateInputValue(double value) {
        // Default implementation - can be enhanced by subclasses
        // as long as they don't violate Liskov Substitution Principle
    }

    /**
     * Round the result to a sensible precision
     *
     * @param result The conversion result to round
     * @return The rounded result
     */
    protected double roundResult(double result) {
        // Default implementation rounds to 2 decimal places
        return Math.round(result * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        return sourceUnit + " to " + targetUnit;
    }

    /**
     * Get the source unit
     * @return The source unit of measurement
     */
    public String getSourceUnit() {
        return sourceUnit;
    }

    /**
     * Get the target unit
     * @return The target unit of measurement
     */
    public String getTargetUnit() {
        return targetUnit;
    }
}