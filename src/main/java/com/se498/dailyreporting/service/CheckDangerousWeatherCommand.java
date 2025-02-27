package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.WeatherRecord;

/**
 * Command to check if weather conditions are dangerous
 */
public class CheckDangerousWeatherCommand implements WeatherServiceCommand {
    private final WeatherRecord weatherRecord;
    private final WeatherAssessmentStrategy strategy;

    public CheckDangerousWeatherCommand(WeatherRecord weatherRecord, WeatherStrategyFactory.StrategyType strategyType) {
        this.weatherRecord = weatherRecord;
        this.strategy = WeatherStrategyFactory.createStrategy(strategyType);
    }

    public CheckDangerousWeatherCommand(WeatherRecord weatherRecord, WeatherAssessmentStrategy strategy) {
        this.weatherRecord = weatherRecord;
        this.strategy = strategy;
    }

    @Override
    public String execute() {
        boolean isDangerous = strategy.isDangerous(weatherRecord);

        StringBuilder result = new StringBuilder();
        result.append("Danger assessment (").append(strategy.getStrategyName()).append("): ");
        if (isDangerous) {
            result.append("DANGEROUS CONDITIONS DETECTED");
        } else {
            result.append("No dangerous conditions detected");
        }

        return result.toString();
    }
}