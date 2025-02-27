package com.se498.dailyreporting.service;


import com.se498.dailyreporting.domain.bo.WeatherRecord;
import com.se498.dailyreporting.service.WeatherStrategyFactory.StrategyType;
import lombok.Getter;
import lombok.Setter;

/**
 * Implementation of the WeatherService interface
 * Uses the Singleton pattern to ensure only one instance exists
 * Delegates to specific assessment strategies based on context
 */
@Setter
@Getter
public class WeatherServiceImpl implements WeatherService {

    // Volatile ensures visibility across threads
    private static volatile WeatherServiceImpl instance;

    /**
     * -- GETTER --
     *  Gets the current default assessment strategy
     *
     *
     * -- SETTER --
     *  Sets the default assessment strategy directly
     * @return The current default strategy
     * @param strategy The strategy to use as default
     */
    // Strategy pattern: holds the current default assessment strategy
    private WeatherAssessmentStrategy defaultStrategy;

    // Private constructor prevents instantiation from outside
    private WeatherServiceImpl() {
        // Prevent instantiation via reflection
        if (instance != null) {
            throw new IllegalStateException("Singleton already initialized");
        }

        // Default to standard strategy
        this.defaultStrategy = WeatherStrategyFactory.createStrategy(StrategyType.STANDARD);
    }

    /**
     * Gets the singleton instance of WeatherServiceImpl
     * Uses double-checked locking for thread safety
     * @return The singleton instance
     */
    public static WeatherServiceImpl getInstance() {
        // Double-checked locking
        if (instance == null) {
            synchronized (WeatherServiceImpl.class) {
                if (instance == null) {
                    instance = new WeatherServiceImpl();
                }
            }
        }
        return instance;
    }

    /**
     * Sets the default assessment strategy
     * @param strategyType The type of strategy to use as default
     */
    public void setDefaultStrategy(StrategyType strategyType) {
        this.defaultStrategy = WeatherStrategyFactory.createStrategy(strategyType);
    }

    @Override
    public boolean isDangerous(WeatherRecord record) {
        return defaultStrategy.isDangerous(record);
    }

    /**
     * Determines if weather conditions are dangerous using a specific strategy
     * @param record The weather record to assess
     * @param strategyType The specific strategy to use
     * @return true if weather conditions are dangerous, false otherwise
     */
    public boolean isDangerous(WeatherRecord record, StrategyType strategyType) {
        WeatherAssessmentStrategy strategy = WeatherStrategyFactory.createStrategy(strategyType);
        return strategy.isDangerous(record);
    }

    @Override
    public boolean isGoodForOutdoor(WeatherRecord record) {
        return defaultStrategy.isGoodForOutdoor(record);
    }

    /**
     * Determines if weather is suitable for outdoor activities using a specific strategy
     * @param record The weather record to assess
     * @param strategyType The specific strategy to use
     * @return true if weather is good for outdoor activities, false otherwise
     */
    public boolean isGoodForOutdoor(WeatherRecord record, StrategyType strategyType) {
        WeatherAssessmentStrategy strategy = WeatherStrategyFactory.createStrategy(strategyType);
        return strategy.isGoodForOutdoor(record);
    }

    @Override
    public int getComfortRating(WeatherRecord record) {
        return defaultStrategy.getComfortRating(record);
    }

    /**
     * Gets comfort rating using a specific strategy
     * @param record The weather record to assess
     * @param strategyType The specific strategy to use
     * @return comfort rating from 1 (extremely uncomfortable) to 10 (perfect)
     */
    public int getComfortRating(WeatherRecord record, StrategyType strategyType) {
        WeatherAssessmentStrategy strategy = WeatherStrategyFactory.createStrategy(strategyType);
        return strategy.getComfortRating(record);
    }

    @Override
    public String getWeatherDescription(WeatherRecord record) {
        return defaultStrategy.getWeatherDescription(record);
    }

    /**
     * Gets weather description using a specific strategy
     * @param record The weather record to assess
     * @param strategyType The specific strategy to use
     * @return A textual description of the weather conditions
     */
    public String getWeatherDescription(WeatherRecord record, StrategyType strategyType) {
        WeatherAssessmentStrategy strategy = WeatherStrategyFactory.createStrategy(strategyType);
        return strategy.getWeatherDescription(record);
    }

    /**
     * Compares assessment results across different strategies
     * @param record The weather record to assess
     * @return A comparison of assessments from different strategies
     */
    public String compareStrategies(WeatherRecord record) {
        StringBuilder result = new StringBuilder();
        result.append("STRATEGY COMPARISON\n");
        result.append("===================\n");

        // Get all strategy types
        StrategyType[] strategyTypes = StrategyType.values();

        // Compare comfort ratings
        result.append("Comfort Ratings:\n");
        for (StrategyType type : strategyTypes) {
            int rating = getComfortRating(record, type);
            WeatherAssessmentStrategy strategy = WeatherStrategyFactory.createStrategy(type);
            result.append("- ").append(strategy.getStrategyName())
                    .append(": ").append(rating).append("/10\n");
        }
        result.append("\n");

        // Compare danger assessments
        result.append("Danger Assessments:\n");
        for (StrategyType type : strategyTypes) {
            boolean isDangerous = isDangerous(record, type);
            WeatherAssessmentStrategy strategy = WeatherStrategyFactory.createStrategy(type);
            result.append("- ").append(strategy.getStrategyName())
                    .append(": ").append(isDangerous ? "DANGEROUS" : "Safe").append("\n");
        }

        return result.toString();
    }
}