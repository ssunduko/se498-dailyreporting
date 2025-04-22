package com.se498.dailyreporting.repository;

import com.se498.dailyreporting.domain.bo.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Fake implementation of WeatherRecordRepository for testing
 */
@Repository
public class FakeWeatherRecordRepository implements WeatherRecordRepository {

    private final Map<String, WeatherRecord> recordsById = new ConcurrentHashMap<>();
    private final Map<String, List<WeatherRecord>> recordsByZip = new ConcurrentHashMap<>();

    /**
     * Save a weather record
     */
    @Override
    public WeatherRecord save(WeatherRecord weatherRecord) {
        // Generate ID if not present
        String id = weatherRecord.getId();
        if (id == null) {
            id = UUID.randomUUID().toString();

            // Create a new record with the generated id
            weatherRecord = WeatherRecord.builder()
                    .id(id)
                    .location(weatherRecord.getLocation())
                    .temperature(weatherRecord.getTemperature())
                    .humidity(weatherRecord.getHumidity())
                    .windSpeed(weatherRecord.getWindSpeed())
                    .condition(weatherRecord.getCondition())
                    .pressureInHg(weatherRecord.getPressureInHg())
                    .visibilityMiles(weatherRecord.getVisibilityMiles())
                    .uvIndex(weatherRecord.getUvIndex())
                    .recordedAt(weatherRecord.getRecordedAt())
                    .fetchedAt(weatherRecord.getFetchedAt())
                    .dataSource(weatherRecord.getDataSource())
                    .build();
        }

        // Store in maps
        recordsById.put(id, weatherRecord);

        // Store in zip map for location-based lookups
        String zip = weatherRecord.getLocation().getZipCode();
        if (zip != null) {
            recordsByZip.computeIfAbsent(zip, k -> new ArrayList<>()).add(weatherRecord);

            // Sort by fetchedAt time (descending)
            recordsByZip.get(zip).sort(Comparator.comparing(WeatherRecord::getFetchedAt).reversed());
        }

        return weatherRecord;
    }

    /**
     * Find the most recent weather record for a zip
     */
    @Override
    public Optional<WeatherRecord> findMostRecentByZip(String zip) {
        List<WeatherRecord> records = recordsByZip.get(zip);
        if (records == null || records.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(records.get(0)); // First record is most recent due to sorting
    }

    /**
     * Find weather records for a zip within a date range
     */
    @Override
    public List<WeatherRecord> findByLocationZipAndFetchedAtBetween(String zip, LocalDateTime start, LocalDateTime end) {
        List<WeatherRecord> records = recordsByZip.get(zip);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        return records.stream()
                .filter(record -> !record.getFetchedAt().isBefore(start) && !record.getFetchedAt().isAfter(end))
                .collect(Collectors.toList());
    }

    /**
     * Find the most recent n weather records for a zip
     */
    @Override
    public List<WeatherRecord> findRecentByZip(String zip, int limit) {
        List<WeatherRecord> records = recordsByZip.get(zip);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        return records.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Clear all cached weather data
     */
    @Override
    public void clearCache() {
        recordsById.clear();
        recordsByZip.clear();
    }

    /**
     * Clear cached data for a specific zip
     */
    @Override
    public void clearCacheForZip(String zip) {
        List<WeatherRecord> records = recordsByZip.remove(zip);
        if (records != null) {
            for (WeatherRecord record : records) {
                recordsById.remove(record.getId());
            }
        }
    }

    /**
     * Get all weather records (for testing)
     */
    public Collection<WeatherRecord> findAll() {
        return recordsById.values();
    }

    /**
     * Find record by ID (for testing)
     */
    public Optional<WeatherRecord> findById(String id) {
        return Optional.ofNullable(recordsById.get(id));
    }

    /**
     * Add test data
     */
    public void addTestData() {
        // Create a few sample locations
        Location beverly = new Location("Beverly Hills", "US", "CA", "90210");
        Location nyc = new Location("New York", "US", "NY", "10001");
        Location chicago = new Location("Chicago", "US", "IL", "60601");

        // Create sample weather data
        LocalDateTime now = LocalDateTime.now();

        // Beverly Hills - sunny and warm
        WeatherRecord beverlySunny = WeatherRecord.builder()
                .id(UUID.randomUUID().toString())
                .location(beverly)
                .temperature(Temperature.fromFahrenheit(75.0))
                .humidity(Humidity.of(45))
                .windSpeed(WindSpeed.fromMph(5.0))
                .condition(new WeatherCondition("Clear skies", "01d"))
                .pressureInHg(29.92)
                .visibilityMiles(10.0)
                .uvIndex(3)
                .recordedAt(now)
                .fetchedAt(now)
                .dataSource("Test")
                .build();

        // NYC - rainy
        WeatherRecord nycRainy = WeatherRecord.builder()
                .id(UUID.randomUUID().toString())
                .location(nyc)
                .temperature(Temperature.fromFahrenheit(55.0))
                .humidity(Humidity.of(85))
                .windSpeed(WindSpeed.fromMph(12.0))
                .condition(new WeatherCondition("Rain", "09d"))
                .pressureInHg(29.75)
                .visibilityMiles(3.0)
                .uvIndex(1)
                .recordedAt(now)
                .fetchedAt(now)
                .dataSource("Test")
                .build();

        // Chicago - snowy
        WeatherRecord chicagoSnowy = WeatherRecord.builder()
                .id(UUID.randomUUID().toString())
                .location(chicago)
                .temperature(Temperature.fromFahrenheit(28.0))
                .humidity(Humidity.of(65))
                .windSpeed(WindSpeed.fromMph(15.0))
                .condition(new WeatherCondition("Snow", "13d"))
                .pressureInHg(29.65)
                .visibilityMiles(1.0)
                .uvIndex(0)
                .recordedAt(now)
                .fetchedAt(now)
                .dataSource("Test")
                .build();

        // Save all test records
        save(beverlySunny);
        save(nycRainy);
        save(chicagoSnowy);
    }
}