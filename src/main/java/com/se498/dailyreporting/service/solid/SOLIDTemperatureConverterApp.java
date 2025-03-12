package com.se498.dailyreporting.service.solid;

import java.util.ArrayList;
import java.util.List;

/**
 * Main application class demonstrating SOLID principles
 * and especially the Liskov Substitution Principle.
 *
 * SOLID PRINCIPLES DEMONSTRATED:
 * - [SRP] Single Responsibility Principle: Each class has exactly one responsibility
 * - [OCP] Open/Closed Principle: System can be extended without modification
 * - [LSP] Liskov Substitution Principle: Subtypes can be used in place of base types
 * - [ISP] Interface Segregation Principle: Interfaces are minimal and focused
 * - [DIP] Dependency Inversion Principle: High-level modules depend on abstractions
 */
public class SOLIDTemperatureConverterApp {

    public static void main(String[] args) {
        // [DIP] Dependency Inversion Principle: Create as interface type
        UserInputHandler inputHandler = new ConsoleInputHandler();

        // [LSP] Liskov Substitution Principle: Demonstrated by mixing different
        // implementations that can be used interchangeably
        List<TemperatureConverterContract> converters = new ArrayList<>();

        // [LSP] A specialized implementation - can be used anywhere a TemperatureConverterContract is needed
        converters.add(new CelsiusToFahrenheitConverter());

        // [OCP] Factory methods - extending functionality without modification
        converters.add(SimpleTemperatureConverter.celsiusToKelvin());

        // [OCP] Direct instantiation with lambda - demonstrating extensibility
        converters.add(new SimpleTemperatureConverter(
                "Fahrenheit",
                "Celsius",
                fahrenheit -> (fahrenheit - 32) * 5/9
        ));

        // [DIP] Dependency Inversion - passing interfaces not implementations
        TemperatureConversionService service = new TemperatureConversionService(
                inputHandler,
                converters
        );

        // Run the application
        System.out.println("Temperature Converter - SOLID Principles Demo");
        System.out.println("===========================================");
        service.run();
    }
}