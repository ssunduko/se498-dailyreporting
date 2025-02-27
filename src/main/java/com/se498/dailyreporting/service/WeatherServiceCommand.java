package com.se498.dailyreporting.service;

/**
 * Command interface for weather service operations
 * Implements the Command pattern
 */
public interface WeatherServiceCommand {
    /**
     * Executes the weather service command
     * @return Result of the command execution
     */
    String execute();
}