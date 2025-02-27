package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.WeatherCondition;
import com.se498.dailyreporting.domain.bo.WeatherRecord;
import com.se498.dailyreporting.domain.bo.WindSpeed;

import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WeatherRecord using fake objects
 *
 * Fakes are working implementations with simplified functionality
 * that are suitable for testing but not for production.
 */
public class WeatherRecordFakeTest {

    /**
     * Fake WindSpeed with simplified but working implementation
     */
    @Setter
    private static class FakeWindSpeed extends WindSpeed {
        private double mph;

        public FakeWindSpeed(double mph) {
            this.mph = mph;
        }

        @Override
        public Double getMph() {
            return mph;
        }

        @Override
        public boolean isStrong() {
            return mph > 20.0; // Simplified but functional implementation
        }

    }

    /**
     * Simplistic fake condition
     */
    @Setter
    private static class FakeWeatherCondition extends WeatherCondition {
        private String conditionName;

        public FakeWeatherCondition(String conditionName) {
            this.conditionName = conditionName;
        }

        @Override
        public boolean isSevere() {
            // Real-like implementation
            return conditionName.equals("THUNDERSTORM") ||
                    conditionName.equals("TORNADO") ||
                    conditionName.equals("HURRICANE");
        }

        @Override
        public boolean isGoodForOutdoorActivities() {
            // Real-like implementation
            return conditionName.equals("CLEAR") ||
                    conditionName.equals("PARTLY_CLOUDY") ||
                    conditionName.equals("CLOUDY");
        }

        public void setCondition(String condition) {
            this.conditionName = condition;
        }
    }

    private TemperatureFake fakeTemperature;
    private HumidityFake fakeHumidity;
    private FakeWindSpeed fakeWindSpeed;
    private FakeWeatherCondition fakeCondition;
    private WeatherRecord weatherRecord;

    @BeforeEach
    void setUp() {
        // Using the fakes from the dedicated test doubles class
        fakeTemperature = new TemperatureFake(70.0);
        fakeHumidity = new HumidityFake(50);
        fakeWindSpeed = new FakeWindSpeed(10.0);
        fakeCondition = new FakeWeatherCondition("CLEAR");

        weatherRecord = WeatherRecord.builder()
                .id("test-id")
                .location(null)
                .temperature(fakeTemperature)
                .humidity(fakeHumidity)
                .windSpeed(fakeWindSpeed)
                .condition(fakeCondition)
                .pressureInHg(30.0)
                .visibilityMiles(10.0)
                .uvIndex(5)
                .recordedAt(LocalDateTime.now().minusMinutes(10))
                .fetchedAt(LocalDateTime.now().minusMinutes(5))
                .dataSource("test-source")
                .build();
    }

    @Test
    void testHeatIndex_withChangingConditions() {
        // Initial check with moderate temperature
        assertEquals(70.0, weatherRecord.getHeatIndex(), "Heat index should equal temperature when below 80°F");

        // Change temperature to trigger heat index calculation
        fakeTemperature.setFahrenheit(95.0);
        fakeHumidity.setPercentage(80);

        // Heat index should now be calculated
        assertTrue(weatherRecord.getHeatIndex() > 95.0, "Heat index should be higher than temperature in hot, humid conditions");

        // Verify calculation with known values
        // (for 95°F and 80% humidity, heat index should be approximately 133°F)
        assertEquals(133.7, weatherRecord.getHeatIndex(), 0.1);
    }

    @Test
    void testWindChill_withChangingConditions() {
        // Initial check with moderate temperature
        assertEquals(70.0, weatherRecord.getWindChill(), "Wind chill should equal temperature when above 50°F");

        // Change temperature and wind to trigger wind chill calculation
        fakeTemperature.setFahrenheit(20.0);
        fakeWindSpeed.setMph(25.0);

        // Wind chill should now be calculated
        assertTrue(weatherRecord.getWindChill() < 20.0, "Wind chill should be lower than temperature in cold, windy conditions");

        // Verify calculation with known values
        // (for 20°F and 25mph wind, wind chill should be approximately 2.5°F)
        assertEquals(2.5, weatherRecord.getWindChill(), 0.5);
    }

