package com.se498.dailyreporting.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import com.se498.dailyreporting.domain.bo.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class WeatherServiceIntegrationTest {

    private WeatherServiceImpl weatherService;
    private WeatherRecord niceWeather;
    private WeatherRecord stormyWeather;
    private WeatherRecord extremeHeatWeather;

    @BeforeEach
    public void setUp() {
        // Initialize the weather service
        weatherService = WeatherServiceImpl.getInstance();

        // Reset to standard strategy before each test
        weatherService.setDefaultStrategy(WeatherStrategyFactory.StrategyType.STANDARD);

        // Initialize weather records for testing
        niceWeather = createNiceWeatherRecord();
        stormyWeather = createStormyWeatherRecord();
        extremeHeatWeather = createExtremeHeatWeatherRecord();
    }

    @Nested
    @DisplayName("Singleton Pattern Tests")
    class SingletonTests {

        @Test
        @DisplayName("Multiple getInstance calls should return the same instance")
        public void testSingletonPattern() {
            // Get multiple instances
            WeatherService instance1 = WeatherServiceImpl.getInstance();
            WeatherService instance2 = WeatherServiceImpl.getInstance();

            // Verify both references point to the same object
            assertSame(instance1, instance2, "Both instances should be the same object");
        }
    }

    @Nested
    @DisplayName("Strategy Pattern Tests")
    class StrategyTests {

        @Test
        @DisplayName("Factory should create different types of strategies")
        public void testStrategyFactory() {
            // Test creating strategies for each strategy type
            for (WeatherStrategyFactory.StrategyType type : WeatherStrategyFactory.StrategyType.values()) {
                WeatherAssessmentStrategy strategy = WeatherStrategyFactory.createStrategy(type);

                // Verify strategy was created and is not null
                assertNotNull(strategy, "Strategy should not be null for type: " + type);

                // Check that the strategy name is not null or empty
                String strategyName = strategy.getStrategyName();
                assertNotNull(strategyName, "Strategy name should not be null for type: " + type);
                assertFalse(strategyName.isEmpty(), "Strategy name should not be empty for type: " + type);

                // Verify the strategy class name contains the type name
                // (avoid testing the exact content of getStrategyName() as it might be customized)
                String className = strategy.getClass().getSimpleName();
                assertTrue(className.contains(type.name()) ||
                                className.contains(type.name().replace("_", "")) ||
                                className.contains(convertToCamelCase(type.name())),
                        "Strategy class name should relate to its type: " + className + " for type " + type);
            }
        }

        /**
         * Helper method to convert ENUM_NAME to EnumName format
         */
        private String convertToCamelCase(String enumName) {
            StringBuilder result = new StringBuilder();
            String[] words = enumName.split("_");

            for (String word : words) {
                if (!word.isEmpty()) {
                    result.append(word.substring(0, 1).toUpperCase());
                    if (word.length() > 1) {
                        result.append(word.substring(1).toLowerCase());
                    }
                }
            }

            return result.toString();
        }

        @Test
        @DisplayName("Different strategies should provide different comfort ratings")
        public void testDifferentStrategiesForSameWeather() {
            // Get ratings from different strategies for the same weather
            int standardRating = weatherService.getComfortRating(niceWeather, WeatherStrategyFactory.StrategyType.STANDARD);
            int outdoorRating = weatherService.getComfortRating(niceWeather, WeatherStrategyFactory.StrategyType.OUTDOOR_ACTIVITY);
            int travelRating = weatherService.getComfortRating(niceWeather, WeatherStrategyFactory.StrategyType.TRAVEL_SAFETY);
            int healthRating = weatherService.getComfortRating(niceWeather, WeatherStrategyFactory.StrategyType.HEALTH_IMPACT);

            // Store all ratings in an array for easier verification
            int[] ratings = {standardRating, outdoorRating, travelRating, healthRating};

            // Verify that at least some ratings are different
            boolean allSame = true;
            for (int i = 1; i < ratings.length; i++) {
                if (ratings[i] != ratings[0]) {
                    allSame = false;
                    break;
                }
            }

            assertFalse(allSame, "Different strategies should provide at least some different ratings");
        }

        @Test
        @DisplayName("Default strategy can be changed")
        public void testChangeDefaultStrategy() {
            // Get initial strategy
            WeatherAssessmentStrategy initialStrategy = weatherService.getDefaultStrategy();
            String initialClassName = initialStrategy.getClass().getSimpleName();

            // Change the strategy
            weatherService.setDefaultStrategy(WeatherStrategyFactory.StrategyType.TRAVEL_SAFETY);

            // Get the new strategy
            WeatherAssessmentStrategy newStrategy = weatherService.getDefaultStrategy();
            String newClassName = newStrategy.getClass().getSimpleName();

            // Verify they are different objects of different classes
            assertNotEquals(initialStrategy, newStrategy, "Strategy object should change after setting new strategy");
            assertNotEquals(initialClassName, newClassName, "Strategy class should change after setting new strategy");

            // Verify the new strategy class name relates to TRAVEL_SAFETY (being more flexible about exact naming)
            assertTrue(
                    newClassName.contains("Travel") ||
                            newClassName.contains("travel") ||
                            newClassName.contains("TRAVEL") ||
                            newClassName.toLowerCase().contains("safety"),
                    "New strategy class name should relate to TRAVEL_SAFETY but was: " + newClassName
            );

            // Reset for other tests
            weatherService.setDefaultStrategy(WeatherStrategyFactory.StrategyType.STANDARD);
        }
    }

    @Nested
    @DisplayName("Weather Assessment Tests")
    class WeatherAssessmentTests {

        @Test
        @DisplayName("Nice weather should have good comfort ratings")
        public void testNiceWeatherRatings() {
            int standardRating = weatherService.getComfortRating(niceWeather, WeatherStrategyFactory.StrategyType.STANDARD);
            int outdoorRating = weatherService.getComfortRating(niceWeather, WeatherStrategyFactory.StrategyType.OUTDOOR_ACTIVITY);

            assertTrue(standardRating >= 7, "Nice weather should have good standard comfort rating");
            assertTrue(outdoorRating >= 7, "Nice weather should have good outdoor comfort rating");
        }

        @Test
        @DisplayName("Stormy weather should have poor ratings for outdoor and travel")
        public void testStormyWeatherRatings() {
            int outdoorRating = weatherService.getComfortRating(stormyWeather, WeatherStrategyFactory.StrategyType.OUTDOOR_ACTIVITY);
            int travelRating = weatherService.getComfortRating(stormyWeather, WeatherStrategyFactory.StrategyType.TRAVEL_SAFETY);

            assertTrue(outdoorRating <= 4, "Stormy weather should have poor outdoor rating");
            assertTrue(travelRating <= 4, "Stormy weather should have poor travel rating");
        }

        @Test
        @DisplayName("Extreme heat should have poor health impact rating")
        public void testExtremeHeatHealthRating() {
            int healthRating = weatherService.getComfortRating(extremeHeatWeather, WeatherStrategyFactory.StrategyType.HEALTH_IMPACT);

            assertTrue(healthRating <= 4, "Extreme heat should have poor health impact rating");
        }

        @Test
        @DisplayName("Weather descriptions should be strategy-specific")
        public void testWeatherDescriptions() {
            String standardDesc = weatherService.getWeatherDescription(niceWeather, WeatherStrategyFactory.StrategyType.STANDARD);
            String outdoorDesc = weatherService.getWeatherDescription(niceWeather, WeatherStrategyFactory.StrategyType.OUTDOOR_ACTIVITY);

            assertNotNull(standardDesc, "Standard description should not be null");
            assertNotNull(outdoorDesc, "Outdoor description should not be null");
            assertNotEquals(standardDesc, outdoorDesc, "Different strategies should provide different descriptions");
        }

        @Test
        @DisplayName("Weather danger check should identify dangerous conditions")
        public void testDangerousWeatherCheck() {
            boolean niceIsDangerous = weatherService.isDangerous(niceWeather, WeatherStrategyFactory.StrategyType.STANDARD);
            boolean stormyIsDangerous = weatherService.isDangerous(stormyWeather, WeatherStrategyFactory.StrategyType.STANDARD);

            assertFalse(niceIsDangerous, "Nice weather should not be marked as dangerous");
            assertTrue(stormyIsDangerous, "Stormy weather should be marked as dangerous");
        }
    }

    @Nested
    @DisplayName("Command Pattern Tests")
    class CommandPatternTests {

        @Test
        @DisplayName("Basic command execution should work correctly")
        public void testBasicCommandExecution() {
            // Create a simple command
            WeatherServiceCommand comfortCommand = new GetComfortRatingCommand(niceWeather, WeatherStrategyFactory.StrategyType.STANDARD);

            // Execute command directly
            String result = comfortCommand.execute();

            // Verify command execution
            assertNotNull(result, "Command execution should return a non-null result");
            assertFalse(result.isEmpty(), "Command result should not be empty");
            assertTrue(result.contains("Comfort rating"), "Result should contain comfort rating information");
        }

        @Test
        @DisplayName("Commands with different strategies should produce different results")
        public void testCommandsWithDifferentStrategies() {
            // Create commands with different strategies
            WeatherServiceCommand standardCommand = new GetComfortRatingCommand(niceWeather, WeatherStrategyFactory.StrategyType.STANDARD);
            WeatherServiceCommand outdoorCommand = new GetComfortRatingCommand(niceWeather, WeatherStrategyFactory.StrategyType.OUTDOOR_ACTIVITY);

            // Execute commands directly
            String standardResult = standardCommand.execute();
            String outdoorResult = outdoorCommand.execute();

            // Verify different results
            assertNotNull(standardResult);
            assertNotNull(outdoorResult);
            assertNotEquals(standardResult, outdoorResult, "Different strategies should give different results");
        }

        @Test
        @DisplayName("Change strategy command should update the service's default strategy")
        public void testChangeStrategyCommand() {
            // Get initial strategy and its class
            WeatherAssessmentStrategy initialStrategy = weatherService.getDefaultStrategy();
            Class<?> initialStrategyClass = initialStrategy.getClass();

            // Create command to change strategy
            WeatherServiceCommand changeCommand = new ChangeWeatherAssessmentStrategyCommand(WeatherStrategyFactory.StrategyType.TRAVEL_SAFETY);

            // Execute command directly
            String result = changeCommand.execute();

            // Verify strategy was changed
            WeatherAssessmentStrategy newStrategy = weatherService.getDefaultStrategy();
            Class<?> newStrategyClass = newStrategy.getClass();

            // Verify the classes are different (strategy was changed)
            assertNotEquals(initialStrategyClass, newStrategyClass,
                    "Default strategy class should be changed");

            // Verify the new strategy is related to travel safety by checking its class name
            String newClassName = newStrategyClass.getSimpleName();
            assertTrue(
                    newClassName.contains("Travel") ||
                            newClassName.toLowerCase().contains("travel") ||
                            newClassName.toLowerCase().contains("safety"),
                    "New strategy class should be related to travel safety but was: " + newClassName
            );

            // Verify the command returned a meaningful result
            assertNotNull(result, "Command result should not be null");
            assertFalse(result.isEmpty(), "Command result should not be empty");

            // Reset for other tests
            weatherService.setDefaultStrategy(WeatherStrategyFactory.StrategyType.STANDARD);
        }

        @Test
        @DisplayName("CheckDangerousWeatherCommand should correctly identify dangerous conditions")
        public void testCheckDangerousWeatherCommand() {
            // Test with nice weather (should not be dangerous)
            WeatherServiceCommand safeCommand = new CheckDangerousWeatherCommand(niceWeather, WeatherStrategyFactory.StrategyType.STANDARD);
            String safeResult = safeCommand.execute();

            // Test with stormy weather (should be dangerous)
            WeatherServiceCommand dangerousCommand = new CheckDangerousWeatherCommand(stormyWeather, WeatherStrategyFactory.StrategyType.STANDARD);
            String dangerousResult = dangerousCommand.execute();

            // Verify results
            assertNotNull(safeResult);
            assertNotNull(dangerousResult);
            assertTrue(safeResult.contains("No dangerous conditions"),
                    "Nice weather should not be identified as dangerous");
            assertTrue(dangerousResult.contains("DANGEROUS CONDITIONS"),
                    "Stormy weather should be identified as dangerous");
        }

        @Test
        @DisplayName("CheckOutdoorConditionsCommand should correctly assess outdoor suitability")
        public void testCheckOutdoorConditionsCommand() {
            // Test with nice weather (should be good for outdoor)
            WeatherServiceCommand goodOutdoorCommand = new CheckOutdoorConditionsCommand(niceWeather, WeatherStrategyFactory.StrategyType.OUTDOOR_ACTIVITY);
            String goodOutdoorResult = goodOutdoorCommand.execute();

            // Test with stormy weather (should not be good for outdoor)
            WeatherServiceCommand badOutdoorCommand = new CheckOutdoorConditionsCommand(stormyWeather, WeatherStrategyFactory.StrategyType.OUTDOOR_ACTIVITY);
            String badOutdoorResult = badOutdoorCommand.execute();

            // Verify results
            assertNotNull(goodOutdoorResult);
            assertNotNull(badOutdoorResult);
            assertTrue(goodOutdoorResult.contains("favorable"),
                    "Nice weather should be favorable for outdoor activities");
            assertTrue(badOutdoorResult.contains("NOT favorable"),
                    "Stormy weather should not be favorable for outdoor activities");
        }

        @Test
        @DisplayName("GetComfortRatingCommand should provide correct ratings")
        public void testGetComfortRatingCommand() {
            // Test with different weather conditions
            WeatherServiceCommand niceCommand = new GetComfortRatingCommand(niceWeather, WeatherStrategyFactory.StrategyType.STANDARD);
            WeatherServiceCommand stormyCommand = new GetComfortRatingCommand(stormyWeather, WeatherStrategyFactory.StrategyType.STANDARD);
            WeatherServiceCommand heatCommand = new GetComfortRatingCommand(extremeHeatWeather, WeatherStrategyFactory.StrategyType.STANDARD);

            // Execute commands directly
            String niceResult = niceCommand.execute();
            String stormyResult = stormyCommand.execute();
            String heatResult = heatCommand.execute();

            // Verify results
            assertNotNull(niceResult);
            assertNotNull(stormyResult);
            assertNotNull(heatResult);

            // Nice weather should have higher rating than stormy or extreme heat
            // Extract ratings and compare - we can only approximate since we don't control the rating algorithm
            int niceRating = extractRatingFromResult(niceResult);
            int stormyRating = extractRatingFromResult(stormyResult);
            int heatRating = extractRatingFromResult(heatResult);

            assertTrue(niceRating > stormyRating,
                    "Nice weather should have better rating than stormy weather");
            assertTrue(niceRating > heatRating,
                    "Nice weather should have better rating than extreme heat");
        }

        @Test
        @DisplayName("Command with custom strategy should use that strategy")
        public void testCommandWithCustomStrategy() {
            // Create a custom strategy implementation
            WeatherAssessmentStrategy customStrategy = new CustomTestStrategy();

            // Create command with explicit strategy
            WeatherServiceCommand command = new GetComfortRatingCommand(niceWeather, customStrategy);

            // Execute command directly
            String result = command.execute();

            // Verify result
            assertNotNull(result);
            assertTrue(result.contains("Custom Test Strategy"), "Result should use the custom strategy name");
            assertTrue(result.contains("5/10"), "Result should contain the fixed rating from custom strategy");
        }

        @Test
        @DisplayName("Multiple command execution in sequence should work correctly")
        public void testMultipleCommandsSequence() {
            // Create commands with the same strategy type
            WeatherServiceCommand comfortCommand = new GetComfortRatingCommand(niceWeather, WeatherStrategyFactory.StrategyType.OUTDOOR_ACTIVITY);
            WeatherServiceCommand outdoorCommand = new CheckOutdoorConditionsCommand(niceWeather, WeatherStrategyFactory.StrategyType.OUTDOOR_ACTIVITY);
            WeatherServiceCommand dangerCommand = new CheckDangerousWeatherCommand(niceWeather, WeatherStrategyFactory.StrategyType.OUTDOOR_ACTIVITY);

            // Execute commands in sequence
            String comfortResult = comfortCommand.execute();
            String outdoorResult = outdoorCommand.execute();
            String dangerResult = dangerCommand.execute();

            // Verify each result
            assertNotNull(comfortResult, "Comfort command result should not be null");
            assertTrue(comfortResult.contains("Comfort rating"), "Result should contain comfort rating information");

            assertNotNull(outdoorResult, "Outdoor command result should not be null");
            assertTrue(outdoorResult.toLowerCase().contains("outdoor") ||
                            outdoorResult.contains("favorable") ||
                            outdoorResult.contains("activities"),
                    "Result should contain information about outdoor suitability");

            assertNotNull(dangerResult, "Danger command result should not be null");
            assertTrue(dangerResult.toLowerCase().contains("danger") ||
                            dangerResult.contains("conditions"),
                    "Result should contain information about dangerous conditions");
        }
    }

    /**
     * Custom strategy implementation for testing without mocks
     */
    private static class CustomTestStrategy implements WeatherAssessmentStrategy {
        @Override
        public String getStrategyName() {
            return "Custom Test Strategy";
        }

        @Override
        public int getComfortRating(WeatherRecord record) {
            return 5; // Fixed rating for testing
        }

        @Override
        public String getWeatherDescription(WeatherRecord record) {
            return "Custom weather description for testing";
        }

        @Override
        public boolean isDangerous(WeatherRecord record) {
            return false; // Fixed value for testing
        }

        @Override
        public boolean isGoodForOutdoor(WeatherRecord record) {
            return true; // Fixed value for testing
        }
    }

    /**
     * Helper method to extract numeric rating from command result string
     */
    private int extractRatingFromResult(String result) {
        // Expected format: "Comfort rating (STRATEGY_NAME): X/10 - Description"
        try {
            int ratingStart = result.indexOf(": ") + 2;
            int ratingEnd = result.indexOf("/10");
            return Integer.parseInt(result.substring(ratingStart, ratingEnd).trim());
        } catch (Exception e) {
            fail("Could not extract rating from result: " + result);
            return -1;
        }
    }

    // Helper methods to create test weather records

    private static WeatherRecord createNiceWeatherRecord() {
        // Create a pleasant weather record
        Temperature temperature = Temperature.fromFahrenheit(75.0);
        Humidity humidity = Humidity.of(45);
        WindSpeed windSpeed = WindSpeed.fromMph(8.0);
        WeatherCondition weatherCondition = new WeatherCondition("Clear and sunny");
        Location location = new Location("Pleasant City", "Test Country", "Test State");

        return WeatherRecord.builder()
                .id("nice-weather")
                .location(location)
                .temperature(temperature)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .condition(weatherCondition)
                .pressureInHg(30.0)
                .visibilityMiles(10.0)
                .uvIndex(4)
                .recordedAt(LocalDateTime.now())
                .fetchedAt(LocalDateTime.now())
                .dataSource("WeatherTest")
                .build();
    }

    private static WeatherRecord createStormyWeatherRecord() {
        // Create a stormy weather record
        Temperature temperature = Temperature.fromFahrenheit(60.0);
        Humidity humidity = Humidity.of(80);
        WindSpeed windSpeed = WindSpeed.fromMph(35.0);
        WeatherCondition weatherCondition = new WeatherCondition("Thunderstorm with heavy rain");
        Location location = new Location("Stormy City", "Test Country", "Test State");

        return WeatherRecord.builder()
                .id("stormy-weather")
                .location(location)
                .temperature(temperature)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .condition(weatherCondition)
                .pressureInHg(29.5)
                .visibilityMiles(2.0)
                .uvIndex(1)
                .recordedAt(LocalDateTime.now())
                .fetchedAt(LocalDateTime.now())
                .dataSource("WeatherTest")
                .build();
    }

    private static WeatherRecord createExtremeHeatWeatherRecord() {
        // Create an extreme heat weather record
        Temperature temperature = Temperature.fromFahrenheit(102.0);
        Humidity humidity = Humidity.of(70);
        WindSpeed windSpeed = WindSpeed.fromMph(5.0);
        WeatherCondition weatherCondition = new WeatherCondition("Clear and hot");
        Location location = new Location("Hot City", "Test Country", "Test State");

        return WeatherRecord.builder()
                .id("extreme-heat")
                .location(location)
                .temperature(temperature)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .condition(weatherCondition)
                .pressureInHg(30.1)
                .visibilityMiles(8.0)
                .uvIndex(9)
                .recordedAt(LocalDateTime.now())
                .fetchedAt(LocalDateTime.now())
                .dataSource("WeatherTest")
                .build();
    }
}