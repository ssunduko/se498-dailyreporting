package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.WeatherRecord;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for WeatherRecord using dummy objects
 *
 * Dummies are objects that are passed around but never actually used.
 * They are just used to fill parameter lists.
 */
public class WeatherRecordDummyTest {

    @Test
    void testIsFresh_withDummyObjects() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recentTime = now.minusMinutes(15);

        // We can use dummy objects here as the isFresh method doesn't use temperature or humidity
        WeatherRecord record = WeatherRecord.builder()
                .id("test-id")
                .location(null) // null dummy for location
                .temperature(new TemperatureDummy()) // dummy temperature
                .humidity(new HumidityDummy()) // dummy humidity
                .windSpeed(null) // null dummy for wind speed
                .condition(null) // null dummy for condition
                .fetchedAt(recentTime)
                .build();

        // Act & Assert
        assertTrue(record.isFresh(), "Weather record should be fresh when fetched 15 minutes ago");
    }

    @Test
    void testIsFresh_withDummyObjects_notFresh() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oldTime = now.minusMinutes(45);

        // We can use dummy objects here as the isFresh method doesn't use temperature or humidity
        WeatherRecord record = WeatherRecord.builder()
                .id("test-id")
                .location(null) // null dummy for location
                .temperature(new TemperatureDummy()) // dummy temperature
                .humidity(new HumidityDummy()) // dummy humidity
                .windSpeed(null) // null dummy for wind speed
                .condition(null) // null dummy for condition
                .fetchedAt(oldTime)
                .build();

        // Act & Assert
        assertFalse(record.isFresh(), "Weather record should not be fresh when fetched 45 minutes ago");
    }

    @Test
    void testGetAgeInMinutes_withDummyObjects() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fetchTime = now.minusMinutes(25);

        // We can use dummy objects here as the getAgeInMinutes method doesn't use temperature or humidity
        WeatherRecord record = WeatherRecord.builder()
                .id("test-id")
                .location(null) // null dummy for location
                .temperature(new TemperatureDummy()) // dummy temperature
                .humidity(new HumidityDummy()) // dummy humidity
                .windSpeed(null) // null dummy for wind speed
                .condition(null) // null dummy for condition
                .fetchedAt(fetchTime)
                .build();

        // Act
        long age = record.getAgeInMinutes();

        // Assert
        // Allow 1-minute difference because the current time might have changed between test setup and execution
        assertTrue(age >= 25 && age <= 26, "Age should be approximately 25 minutes");
    }

    @Test
    void testRecordTimeFields_withDummyObjects() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recordedAt = now.minusMinutes(35);
        LocalDateTime fetchedAt = now.minusMinutes(25);

        WeatherRecord record = WeatherRecord.builder()
                .id("test-id")
                .temperature(new TemperatureDummy())
                .humidity(new HumidityDummy())
                .recordedAt(recordedAt)
                .fetchedAt(fetchedAt)
                .build();

        // Act & Assert
        assertEquals(recordedAt, record.getRecordedAt(), "RecordedAt field should be correctly stored");
        assertEquals(fetchedAt, record.getFetchedAt(), "FetchedAt field should be correctly stored");
    }

    @Test
    void testDataSourceAndId_withDummyObjects() {
        // Arrange
        String id = "station-123";
        String dataSource = "weather-api";

        WeatherRecord record = WeatherRecord.builder()
                .id(id)
                .temperature(new TemperatureDummy())
                .humidity(new HumidityDummy())
                .dataSource(dataSource)
                .fetchedAt(LocalDateTime.now())
                .build();

        // Act & Assert
        assertEquals(id, record.getId(), "ID should be correctly stored");
        assertEquals(dataSource, record.getDataSource(), "DataSource should be correctly stored");
    }
}
