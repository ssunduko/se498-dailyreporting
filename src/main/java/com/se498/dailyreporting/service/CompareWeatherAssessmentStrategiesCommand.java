package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.WeatherRecord;

import com.se498.dailyreporting.service.WeatherStrategyFactory.StrategyType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Command to compare different assessment strategies for the same weather record
 * Provides a visual side-by-side comparison of assessments from all strategies
 */
public class CompareWeatherAssessmentStrategiesCommand implements WeatherServiceCommand {

    private final WeatherRecord weatherRecord;
    private final boolean includeDetailedDescriptions;

    /**
     * Creates a command to compare strategies for the given weather record
     * @param weatherRecord The weather record to assess
     */
    public CompareWeatherAssessmentStrategiesCommand(WeatherRecord weatherRecord) {
        this(weatherRecord, false);
    }

    /**
     * Creates a command to compare strategies for the given weather record
     * @param weatherRecord The weather record to assess
     * @param includeDetailedDescriptions Whether to include detailed descriptions in the comparison
     */
    public CompareWeatherAssessmentStrategiesCommand(WeatherRecord weatherRecord, boolean includeDetailedDescriptions) {
        this.weatherRecord = weatherRecord;
        this.includeDetailedDescriptions = includeDetailedDescriptions;
    }

    @Override
    public String execute() {
        // Create a map to store assessment results for each strategy
        Map<WeatherStrategyFactory.StrategyType, StrategyAssessment> assessments = new EnumMap<>(WeatherStrategyFactory.StrategyType.class);

        // Get all strategy types
        WeatherStrategyFactory.StrategyType[] strategyTypes = WeatherStrategyFactory.StrategyType.values();

        // Perform assessments with each strategy
        for (WeatherStrategyFactory.StrategyType type : strategyTypes) {
            WeatherAssessmentStrategy strategy = WeatherStrategyFactory.createStrategy(type);
            StrategyAssessment assessment = new StrategyAssessment(
                    strategy.getStrategyName(),
                    strategy.isDangerous(weatherRecord),
                    strategy.isGoodForOutdoor(weatherRecord),
                    strategy.getComfortRating(weatherRecord),
                    strategy.getWeatherDescription(weatherRecord)
            );
            assessments.put(type, assessment);
        }

        // Format the comparison results
        return formatComparisonResults(assessments);
    }

    /**
     * Formats the comparison results into a readable string
     *
     * @param assessments Map of assessments for each strategy
     * @return Formatted comparison string
     */
    private String formatComparisonResults(Map<StrategyType, StrategyAssessment> assessments) {
        StringBuilder result = new StringBuilder();

        // Add header
        result.append("======= WEATHER ASSESSMENT STRATEGY COMPARISON =======\n\n");
        result.append("Location: ").append(weatherRecord.getLocation()).append("\n");
        result.append("Temperature: ").append(weatherRecord.getTemperature()).append("\n");
        result.append("Humidity: ").append(weatherRecord.getHumidity()).append("\n");
        result.append("Wind: ").append(weatherRecord.getWindSpeed()).append(" mph\n");
        result.append("Condition: ").append(weatherRecord.getCondition()).append("\n\n");

        // Format comparison table
        result.append(String.format("%-25s | %-15s | %-25s | %-6s\n",
                "STRATEGY", "DANGEROUS?", "GOOD FOR OUTDOOR?", "RATING"));
        result.append("-------------------------------------------------------------------------\n");

        for (StrategyType type : StrategyType.values()) {
            StrategyAssessment assessment = assessments.get(type);
            result.append(String.format("%-25s | %-15s | %-25s | %d/10\n",
                    assessment.strategyName,
                    assessment.isDangerous ? "YES - DANGEROUS" : "No",
                    assessment.isGoodForOutdoor ? "Yes" : "NO - UNFAVORABLE",
                    assessment.comfortRating));
        }

        result.append("\n");

        // Add detailed descriptions if requested
        if (includeDetailedDescriptions) {
            result.append("DETAILED DESCRIPTIONS:\n");
            result.append("======================\n\n");

            for (StrategyType type : StrategyType.values()) {
                StrategyAssessment assessment = assessments.get(type);
                result.append(assessment.strategyName).append(":\n");
                result.append("- ").append(assessment.description).append("\n\n");
            }
        }

        // Add conclusion
        result.append("ANALYSIS:\n");

        // Check if strategies agree on danger assessment
        boolean allAgreeOnDanger = true;
        boolean isDangerous = assessments.get(StrategyType.STANDARD).isDangerous;
        for (StrategyAssessment assessment : assessments.values()) {
            if (assessment.isDangerous != isDangerous) {
                allAgreeOnDanger = false;
                break;
            }
        }

        if (allAgreeOnDanger) {
            result.append("- All strategies agree that the weather ")
                    .append(isDangerous ? "is DANGEROUS" : "is NOT dangerous")
                    .append(".\n");
        } else {
            result.append("- Strategies DISAGREE on whether the weather is dangerous.\n");

            // List which strategies consider it dangerous
            result.append("  * Strategies considering it dangerous: ");
            boolean first = true;
            for (Map.Entry<StrategyType, StrategyAssessment> entry : assessments.entrySet()) {
                if (entry.getValue().isDangerous) {
                    if (!first) {
                        result.append(", ");
                    }
                    result.append(entry.getValue().strategyName);
                    first = false;
                }
            }
            result.append("\n");
        }

        // Check which strategy gives the highest comfort rating
        StrategyType highestRatingStrategy = null;
        int highestRating = -1;
        for (Map.Entry<StrategyType, StrategyAssessment> entry : assessments.entrySet()) {
            if (entry.getValue().comfortRating > highestRating) {
                highestRating = entry.getValue().comfortRating;
                highestRatingStrategy = entry.getKey();
            }
        }

        result.append("- Highest comfort rating (")
                .append(highestRating)
                .append("/10) given by ")
                .append(assessments.get(highestRatingStrategy).strategyName)
                .append(" strategy.\n");

        // Check if any strategy considers it good for outdoor
        boolean anyGoodForOutdoor = false;
        for (StrategyAssessment assessment : assessments.values()) {
            if (assessment.isGoodForOutdoor) {
                anyGoodForOutdoor = true;
                break;
            }
        }

        if (anyGoodForOutdoor) {
            result.append("- Some strategies consider the weather suitable for outdoor activities.\n");
        } else {
            result.append("- All strategies agree that the weather is NOT suitable for outdoor activities.\n");
        }

        return result.toString();
    }

    /**
     * Helper class to store assessment results for a strategy
     */
    private static class StrategyAssessment {
        final String strategyName;
        final boolean isDangerous;
        final boolean isGoodForOutdoor;
        final int comfortRating;
        final String description;

        public StrategyAssessment(String strategyName, boolean isDangerous,
                                  boolean isGoodForOutdoor, int comfortRating, String description) {
            this.strategyName = strategyName;
            this.isDangerous = isDangerous;
            this.isGoodForOutdoor = isGoodForOutdoor;
            this.comfortRating = comfortRating;
            this.description = description;
        }
    }
}
