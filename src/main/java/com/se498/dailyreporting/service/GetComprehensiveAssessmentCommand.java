package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.WeatherRecord;

/**
 * Command to get a comprehensive weather assessment
 */
public class GetComprehensiveAssessmentCommand implements WeatherServiceCommand {
    private final WeatherRecord weatherRecord;
    private final WeatherAssessmentStrategy strategy;

    public GetComprehensiveAssessmentCommand(WeatherRecord weatherRecord, WeatherStrategyFactory.StrategyType strategyType) {
        this.weatherRecord = weatherRecord;
        this.strategy = WeatherStrategyFactory.createStrategy(strategyType);
    }

    public GetComprehensiveAssessmentCommand(WeatherRecord weatherRecord, WeatherAssessmentStrategy strategy) {
        this.weatherRecord = weatherRecord;
        this.strategy = strategy;
    }

    @Override
    public String execute() {
        StringBuilder result = new StringBuilder();
        result.append("COMPREHENSIVE WEATHER ASSESSMENT\n");
        result.append("=================================\n");
        result.append("Using strategy: ").append(strategy.getStrategyName()).append("\n");
        result.append("Location: ").append(weatherRecord.getLocation()).append("\n");
        result.append("Time: ").append(weatherRecord.getRecordedAt()).append("\n\n");

        // Execute other commands to get their results
        WeatherServiceCommand dangerCommand = new CheckDangerousWeatherCommand(weatherRecord, strategy);
        WeatherServiceCommand outdoorCommand = new CheckOutdoorConditionsCommand(weatherRecord, strategy);
        WeatherServiceCommand comfortCommand = new GetComfortRatingCommand(weatherRecord, strategy);
        WeatherServiceCommand descriptionCommand = new GetWeatherDescriptionCommand(weatherRecord, strategy);

        result.append(dangerCommand.execute()).append("\n");
        result.append(outdoorCommand.execute()).append("\n");
        result.append(comfortCommand.execute()).append("\n");
        result.append(descriptionCommand.execute()).append("\n\n");

        // Add raw data
        result.append("Raw Weather Data:\n");
        result.append("- Temperature: ").append(weatherRecord.getTemperature()).append("\n");
        result.append("- Humidity: ").append(weatherRecord.getHumidity()).append("\n");
        result.append("- Wind Speed: ").append(weatherRecord.getWindSpeed()).append(" mph\n");
        result.append("- Heat Index: ").append(String.format("%.1f°F", weatherRecord.getHeatIndex())).append("\n");
        result.append("- Wind Chill: ").append(String.format("%.1f°F", weatherRecord.getWindChill())).append("\n");
        result.append("- Feels Like: ").append(String.format("%.1f°F", weatherRecord.getFeelsLikeTemperature())).append("\n");
        result.append("- UV Index: ").append(weatherRecord.getUvIndex()).append("\n");

        return result.toString();
    }
}