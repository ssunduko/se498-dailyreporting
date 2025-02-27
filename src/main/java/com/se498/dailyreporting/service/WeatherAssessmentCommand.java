package com.se498.dailyreporting.service;
/**
 * Command interface for weather assessment operations
 * Implements the Command pattern
 */
public interface WeatherAssessmentCommand {
    /**
     * Executes the weather assessment command
     * @return Result of the command execution
     */
    String execute();
}