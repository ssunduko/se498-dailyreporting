package com.se498.dailyreporting.service.converter;

import java.util.Map;
import java.util.Scanner;

/**
 * Temperature converter implementation using multiple design patterns:
 * - Singleton: Ensures a single instance of UniversalTemperatureConverter
 * - Factory: Creates appropriate conversion strategies
 * - Command: Encapsulates conversion operations
 * - Strategy: Defines family of interchangeable conversion algorithms
 */
public class UniversalTemperatureConverter extends AbstractTemperatureConverter {

    // Singleton instance
    private static UniversalTemperatureConverter instance;

    // Private constructor for Singleton pattern
    private UniversalTemperatureConverter() {}

    /**
     * Get the singleton instance
     * @return UniversalTemperatureConverter instance
     */
    public static UniversalTemperatureConverter getInstance() {
        if (instance == null) {
            instance = new UniversalTemperatureConverter();
        }
        return instance;
    }

    /**
     * Get a conversion strategy by name using the factory
     * @param strategyName Name of the strategy
     * @return The strategy, or null if not found
     */
    @Override
    protected ConversionStrategy getStrategyByName(String strategyName) {
        return ConversionStrategyFactory.getStrategy(strategyName);
    }

    /**
     * Get all available conversion strategies from the factory
     * @return Map of strategy names to strategies
     */
    @Override
    public Map<String, ConversionStrategy> getAvailableStrategies() {
        return ConversionStrategyFactory.getAllStrategies();
    }

    /**
     * Read a number from user input
     * @return User-entered number
     */
    private static double readNumber() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please Enter Number To Convert: ");
        return scanner.nextDouble();
    }

    /**
     * Read conversion scale from user input
     * @return User-entered conversion scale
     */
    private static String readConversionScale() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please Enter Conversion Scale (Celsius to Fahrenheit or Celsius to Kelvin): ");
        return scanner.nextLine();
    }

    /**
     * Main method to run the temperature converter
     */
    public static void main(String[] args) throws Exception {
        UniversalTemperatureConverter converter = UniversalTemperatureConverter.getInstance();

        // Display available strategies
        System.out.println("Available conversion strategies:");
        System.out.println("- Default: Celsius to Fahrenheit");
        for (ConversionStrategy strategy : converter.getAvailableStrategies().values()) {
            System.out.println("- " + strategy.getDescription());
        }

        double numberToConvert = readNumber();

        // Ask if user wants to use default strategy
        Scanner scanner = new Scanner(System.in);
        System.out.print("Use default strategy (Celsius to Fahrenheit)? (y/n): ");
        String useDefault = scanner.nextLine().trim().toLowerCase();

        Double result;
        if (useDefault.equals("y") || useDefault.isEmpty()) {
            // Use default strategy
            result = converter.convert(numberToConvert);
            System.out.println("Converted Number (using default strategy): " + result);
        } else {
            // Use specified strategy
            String conversionScale = readConversionScale();
            result = converter.convert(numberToConvert, conversionScale);
            if (result != null) {
                System.out.println("Converted Number: " + result);
            }
        }
    }
}