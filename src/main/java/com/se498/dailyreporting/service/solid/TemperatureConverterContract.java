package com.se498.dailyreporting.service.solid;

/**
 * Interface defining a temperature conversion strategy.
 *
 * SOLID PRINCIPLES:
 * - [SRP] Single Responsibility Principle: This interface has the single responsibility
 *   of defining the contract for temperature conversion operations.
 * - [OCP] Open/Closed Principle: New conversion types can be added by implementing this
 *   interface without modifying existing code.
 * - [ISP] Interface Segregation Principle: Contains only methods that all temperature
 *   converters need to implement, keeping it focused and minimal.
 * - [DIP] Dependency Inversion Principle: High-level modules can depend on this
 *   abstraction rather than concrete implementations.
 */
public interface TemperatureConverterContract {
    /**
     * Convert a temperature value
     * @param value The temperature value to convert
     * @return The converted temperature value
     */
    double convert(double value);

    /**
     * Get a description of this conversion
     * @return Description of the conversion (e.g., "Celsius to Fahrenheit")
     */
    String getDescription();
}