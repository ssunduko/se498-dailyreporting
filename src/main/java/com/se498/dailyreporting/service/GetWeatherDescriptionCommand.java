package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.WeatherRecord;

/**
 * Command to get a full weather description
 */
public class GetWeatherDescriptionCommand implements WeatherServiceCommand {
    private final WeatherRecord weatherRecord;
    private final WeatherAssessmentStrategy strategy;

    public GetWeatherDescriptionCommand(WeatherRecord weatherRecord, WeatherStrategyFactory.StrategyType strategyType) {
        this.weatherRecord = weatherRecord;
        this.strategy = WeatherStrategyFactory.createStrategy(strategyType);
    }

    public GetWeatherDescriptionCommand(WeatherRecord weatherRecord, WeatherAssessmentStrategy strategy) {
        this.weatherRecord = weatherRecord;
        this.strategy = strategy;
    }

    @Override
    public String execute() {
        String description = strategy.getWeatherDescription(weatherRecord);

        return "Weather description (" + strategy.getStrategyName() + "): " + description;
    }
}
