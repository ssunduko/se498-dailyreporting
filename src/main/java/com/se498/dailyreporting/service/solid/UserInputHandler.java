package com.se498.dailyreporting.service.solid;

/**
 * Interface for handling user input.
 *
 * SOLID PRINCIPLES:
 * - [SRP] Single Responsibility Principle: This interface has the sole responsibility
 *   of defining user input operations.
 * - [ISP] Interface Segregation Principle: Contains only methods needed by clients,
 *   avoiding forcing clients to depend on methods they don't use.
 * - [DIP] Dependency Inversion Principle: High-level modules can depend on this
 *   abstraction rather than concrete input implementations.
 */
public interface UserInputHandler {
    /**
     * Read a numeric input value
     * @return The number entered by the user
     */
    double readNumberInput();

    /**
     * Read menu selection from the user
     * @return The menu option selected
     */
    int readMenuSelection();
}