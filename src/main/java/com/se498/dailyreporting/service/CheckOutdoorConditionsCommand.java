package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.WeatherRecord;

/**
 * Command to check if weather is good for outdoor activities
 */
public class CheckOutdoorConditionsCommand implements WeatherServiceCommand {
    private final WeatherRecord weatherRecord;
    private final WeatherAssessmentStrategy strategy;

    public CheckOutdoorConditionsCommand(WeatherRecord weatherRecord, WeatherStrategyFactory.StrategyType strategyType) {
        this.weatherRecord = weatherRecord;
        this.strategy = WeatherStrategyFactory.createStrategy(strategyType);
    }

    public CheckOutdoorConditionsCommand(WeatherRecord weatherRecord, WeatherAssessmentStrategy strategy) {
        this.weatherRecord = weatherRecord;
        this.strategy = strategy;
    }

    @Override
    public String execute() {
        boolean isGoodForOutdoor = strategy.isGoodForOutdoor(weatherRecord);

        StringBuilder result = new StringBuilder();
        result.append("Outdoor activity assessment (").append(strategy.getStrategyName()).append("): ");
        if (isGoodForOutdoor) {
            result.append("Conditions are favorable for outdoor activities");
        } else {
            result.append("Conditions are NOT favorable for outdoor activities");
        }

        return result.toString();
    }
}