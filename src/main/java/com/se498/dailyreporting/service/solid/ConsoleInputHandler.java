package com.se498.dailyreporting.service.solid;

import java.util.Scanner;

/**
 * Console-based implementation of the UserInputHandler interface.
 *
 * SOLID PRINCIPLES:
 * - [SRP] Single Responsibility Principle: This class has the single responsibility
 *   of handling user input via the console.
 * - [ISP] Interface Segregation Principle: Implements only the methods defined
 *   in the UserInputHandler interface that it requires.
 * - [OCP] Open/Closed Principle: New input handling behavior can be added by
 *   extension rather than modifying this class.
 * - [LSP] Liskov Substitution Principle: This class can be used anywhere a
 *   UserInputHandler is expected.
 */
public class ConsoleInputHandler implements UserInputHandler {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public double readNumberInput() {
        System.out.print("Enter temperature value: ");
        return scanner.nextDouble();
    }

    @Override
    public int readMenuSelection() {
        System.out.println("\nSelect conversion type:");
        System.out.println("1. Celsius to Fahrenheit");
        System.out.println("2. Celsius to Kelvin");
        System.out.println("3. Fahrenheit to Celsius");
        System.out.println("0. Exit");
        System.out.print("Your choice: ");
        return scanner.nextInt();
    }

    /**
     * Close the scanner to prevent resource leaks
     */
    public void close() {
        scanner.close();
    }
}