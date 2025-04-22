package com.se498.dailyreporting.repository;

import com.se498.dailyreporting.TestDailyReportingApplication;
import com.se498.dailyreporting.domain.bo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {TestDailyReportingApplication.class})
@ActiveProfiles("test")
public class WeatherRepositoryIntegrationTest {

    @Autowired
    private WeatherRecordRepository weatherRecordRepository;

    @Test
    public void testSaveAndFindWeatherRecord() {
        // Create a test weather record
        Location location = new Location("Test City", "US", "CA", "12345");
        Temperature temperature = Temperature.fromFahrenheit(70.0);
        Humidity humidity = Humidity.of(50);
        WindSpeed windSpeed = WindSpeed.fromMph(5.0);
        WeatherCondition condition = new WeatherCondition("Clear skies", "01d");

        LocalDateTime now = LocalDateTime.now();

        WeatherRecord record = WeatherRecord.builder()
                .id(UUID.randomUUID().toString())
                .location(location)
                .temperature(temperature)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .condition(condition)
                .pressureInHg(29.92)
                .visibilityMiles(10.0)
                .uvIndex(3)
                .recordedAt(now)
                .fetchedAt(now)
                .dataSource("Integration Test")
                .build();

        // Save the record
        WeatherRecord savedRecord = weatherRecordRepository.save(record);
        assertNotNull(savedRecord, "Saved record should not be null");
        assertEquals(record.getId(), savedRecord.getId(), "Record ID should match");

        // Find the record by zip
        Optional<WeatherRecord> foundRecord = weatherRecordRepository.findMostRecentByZip("12345");
        assertTrue(foundRecord.isPresent(), "Should find a record for the zip code");
        assertEquals(record.getId(), foundRecord.get().getId(), "Found record should match saved record");

        // Test finding records within time range
        List<WeatherRecord> recordsInRange = weatherRecordRepository.findByLocationZipAndFetchedAtBetween(
                "12345",
                now.minusMinutes(5),
                now.plusMinutes(5));
        assertFalse(recordsInRange.isEmpty(), "Should find records within the time range");
        assertTrue(recordsInRange.stream().anyMatch(r -> r.getId().equals(record.getId())),
                "Time range query should include our record");

        // Test finding recent records
        List<WeatherRecord> recentRecords = weatherRecordRepository.findRecentByZip("12345", 10);
        assertFalse(recentRecords.isEmpty(), "Should find recent records");
        assertTrue(recentRecords.stream().anyMatch(r -> r.getId().equals(record.getId())),
                "Recent records should include our record");

        // Test clearing cache for zip
        weatherRecordRepository.clearCacheForZip("12345");
        Optional<WeatherRecord> afterClearZip = weatherRecordRepository.findMostRecentByZip("12345");
        assertFalse(afterClearZip.isPresent(), "Should not find record after clearing cache for zip");

        // Re-add the record and test clearing all cache
        weatherRecordRepository.save(record);
        weatherRecordRepository.clearCache();
        Optional<WeatherRecord> afterClearAll = weatherRecordRepository.findMostRecentByZip("12345");
        assertFalse(afterClearAll.isPresent(), "Should not find record after clearing all cache");
    }
}