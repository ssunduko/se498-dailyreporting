package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.WeatherRecord;
import com.se498.dailyreporting.repository.WeatherRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import com.se498.dailyreporting.exception.WeatherApiException;
import com.se498.dailyreporting.domain.bo.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Weather Reporting Service Tests")
class WeatherServiceMockTest {

    @Mock
    private WeatherRecordRepository weatherRecordRepository;

    @Mock
    private WeatherApiClient weatherApiClient;

    @InjectMocks
    private WeatherReportingServiceImpl weatherService;

    private Location testLocation;
    private WeatherRecord freshWeatherRecord;
    private WeatherRecord staleWeatherRecord;

    @BeforeEach
    void setUp() {
        // Set configuration values
        ReflectionTestUtils.setField(weatherService, "maxCacheAgeMinutes", 30);
        ReflectionTestUtils.setField(weatherService, "alertsEnabled", true);

        // Create test location
        testLocation = new Location("92618", "US", "TestState", "92618");

        // Create fresh and stale weather records
        freshWeatherRecord = createWeatherRecord(LocalDateTime.now().minusMinutes(10), false);
        staleWeatherRecord = createWeatherRecord(LocalDateTime.now().minusMinutes(60), false);
    }

    @Nested
    @DisplayName("getCurrentWeather method tests")
    class GetCurrentWeatherTests {

        @Test
        @DisplayName("Should return cached weather when fresh data is available")
        void shouldReturnCachedWeatherWhenFreshDataIsAvailable() {
            // Arrange
            when(weatherRecordRepository.findMostRecentByZip("92618"))
                    .thenReturn(Optional.of(freshWeatherRecord));

            // Act
            WeatherRecord result = weatherService.getCurrentWeather(testLocation);

            // Assert
            assertNotNull(result);
            assertEquals(freshWeatherRecord, result);
            verify(weatherRecordRepository).findMostRecentByZip("92618");
            verifyNoInteractions(weatherApiClient);
        }

        @Test
        @DisplayName("Should fetch new weather when cache is empty")
        void shouldFetchNewWeatherWhenCacheIsEmpty() {
            // Arrange
            when(weatherRecordRepository.findMostRecentByZip("92618"))
                    .thenReturn(Optional.empty());
            when(weatherApiClient.fetchWeatherData(testLocation))
                    .thenReturn(freshWeatherRecord);
            when(weatherRecordRepository.save(freshWeatherRecord))
                    .thenReturn(freshWeatherRecord);

            // Act
            WeatherRecord result = weatherService.getCurrentWeather(testLocation);

            // Assert
            assertNotNull(result);
            assertEquals(freshWeatherRecord, result);
            verify(weatherRecordRepository).findMostRecentByZip("92618");
            verify(weatherApiClient).fetchWeatherData(testLocation);
            verify(weatherRecordRepository).save(freshWeatherRecord);
        }

        @Test
        @DisplayName("Should fetch new weather when cache is stale")
        void shouldFetchNewWeatherWhenCacheIsStale() {
            // Arrange
            when(weatherRecordRepository.findMostRecentByZip("92618"))
                    .thenReturn(Optional.of(staleWeatherRecord));
            when(weatherApiClient.fetchWeatherData(testLocation))
                    .thenReturn(freshWeatherRecord);
            when(weatherRecordRepository.save(freshWeatherRecord))
                    .thenReturn(freshWeatherRecord);

            // Act
            WeatherRecord result = weatherService.getCurrentWeather(testLocation);

            // Assert
            assertNotNull(result);
            assertEquals(freshWeatherRecord, result);
            verify(weatherRecordRepository).findMostRecentByZip("92618");
            verify(weatherApiClient).fetchWeatherData(testLocation);
            verify(weatherRecordRepository).save(freshWeatherRecord);
        }

