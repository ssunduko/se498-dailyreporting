package com.se498.dailyreporting.repository;

import com.se498.dailyreporting.config.CacheConfig;
import com.se498.dailyreporting.domain.bo.WeatherRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory implementation of WeatherRecordRepository using Spring Cache
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InMemoryWeatherRepository implements WeatherRecordRepository {

    private final CacheManager cacheManager;

    // In-memory storage structure with zip as key and list of records as value
    private final Map<String, List<WeatherRecord>> zipWeatherMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * {@inheritDoc}
     *
     * Stores a weather record and updates the cache for the zip
     */
    @Override
    public WeatherRecord save(WeatherRecord weatherRecord) {
        log.debug("Saving weather record for {}", weatherRecord.getLocation());

        // Generate ID if not present
        final WeatherRecord recordToSave;
        if (weatherRecord.getId() == null) {
            recordToSave = WeatherRecord.builder()
                    .id(String.valueOf(idGenerator.getAndIncrement()))
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
        } else {
            recordToSave = weatherRecord;
        }

        // Store in memory map organized by zip
        String zip = recordToSave.getLocation().getZipCode();

        log.debug("Zip {}", zip);
        log.debug("Zip map {}", zipWeatherMap);

        List<WeatherRecord> zipRecords = zipWeatherMap.computeIfAbsent(zip, k -> new ArrayList<>());

        // Remove any existing record with same ID
        zipRecords.removeIf(record -> record.getId().equals(recordToSave.getId()));

        // Add and sort by fetchedAt time (most recent first)
        zipRecords.add(recordToSave);
        zipRecords.sort(Comparator.comparing(WeatherRecord::getFetchedAt).reversed());

        // Maintain reasonable size (keep most recent 100 records)
        if (zipRecords.size() > 100) {
            List<WeatherRecord> trimmed = zipRecords.subList(0, 100);
            zipWeatherMap.put(zip, new ArrayList<>(trimmed));
        }

        // Update cache for this zip
        updateCache(zip);

        return recordToSave;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the most recent weather record for a zip
     */
    @Override
    @Cacheable(value = CacheConfig.WEATHER_CACHE, key = "'latest-' + #zip", unless = "#result ==null || #result.isEmpty()")
    public Optional<WeatherRecord> findMostRecentByZip(String zip) {
        log.debug("Finding most recent weather record for zip: {}", zip);

        List<WeatherRecord> records = zipWeatherMap.get(zip);

        if (records == null || records.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(records.getFirst());
    }

    /**
     * {@inheritDoc}
     *
     * Gets weather records for a zip within a date range
     */
    @Override
    public List<WeatherRecord> findByLocationZipAndFetchedAtBetween(String zip, LocalDateTime start, LocalDateTime end) {
        log.debug("Finding historical weather for zip: {} between {} and {}", zip, start, end);

        List<WeatherRecord> records = zipWeatherMap.get(zip);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        // Find records within the date range
        return records.stream()
                .filter(record -> !record.getFetchedAt().isBefore(start) && !record.getFetchedAt().isAfter(end))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the most recent n weather records for a zip
     */
    @Override
    @Cacheable(value = CacheConfig.WEATHER_CACHE, key = "'recent-' + #zip + '-' + #limit", unless = "#result.isEmpty()")
    public List<WeatherRecord> findRecentByZip(String zip, int limit) {
        log.debug("Finding {} most recent weather records for zip: {}", limit, zip);

        List<WeatherRecord> records = zipWeatherMap.get(zip);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        // Return most recent records up to limit
        return records.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * Clears all cached weather data
     */
    @Override
    @CacheEvict(value = CacheConfig.WEATHER_CACHE, allEntries = true)
    public void clearCache() {
        log.info("Clearing all weather caches");
        zipWeatherMap.clear();
    }

    /**
     * {@inheritDoc}
     *
     * Clears cached data for a specific zip
     */
    @Override
    @CacheEvict(value = CacheConfig.WEATHER_CACHE, allEntries = true)
    public void clearCacheForZip(String zip) {
        log.info("Clearing cache for zip: {}", zip);
        zipWeatherMap.remove(zip);

        // Manually evict specific cache entries
        Cache cache = cacheManager.getCache(CacheConfig.WEATHER_CACHE);
        if (cache != null) {
            cache.evict("latest-" + zip);
            cache.evict("recent-" + zip + "-10"); // Common limit
            cache.evict("recent-" + zip + "-5");  // Common limit
        }
    }

    /**
     * Update the cache for a zip
     */
    private void updateCache(String zip) {
        Cache cache = cacheManager.getCache(CacheConfig.WEATHER_CACHE);
        if (cache != null) {
            List<WeatherRecord> records = zipWeatherMap.get(zip);
            if (records != null && !records.isEmpty()) {
                // Update the latest record cache
                cache.put("latest-" + zip, Optional.of(records.getFirst()));

                // Update common recent records caches
                cache.put("recent-" + zip + "-10", records.stream().limit(10).collect(Collectors.toList()));
                cache.put("recent-" + zip + "-5", records.stream().limit(5).collect(Collectors.toList()));
            }
        }
    }
}