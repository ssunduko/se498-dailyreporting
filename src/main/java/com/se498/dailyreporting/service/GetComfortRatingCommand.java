package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.WeatherRecord;

/**
 * Command to get comfort rating for weather conditions
 */
public class GetComfortRatingCommand implements WeatherServiceCommand {
    private final WeatherRecord weatherRecord;
    private final WeatherAssessmentStrategy strategy;

    public GetComfortRatingCommand(WeatherRecord weatherRecord, WeatherStrategyFactory.StrategyType strategyType) {
        this.weatherRecord = weatherRecord;
        this.strategy = WeatherStrategyFactory.createStrategy(strategyType);
    }

    public GetComfortRatingCommand(WeatherRecord weatherRecord, WeatherAssessmentStrategy strategy) {
        this.weatherRecord = weatherRecord;
        this.strategy = strategy;
    }

    @Override
    public String execute() {
        int rating = strategy.getComfortRating(weatherRecord);

        StringBuilder result = new StringBuilder();
        result.append("Comfort rating (").append(strategy.getStrategyName()).append("): ");
        result.append(rating).append("/10");

        // Add qualitative description
        if (rating >= 9) {
            result.append(" - Excellent");
        } else if (rating >= 7) {
            result.append(" - Good");
        } else if (rating >= 5) {
            result.append(" - Moderate");
        } else if (rating >= 3) {
            result.append(" - Poor");
        } else {
            result.append(" - Very Poor");
        }

        return result.toString();
    }
}