        @Test
        @DisplayName("Should throw exception when location is invalid")
        void shouldThrowExceptionWhenLocationIsInvalid() {
            // Arrange
            Location invalidLocation = new Location(null, null);

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> weatherService.getCurrentWeather(invalidLocation));
            assertEquals("Invalid location provided", exception.getMessage());
            verifyNoInteractions(weatherRecordRepository, weatherApiClient);
        }

        @Test
        @DisplayName("Should propagate API exception when external service fails")
        void shouldPropagateApiExceptionWhenExternalServiceFails() {
            // Arrange
            WeatherApiException apiException = new WeatherApiException("Service unavailable", "TestProvider");
            when(weatherRecordRepository.findMostRecentByZip(anyString()))
                    .thenReturn(Optional.empty());
            when(weatherApiClient.fetchWeatherData(any(Location.class)))
                    .thenThrow(apiException);

            // Act & Assert
            WeatherApiException thrown = assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather(testLocation));
            assertEquals("Service unavailable", thrown.getMessage());
            assertEquals("TestProvider", thrown.getProvider());

            verify(weatherRecordRepository).findMostRecentByZip("92618");
            verify(weatherApiClient).fetchWeatherData(testLocation);
            verifyNoMoreInteractions(weatherRecordRepository);
        }
    }

    @Nested
    @DisplayName("getHistoricalWeather method tests")
    class GetHistoricalWeatherTests {

        @Test
        @DisplayName("Should return historical weather data when valid parameters")
        void shouldReturnHistoricalWeatherDataWhenValidParameters() {
            // Arrange
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();
            List<WeatherRecord> historicalRecords = Arrays.asList(
                    createWeatherRecord(LocalDateTime.now().minusDays(6), false),
                    createWeatherRecord(LocalDateTime.now().minusDays(3), false)
            );

            when(weatherRecordRepository.findByLocationZipAndFetchedAtBetween("92618", start, end))
                    .thenReturn(historicalRecords);

            // Act
            List<WeatherRecord> result = weatherService.getHistoricalWeather(testLocation, start, end);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(weatherRecordRepository).findByLocationZipAndFetchedAtBetween("92618", start, end);
        }

        @Test
        @DisplayName("Should throw exception when location is invalid")
        void shouldThrowExceptionWhenLocationIsInvalid() {
            // Arrange
            Location invalidLocation = new Location(null, null);
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> weatherService.getHistoricalWeather(invalidLocation, start, end));
            assertEquals("Invalid location provided", exception.getMessage());
            verifyNoInteractions(weatherRecordRepository);
        }

        @Test
        @DisplayName("Should throw exception when date range is invalid")
        void shouldThrowExceptionWhenDateRangeIsInvalid() {
            // Arrange
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().minusDays(7); // end before start

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> weatherService.getHistoricalWeather(testLocation, start, end));
            assertEquals("Start date must be before end date", exception.getMessage());
            verifyNoInteractions(weatherRecordRepository);
        }

        @Test
        @DisplayName("Should return empty list when no data available")
        void shouldReturnEmptyListWhenNoDataAvailable() {
            // Arrange
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            when(weatherRecordRepository.findByLocationZipAndFetchedAtBetween("92618", start, end))
                    .thenReturn(Collections.emptyList());

            // Act
            List<WeatherRecord> result = weatherService.getHistoricalWeather(testLocation, start, end);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(weatherRecordRepository).findByLocationZipAndFetchedAtBetween("92618", start, end);
        }
    }

    @Nested
    @DisplayName("getRecentWeather method tests")
    class GetRecentWeatherTests {

        @ParameterizedTest
        @ValueSource(ints = {-5, 0, -100})
        @DisplayName("Should throw exception when limit is non-positive")
        void shouldThrowExceptionWhenLimitIsNonPositive(int invalidLimit) {
            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> weatherService.getRecentWeather(testLocation, invalidLimit));
            assertEquals("Limit must be positive", exception.getMessage());
            verifyNoInteractions(weatherRecordRepository);
        }
    }

    @Nested
    @DisplayName("analyzeForAlerts method tests")
    class AnalyzeForAlertsTests {

        @Test
        @DisplayName("Should return alerts when severe conditions exist")
        void shouldReturnAlertsWhenSevereConditionsExist() {
            // Arrange
            WeatherRecord severeRecord = createWeatherRecord(LocalDateTime.now(), true);

            // Act
            List<String> alerts = weatherService.analyzeForAlerts(severeRecord);

            // Assert
            assertNotNull(alerts);
            assertFalse(alerts.isEmpty());
            assertTrue(alerts.stream().anyMatch(alert -> alert.contains("SEVERE WEATHER ALERT")));
        }

        @Test
        @DisplayName("Should return empty list when no severe conditions")
        void shouldReturnEmptyListWhenNoSevereConditions() {
            // Arrange
            WeatherRecord normalRecord = createWeatherRecord(LocalDateTime.now(), false);

            // Act
            List<String> alerts = weatherService.analyzeForAlerts(normalRecord);

            // Assert
            assertNotNull(alerts);
            assertTrue(alerts.isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when alerts disabled")
        void shouldReturnEmptyListWhenAlertsDisabled() {
            // Arrange
            WeatherRecord severeRecord = createWeatherRecord(LocalDateTime.now(), true);
            ReflectionTestUtils.setField(weatherService, "alertsEnabled", false);

            // Act
            List<String> alerts = weatherService.analyzeForAlerts(severeRecord);

            // Assert
            assertNotNull(alerts);
            assertTrue(alerts.isEmpty());
        }
    }

    @Nested
    @DisplayName("Temperature conversion tests")
    class TemperatureConversionTests {

        @ParameterizedTest
        @CsvSource({
                "32.0, 0.0",
                "212.0, 100.0",
                "0.0, -17.77777777777778",
                "98.6, 37.0"
        })
        @DisplayName("Should convert Fahrenheit to Celsius correctly")
        void shouldConvertFahrenheitToCelsiusCorrectly(double fahrenheit, double expectedCelsius) {
            // Act
            double result = weatherService.convertFahrenheitToCelsius(fahrenheit);

            // Assert
            assertEquals(expectedCelsius, result, 0.001);
        }

        @ParameterizedTest
        @CsvSource({
                "0.0, 32.0",
                "100.0, 212.0",
                "-40.0, -40.0",
                "37.0, 98.6"
        })
        @DisplayName("Should convert Celsius to Fahrenheit correctly")
        void shouldConvertCelsiusToFahrenheitCorrectly(double celsius, double expectedFahrenheit) {
            // Act
            double result = weatherService.convertCelsiusToFahrenheit(celsius);

            // Assert
            assertEquals(expectedFahrenheit, result, 0.001);
        }
    }

    /**
     * Helper method to create weather record for testing
     */
    private WeatherRecord createWeatherRecord(LocalDateTime fetchTime, boolean isSevere) {
        Temperature temperature = Temperature.fromFahrenheit(isSevere ? 105.0 : 72.0);
        Humidity humidity = Humidity.of(isSevere ? 90 : 50);
        WindSpeed windSpeed = WindSpeed.fromMph(isSevere ? 35.0 : 10.0);
        WeatherCondition condition = new WeatherCondition(
                isSevere ? "Severe Thunderstorm" : "Partly Cloudy",
                isSevere ? "11d" : "02d"
        );

        return WeatherRecord.builder()
                .id("test-id-" + (isSevere ? "severe" : "normal"))
                .location(testLocation)
                .temperature(temperature)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .condition(condition)
                .pressureInHg(isSevere ? 28.5 : 30.1)
                .visibilityMiles(isSevere ? 0.5 : 10.0)
                .uvIndex(isSevere ? 11 : 4)
                .recordedAt(fetchTime.minusMinutes(5))
                .fetchedAt(fetchTime)
                .dataSource("Test Provider")
                .build();
    }
}
