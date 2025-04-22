package com.se498.dailyreporting.repository;

import com.se498.dailyreporting.TestDailyReportingApplication;
import com.se498.dailyreporting.domain.bo.*;
import org.junit.jupiter.api.BeforeEach;
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
public class WeatherRecordRepositoryMockTest {

    @Autowired
    @Qualifier("fakeWeatherRecordRepository")
    private FakeWeatherRecordRepository weatherRecordRepository;

    @BeforeEach
    public void setup() {
        // Clear any existing test data
        weatherRecordRepository.clearCache();
    }

    @Test
    public void testRepositoryOperations() {
        // Add test data
        weatherRecordRepository.addTestData();

        // Verify Beverly Hills record can be found
        Optional<WeatherRecord> beverlyRecord = weatherRecordRepository.findMostRecentByZip("90210");
        assertTrue(beverlyRecord.isPresent(), "Should find Beverly Hills record");
        assertEquals("Beverly Hills", beverlyRecord.get().getLocation().getCity(),
                "Should find correct city name");

        // Test time-based queries by adding a record from the past
        Location nyc = new Location("New York", "US", "NY", "10001");
        LocalDateTime pastTime = LocalDateTime.now().minusHours(3);

        WeatherRecord oldNycRecord = WeatherRecord.builder()
                .id(UUID.randomUUID().toString())
                .location(nyc)
                .temperature(Temperature.fromFahrenheit(50.0))
                .humidity(Humidity.of(70))
                .windSpeed(WindSpeed.fromMph(8.0))
                .condition(new WeatherCondition("Cloudy", "03d"))
                .fetchedAt(pastTime)
                .dataSource("Test")
                .build();

        weatherRecordRepository.save(oldNycRecord);

        // Then add a more recent NYC record
        WeatherRecord newNycRecord = WeatherRecord.builder()
                .id(UUID.randomUUID().toString())
                .location(nyc)
                .temperature(Temperature.fromFahrenheit(55.0))
                .humidity(Humidity.of(65))
                .windSpeed(WindSpeed.fromMph(7.0))
                .condition(new WeatherCondition("Partly Cloudy", "02d"))
                .fetchedAt(LocalDateTime.now())
                .dataSource("Test")
                .build();

        weatherRecordRepository.save(newNycRecord);

        // Verify most recent finder returns newer record
        Optional<WeatherRecord> mostRecentNyc = weatherRecordRepository.findMostRecentByZip("10001");
        assertTrue(mostRecentNyc.isPresent(), "Should find NYC record");
        assertEquals(newNycRecord.getId(), mostRecentNyc.get().getId(),
                "Should find most recent NYC record");

        // Test finding by time range
        List<WeatherRecord> nycRecordsInRange = weatherRecordRepository.findByLocationZipAndFetchedAtBetween(
                "10001",
                pastTime.minusMinutes(5),
                LocalDateTime.now().plusMinutes(5));

        assertEquals(3, nycRecordsInRange.size(), "Should find both NYC records in range");

        // Test limit on recent records
        List<WeatherRecord> limitedNycRecords = weatherRecordRepository.findRecentByZip("10001", 1);
        assertEquals(1, limitedNycRecords.size(), "Should respect limit on recent records");
        assertEquals(newNycRecord.getId(), limitedNycRecords.get(0).getId(),
                "Limited results should contain most recent record");

        // Test clearing cache for specific zip
        weatherRecordRepository.clearCacheForZip("10001");
        Optional<WeatherRecord> afterClearZip = weatherRecordRepository.findMostRecentByZip("10001");
        assertFalse(afterClearZip.isPresent(), "Should not find NYC record after clearing cache");

        // Beverly Hills record should still be available
        Optional<WeatherRecord> beverlyAfterClear = weatherRecordRepository.findMostRecentByZip("90210");
        assertTrue(beverlyAfterClear.isPresent(), "Beverly Hills record should still be available");
    }
}