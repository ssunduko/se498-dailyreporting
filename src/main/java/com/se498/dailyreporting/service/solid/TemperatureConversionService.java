package com.se498.dailyreporting.service.solid;

import java.util.Arrays;
import java.util.List;

/**
 * Service for performing temperature conversions.
 *
 * SOLID PRINCIPLES:
 * - [SRP] Single Responsibility Principle: Responsible only for coordinating
 *   the temperature conversion process, not handling input or conversions directly.
 * - [OCP] Open/Closed Principle: Can work with new converter types without modification.
 * - [LSP] Liskov Substitution Principle: Works with any implementation of
 *   TemperatureConverterContract without knowing the specific subtype.
 * - [DIP] Dependency Inversion Principle: Depends on abstractions (interfaces)
 *   rather than concrete implementations.
 */
public class TemperatureConversionService {

    private final UserInputHandler inputHandler;
    private final List<TemperatureConverterContract> converters;

    /**
     * Constructor with dependency injection
     *
     * [DIP] Dependency Inversion Principle: Dependencies are injected rather than
     * created inside the class, and they're abstractions rather than concrete types
     */
    public TemperatureConversionService(UserInputHandler inputHandler,
                                        List<TemperatureConverterContract> converters) {
        this.inputHandler = inputHandler;
        this.converters = converters;
    }

    /**
     * Alternative constructor taking an array
     */
    public TemperatureConversionService(UserInputHandler inputHandler,
                                        TemperatureConverterContract[] converters) {
        this(inputHandler, Arrays.asList(converters));
    }

    /**
     * Demonstrates Liskov Substitution Principle in action.
     * This method uses TemperatureConverterContract without caring about
     * the actual implementation (GenericTemperatureConverter, SimpleTemperatureConverter, etc.)
     *
     * [LSP] Liskov Substitution Principle: Any subtype of TemperatureConverterContract
     * can be passed to this method without affecting its correctness
     *
     * @param temperature Temperature to convert
     * @param converter Any implementation of TemperatureConverterContract
     * @return The conversion result
     */
    public double performConversion(double temperature, TemperatureConverterContract converter) {
        return converter.convert(temperature);
    }

    /**
     * Run the temperature conversion application
     */
    public void run() {
        while (true) {
            int selection = inputHandler.readMenuSelection();

            if (selection == 0) {
                System.out.println("Exiting...");
                break;
            }

            if (selection < 1 || selection > converters.size()) {
                System.out.println("Invalid selection. Please try again.");
                continue;
            }

            // Get the selected converter (list is 0-based, but menu is 1-based)
            TemperatureConverterContract selectedConverter = converters.get(selection - 1);

            // Get temperature input
            double inputValue = inputHandler.readNumberInput();

            try {
                // Perform conversion - demonstrates LSP
                // We don't care if it's GenericTemperatureConverter, SimpleTemperatureConverter, or any other subclass
                double result = performConversion(inputValue, selectedConverter);

                // Display result
                System.out.printf("Result of %s: %.2f%n",
                        selectedConverter.getDescription(), result);

                // Special case handling for CelsiusToFahrenheitConverter
                // This doesn't violate LSP because we're explicitly checking the type
                // for optional enhanced behavior
                if (selectedConverter instanceof CelsiusToFahrenheitConverter c2f) {
                    if (c2f.isFreezing(inputValue)) {
                        System.out.println("Warning: Temperature is at or below freezing point!");
                        System.out.println(c2f.getFreezingDescription(inputValue));
                    }
                }

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }

            System.out.println();
        }

        // Clean up resources if the input handler is a ConsoleInputHandler
        if (inputHandler instanceof ConsoleInputHandler) {
            ((ConsoleInputHandler) inputHandler).close();
        }
    }
}