    @Test
    void testFeelsLikeTemperature_transitioningBetweenConditions() {
        // Start with moderate conditions
        assertEquals(70.0, weatherRecord.getFeelsLikeTemperature(), "Feels like should equal temperature in moderate conditions");

        // Change to hot conditions
        fakeTemperature.setFahrenheit(95.0);
        fakeHumidity.setPercentage(70);
        double expectedHeatIndex = weatherRecord.getHeatIndex();
        assertEquals(expectedHeatIndex, weatherRecord.getFeelsLikeTemperature(), "Feels like should equal heat index in hot conditions");

        // Change to cold conditions
        fakeTemperature.setFahrenheit(20.0);
        fakeWindSpeed.setMph(15.0);
        double expectedWindChill = weatherRecord.getWindChill();
        assertEquals(expectedWindChill, weatherRecord.getFeelsLikeTemperature(), "Feels like should equal wind chill in cold, windy conditions");
    }

    @Test
    void testHasSevereConditions_withChangingConditions() {
        // Initially not severe
        assertFalse(weatherRecord.hasSevereConditions(), "Should not have severe conditions initially");

        // Change to severe weather condition
        fakeCondition.setCondition("THUNDERSTORM");
        assertTrue(weatherRecord.hasSevereConditions(), "Should have severe conditions with thunderstorm");

        // Reset condition but make temperature extreme
        fakeCondition.setCondition("CLEAR");
        fakeTemperature.setFahrenheit(105.0);
        assertTrue(weatherRecord.hasSevereConditions(), "Should have severe conditions with extreme heat");

        // Reset temperature but make wind extreme
        fakeTemperature.setFahrenheit(70.0);
        fakeWindSpeed.setMph(35.0);
        assertTrue(weatherRecord.hasSevereConditions(), "Should have severe conditions with high winds");
    }

    @Test
    void testIsFavorableForOutdoorActivities_withChangingConditions() {
        // Initially favorable
        assertTrue(weatherRecord.isFavorableForOutdoorActivities(), "Should be favorable for outdoor activities initially");

        // Make temperature too hot
        fakeTemperature.setFahrenheit(90.0);
        assertFalse(weatherRecord.isFavorableForOutdoorActivities(), "Should not be favorable when too hot");

        // Reset temperature but make humidity high
        fakeTemperature.setFahrenheit(70.0);
        fakeHumidity.setPercentage(85);
        assertFalse(weatherRecord.isFavorableForOutdoorActivities(), "Should not be favorable when humidity is high");

        // Reset humidity but make wind strong
        fakeHumidity.setPercentage(50);
        fakeWindSpeed.setMph(25.0);
        assertFalse(weatherRecord.isFavorableForOutdoorActivities(), "Should not be favorable when wind is strong");

        // Reset wind but make condition unfavorable
        fakeWindSpeed.setMph(10.0);
        fakeCondition.setCondition("RAIN");
        assertFalse(weatherRecord.isFavorableForOutdoorActivities(), "Should not be favorable when raining");
    }

    @Test
    void testCombinedConditionChanges() {
        // Start with ideal conditions
        assertTrue(weatherRecord.isFavorableForOutdoorActivities(), "Initial conditions should be favorable");

        // Step 1: Increase temperature to hot but still tolerable
        fakeTemperature.setFahrenheit(85.0);
        assertTrue(weatherRecord.isFavorableForOutdoorActivities(), "Should still be favorable at 85°F");

        // Step 2: Increase temperature just past the threshold
        fakeTemperature.setFahrenheit(86.0);
        assertFalse(weatherRecord.isFavorableForOutdoorActivities(), "Should not be favorable above 85°F");

        // Step 3: Reset temperature but make it increasingly cold
        fakeTemperature.setFahrenheit(40.0);
        assertTrue(weatherRecord.isFavorableForOutdoorActivities(), "Should be favorable at 40°F");

        fakeTemperature.setFahrenheit(33.0);
        assertTrue(weatherRecord.isFavorableForOutdoorActivities(), "Should be favorable at 33°F");

        // Step 4: Cross the cold threshold
        fakeTemperature.setFahrenheit(32.0);
        assertFalse(weatherRecord.isFavorableForOutdoorActivities(), "Should not be favorable at freezing (32°F)");
    }

    @Test
    void testCombinedHumidityAndTemperatureEffects() {
        // Testing the heat index calculation with different humidity levels
        fakeTemperature.setFahrenheit(85.0);

        // At 85°F with low humidity
        fakeHumidity.setPercentage(30);
        double lowHumidityHeatIndex = weatherRecord.getHeatIndex();

        // At 85°F with high humidity
        fakeHumidity.setPercentage(90);
        double highHumidityHeatIndex = weatherRecord.getHeatIndex();

        // High humidity should make it feel hotter
        assertTrue(highHumidityHeatIndex > lowHumidityHeatIndex,
                "Heat index should be higher with higher humidity");
    }
}