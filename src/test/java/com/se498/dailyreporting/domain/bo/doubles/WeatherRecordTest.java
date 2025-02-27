package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

@DisplayName("WeatherRecord Tests")
class WeatherRecordTest {

    // Test data
    private Location location;
    private Temperature temperature;
    private WindSpeed windSpeed;
    private WeatherCondition condition;
    private Humidity humidity;
    private LocalDateTime recordedAt;
    private LocalDateTime fetchedAt;
    private WeatherRecord weatherRecord;

    @BeforeEach
    void setUp() {
        // Initialize test location
        location = mock(Location.class);
        when(location.getCity()).thenReturn("Seattle");
        when(location.getStateOrProvince()).thenReturn("WA");
        when(location.getCountry()).thenReturn("US");
        when(location.getZipCode()).thenReturn("98101");

        // Initialize test components
        temperature = mock(Temperature.class);
        when(temperature.getFahrenheit()).thenReturn(72.0);
        when(temperature.getCelsius()).thenReturn(22.2);

        windSpeed = mock(WindSpeed.class);
        when(windSpeed.getMph()).thenReturn(10.0);
        when(windSpeed.getKph()).thenReturn(16.1);

        condition = mock(WeatherCondition.class);
        when(condition.getDescription()).thenReturn("Partly Cloudy");
        when(condition.getIconCode()).thenReturn("partly_cloudy");

        humidity = mock(Humidity.class);
        when(humidity.getPercentage()).thenReturn(65);

        // Initialize timestamps
        recordedAt = LocalDateTime.now().minusHours(1);
        fetchedAt = LocalDateTime.now();

        // Create weather record
        weatherRecord = WeatherRecord.builder()
                .id("wr-123456")
                .location(location)
                .temperature(temperature)
                .windSpeed(windSpeed)
                .condition(condition)
                .humidity(humidity)
                .pressureInHg(30.1)
                .visibilityMiles(10.0)
                .uvIndex(5)
                .recordedAt(recordedAt)
                .fetchedAt(fetchedAt)
                .dataSource("Test Weather API")
                .build();
    }

    @Nested
    @DisplayName("Basic Properties Tests")
    class BasicPropertiesTests {

        @Test
        @DisplayName("Test basic properties")
        void testBasicProperties() {
            assertAll(
                    () -> assertEquals("wr-123456", weatherRecord.getId()),
                    () -> assertEquals(location, weatherRecord.getLocation()),
                    () -> assertEquals(temperature, weatherRecord.getTemperature()),
                    () -> assertEquals(windSpeed, weatherRecord.getWindSpeed()),
                    () -> assertEquals(condition, weatherRecord.getCondition()),
                    () -> assertEquals(humidity, weatherRecord.getHumidity()),
                    () -> assertEquals(30.1, weatherRecord.getPressureInHg()),
                    () -> assertEquals(10.0, weatherRecord.getVisibilityMiles()),
                    () -> assertEquals(5, weatherRecord.getUvIndex()),
                    () -> assertEquals(recordedAt, weatherRecord.getRecordedAt()),
                    () -> assertEquals(fetchedAt, weatherRecord.getFetchedAt()),
                    () -> assertEquals("Test Weather API", weatherRecord.getDataSource())
            );
        }
    }

    @Nested
    @DisplayName("Age Calculation Tests")
    class AgeCalculationTests {

        @Test
        @DisplayName("Test age calculation")
        void testAgeCalculation() {
            // Set up a weather record with known age
            LocalDateTime nowMinus30Min = LocalDateTime.now().minusMinutes(30);
            WeatherRecord record = WeatherRecord.builder()
                    .fetchedAt(nowMinus30Min)
                    .build();

            // Age should be approximately 30 minutes
            assertTrue(record.getAgeInMinutes() >= 29 && record.getAgeInMinutes() <= 31,
                    "Age should be approximately 30 minutes, actual: " + record.getAgeInMinutes());
        }

        @Test
        @DisplayName("Test freshly fetched age")
        void testFreshAge() {
            // Set up a weather record with current time
            WeatherRecord record = WeatherRecord.builder()
                    .fetchedAt(LocalDateTime.now())
                    .build();

            // Age should be very small (less than a minute)
            assertTrue(record.getAgeInMinutes() < 1,
                    "Age should be less than a minute, actual: " + record.getAgeInMinutes());
        }
    }

    @Nested
    @DisplayName("Weather Condition Tests")
    class WeatherConditionTests {

        @Test
        @DisplayName("Test severe weather conditions")
        void testSevereConditions() {
            // Set up condition to report severe weather
            when(condition.isSevere()).thenReturn(true);

            assertTrue(weatherRecord.hasSevereConditions(),
                    "Should report severe conditions when condition is severe");

            // Reset to non-severe
            when(condition.isSevere()).thenReturn(false);

            assertFalse(weatherRecord.hasSevereConditions(),
                    "Should not report severe conditions when condition is not severe");
        }
    }

    @Nested
    @DisplayName("Outdoor Activity Tests")
    class OutdoorActivityTests {

        @Test
        @DisplayName("Test unfavorable weather")
        void testUnfavorableWeather() {
            // Set up bad conditions
            when(temperature.isHot()).thenReturn(true);
            when(condition.isRainy()).thenReturn(true);
            when(windSpeed.isStrong()).thenReturn(true);

            WeatherRecord badRecord = WeatherRecord.builder()
                    .temperature(temperature)
                    .condition(condition)
                    .windSpeed(windSpeed)
                    .visibilityMiles(2.0)
                    .uvIndex(9)
                    .build();

            assertFalse(badRecord.isFavorableForOutdoorActivities(),
                    "Bad weather should not be favorable for outdoor activities");
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Test minimal builder")
        void testMinimalBuilder() {
            // Create a record with minimal required fields
            WeatherRecord minimal = WeatherRecord.builder()
                    .id("min-123")
                    .location(location)
                    .temperature(temperature)
                    .condition(condition)
                    .build();

            assertAll(
                    () -> assertEquals("min-123", minimal.getId()),
                    () -> assertEquals(location, minimal.getLocation()),
                    () -> assertEquals(temperature, minimal.getTemperature()),
                    () -> assertEquals(condition, minimal.getCondition()),
                    () -> assertNull(minimal.getWindSpeed()),
                    () -> assertNull(minimal.getHumidity()),
                    () -> assertNull(minimal.getVisibilityMiles()),
                    () -> assertNull(minimal.getUvIndex())
            );
        }

        @Test
        @DisplayName("Test complete builder")
        void testCompleteBuilder() {
            // Already tested in setUp with the complete builder
            assertNotNull(weatherRecord);
            // Additional assertions for fields that might be null in a minimal build
            assertNotNull(weatherRecord.getWindSpeed());
            assertNotNull(weatherRecord.getHumidity());
            assertNotNull(weatherRecord.getVisibilityMiles());
            assertNotNull(weatherRecord.getUvIndex());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Test fetched before recorded")
        void testFetchedBeforeRecorded() {
            // Create record where fetched is before recorded (unusual but possible)
            LocalDateTime earlier = LocalDateTime.now().minusHours(2);
            LocalDateTime later = LocalDateTime.now().minusHours(1);

            WeatherRecord invertedTimes = WeatherRecord.builder()
                    .fetchedAt(earlier)
                    .recordedAt(later)
                    .build();

            // Should still calculate age based on fetchedAt
            assertTrue(invertedTimes.getAgeInMinutes() > 60,
                    "Age calculation should be based on fetchedAt regardless of recordedAt");
        }
    }
}