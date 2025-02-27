package com.se498.dailyreporting.service;

/**
 * Command to change the default assessment strategy in the WeatherServiceImpl
 */
public class ChangeWeatherAssessmentStrategyCommand implements WeatherServiceCommand {
    private final WeatherStrategyFactory.StrategyType strategyType;

    public ChangeWeatherAssessmentStrategyCommand(WeatherStrategyFactory.StrategyType strategyType) {
        this.strategyType = strategyType;
    }

    @Override
    public String execute() {
        WeatherServiceImpl service = WeatherServiceImpl.getInstance();
        service.setDefaultStrategy(strategyType);

        return "Default assessment strategy changed to: " +
                service.getDefaultStrategy().getStrategyName();
    }
}

