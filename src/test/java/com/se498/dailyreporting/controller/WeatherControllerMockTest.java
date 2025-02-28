package com.se498.dailyreporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se498.dailyreporting.domain.bo.*;
import com.se498.dailyreporting.dto.WeatherMapper;
import com.se498.dailyreporting.dto.WeatherRequest;
import com.se498.dailyreporting.dto.WeatherResponse;
import com.se498.dailyreporting.service.WeatherReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(WeatherController.class)
@DisplayName("Weather Controller Tests with Authentication")
class WeatherControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherReportingService weatherService;

    @MockBean
    private WeatherMapper weatherMapper;

    private WeatherRecord testWeatherRecord;
    private WeatherResponse testResponse;
    private Location cityLocation;
    private Location zipLocation;
    private List<String> alerts;

    // Authentication credentials
    private static final String USERNAME = "sergey";
    private static final String PASSWORD = "chapman";
    private static final String AUTH_HEADER = "Basic " +
            Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());

    @BeforeEach
    void setUp() {
        // Create test locations
        cityLocation = new Location("Seattle", "US", "WA");
        zipLocation = Location.fromZipCode("98101", "US");

        // Create test weather record
        testWeatherRecord = createTestWeatherRecord(false);

        // Create test weather response
        testResponse = createTestWeatherResponse();

        // Create test alerts
        alerts = Arrays.asList(
                "WIND ADVISORY: Wind speeds above 30 mph",
                "COLD ADVISORY: Temperature below 20°F"
        );

        // Setup mapper
        when(weatherMapper.toResponseDto(any(WeatherRecord.class))).thenReturn(testResponse);
        when(weatherMapper.toLocation(any(WeatherRequest.class))).thenAnswer(invocation -> {
            WeatherRequest req = invocation.getArgument(0);
            if (req.getZipCode() != null && !req.getZipCode().isEmpty()) {
                return Location.fromZipCode(req.getZipCode(), req.getCountry());
            }
            return new Location(req.getCity(), req.getCountry(), req.getStateOrProvince());
        });
    }

    /**
     * Helper method for authenticated GET requests
     */
    private MockHttpServletRequestBuilder authenticatedGet(String url) {
        return get(url)
                .header("Authorization", AUTH_HEADER);
    }

    /**
     * Helper method for authenticated POST requests
     */
    private MockHttpServletRequestBuilder authenticatedPost(String url) {
        return post(url)
                .header("Authorization", AUTH_HEADER);
    }

    @Nested
    @DisplayName("GET /weather/current tests")
    class GetCurrentWeatherTests {

        @Test
        @DisplayName("Should return weather data when city provided")
        void shouldReturnWeatherDataWhenCityProvided() throws Exception {
            // Arrange
            when(weatherService.getCurrentWeather(any(Location.class))).thenReturn(testWeatherRecord);

            // Act & Assert
            mockMvc.perform(authenticatedGet("/weather/current")
                            .param("city", "Seattle")
                            .param("country", "US")
                            .param("stateOrProvince", "WA")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.city", is("Seattle")))
                    .andExpect(jsonPath("$.country", is("US")))
                    .andExpect(jsonPath("$.stateOrProvince", is("WA")))
                    .andExpect(jsonPath("$.temperatureF", is(72.5)))
                    .andExpect(jsonPath("$.temperatureC", is(22.5)))
                    .andExpect(jsonPath("$.condition", is("Partly Cloudy")))
                    .andExpect(jsonPath("$.hasAlerts").doesNotExist());

            verify(weatherService).getCurrentWeather(any(Location.class));
            verify(weatherMapper).toResponseDto(testWeatherRecord);
        }

        @Test
        @DisplayName("Should return 401 when authentication missing")
        void shouldReturn401WhenAuthenticationMissing() throws Exception {
            // Test without authentication header
            mockMvc.perform(get("/weather/current")
                            .param("city", "Seattle"))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(weatherService);
        }

        @Test
        @DisplayName("Should return weather data when zip provided")
        void shouldReturnWeatherDataWhenZipProvided() throws Exception {
            // Arrange
            when(weatherService.getCurrentWeather(any(Location.class))).thenReturn(testWeatherRecord);

            // Act & Assert
            mockMvc.perform(authenticatedGet("/weather/current")
                            .param("zip", "98101")
                            .param("country", "US")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.temperatureF", is(72.5)));

            verify(weatherService).getCurrentWeather(any(Location.class));
        }

        @Test
        @DisplayName("Should include alerts when requested")
        void shouldIncludeAlertsWhenRequested() throws Exception {
            // Arrange
            when(weatherService.getCurrentWeather(any(Location.class))).thenReturn(testWeatherRecord);
            when(weatherService.analyzeForAlerts(testWeatherRecord)).thenReturn(alerts);

            // Act & Assert
            mockMvc.perform(authenticatedGet("/weather/current")
                            .param("city", "Seattle")
                            .param("includeAlerts", "true")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasAlerts", is(true)))
                    .andExpect(jsonPath("$.alerts", hasSize(2)))
                    .andExpect(jsonPath("$.alerts[0]", containsString("WIND ADVISORY")));

            verify(weatherService).getCurrentWeather(any(Location.class));
            verify(weatherService).analyzeForAlerts(testWeatherRecord);
        }
    }

    @Nested
    @DisplayName("GET /weather/current/zip/{zipCode} tests")
    class GetCurrentWeatherByZipTests {

        @ParameterizedTest
        @ValueSource(strings = {"90210", "10001", "98101", "60601", "02108"})
        @DisplayName("Should return weather data for valid US zip codes")
        void shouldReturnWeatherDataForValidUSZipCodes(String zipCode) throws Exception {
            // Arrange
            Location zipLoc = Location.fromZipCode(zipCode, "US");
            when(weatherService.getCurrentWeather(any(Location.class))).thenReturn(testWeatherRecord);

            // Act & Assert
            mockMvc.perform(authenticatedGet("/weather/current/zip/" + zipCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.temperatureF", is(72.5)));

            verify(weatherService).getCurrentWeather(any(Location.class));
        }


    }

    @Nested
    @DisplayName("GET /weather/history tests")
    class GetHistoricalWeatherTests {

        @Test
        @DisplayName("Should return historical weather data")
        void shouldReturnHistoricalWeatherData() throws Exception {
            // Arrange
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            List<WeatherRecord> historicalRecords = Arrays.asList(
                    createTestWeatherRecord(false),
                    createTestWeatherRecord(false)
            );

            List<WeatherResponse> historicalResponses = Arrays.asList(
                    testResponse,
                    testResponse
            );

            when(weatherService.getHistoricalWeather(any(Location.class), eq(start), eq(end)))
                    .thenReturn(historicalRecords);

            // Act & Assert
            mockMvc.perform(authenticatedGet("/weather/history")
                            .param("city", "Seattle")
                            .param("start", start.toString())
                            .param("end", end.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(weatherService).getHistoricalWeather(any(Location.class), eq(start), eq(end));
        }

        @Test
        @DisplayName("Should return 404 when no historical data found")
        void shouldReturn404WhenNoHistoricalDataFound() throws Exception {
            // Arrange
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            when(weatherService.getHistoricalWeather(any(Location.class), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(authenticatedGet("/weather/history")
                            .param("city", "Seattle")
                            .param("start", start.toString())
                            .param("end", end.toString()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /weather/alerts tests")
    class GetWeatherAlertsTests {

        @Test
        @DisplayName("Should return alerts for location")
        void shouldReturnAlertsForLocation() throws Exception {
            // Arrange
            when(weatherService.getCurrentWeather(any(Location.class))).thenReturn(testWeatherRecord);
            when(weatherService.analyzeForAlerts(testWeatherRecord)).thenReturn(alerts);

            // Act & Assert
            mockMvc.perform(authenticatedGet("/weather/alerts")
                            .param("city", "Seattle"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.location", containsString("Seattle")))
                    .andExpect(jsonPath("$.alertCount", is(2)))
                    .andExpect(jsonPath("$.alerts", hasSize(2)))
                    .andExpect(jsonPath("$.alerts[0]", containsString("WIND ADVISORY")));

            verify(weatherService).getCurrentWeather(any(Location.class));
            verify(weatherService).analyzeForAlerts(testWeatherRecord);
        }
    }

    @Nested
    @DisplayName("Temperature conversion tests")
    class TemperatureConversionTests {

        @ParameterizedTest
        @CsvSource({
                "32.0, 0.0",
                "212.0, 100.0",
                "98.6, 37.0",
                "0.0, -17.78"
        })
        @DisplayName("Should convert F to C correctly")
        void shouldConvertFToCCorrectly(double fahrenheit, double expectedCelsius) throws Exception {
            // Arrange
            when(weatherService.convertFahrenheitToCelsius(fahrenheit)).thenReturn(expectedCelsius);

            // Act & Assert
            mockMvc.perform(authenticatedGet("/weather/convert/ftoc")
                            .param("temperature", String.valueOf(fahrenheit)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(String.valueOf(expectedCelsius)));

            verify(weatherService).convertFahrenheitToCelsius(fahrenheit);
        }

        @ParameterizedTest
        @CsvSource({
                "0.0, 32.0",
                "100.0, 212.0",
                "37.0, 98.6",
                "-40.0, -40.0"
        })
        @DisplayName("Should convert C to F correctly")
        void shouldConvertCToFCorrectly(double celsius, double expectedFahrenheit) throws Exception {
            // Arrange
            when(weatherService.convertCelsiusToFahrenheit(celsius)).thenReturn(expectedFahrenheit);

            // Act & Assert
            mockMvc.perform(authenticatedGet("/weather/convert/ctof")
                            .param("temperature", String.valueOf(celsius)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(String.valueOf(expectedFahrenheit)));

            verify(weatherService).convertCelsiusToFahrenheit(celsius);
        }
    }

    @Test
    @DisplayName("GET /weather/status should return operational status")
    void getStatusShouldReturnOperationalStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(authenticatedGet("/weather/status"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("operational")));

        verifyNoInteractions(weatherService);
    }

    /**
     * Helper method to create test weather record
     */
    private WeatherRecord createTestWeatherRecord(boolean severe) {
        // Create value objects
        Temperature temperature = Temperature.fromFahrenheit(severe ? 15.0 : 72.5);
        Humidity humidity = Humidity.of(severe ? 90 : 45);
        WindSpeed windSpeed = WindSpeed.fromMph(severe ? 35.0 : 12.0);
        WeatherCondition condition = new WeatherCondition(
                severe ? "Heavy Snowstorm" : "Partly Cloudy",
                severe ? "13d" : "03d"
        );

        // Create and return weather record
        return WeatherRecord.builder()
                .id("test-record-1")
                .location(cityLocation)
                .temperature(temperature)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .condition(condition)
                .pressureInHg(29.92)
                .visibilityMiles(severe ? 0.5 : 10.0)
                .uvIndex(severe ? 0 : 5)
                .recordedAt(LocalDateTime.now().minusMinutes(15))
                .fetchedAt(LocalDateTime.now().minusMinutes(5))
                .dataSource("Test Provider")
                .build();
    }

    /**
     * Helper method to create test weather response
     */
    private WeatherResponse createTestWeatherResponse() {
        return WeatherResponse.builder()
                .id("test-response-1")
                .city("Seattle")
                .country("US")
                .stateOrProvince("WA")
                .temperatureF(72.5)
                .temperatureC(22.5)
                .feelsLikeF(74.0)
                .feelsLikeC(23.3)
                .humidity(45)
                .windSpeedMph(12.0)
                .windSpeedKph(19.3)
                .condition("Partly Cloudy")
                .conditionIcon("03d")
                .isClear(false)
                .isRainy(false)
                .isSnowy(false)
                .pressureInHg(29.92)
                .visibilityMiles(10.0)
                .uvIndex(5)
                .severeWeather(false)
                .favorableForOutdoor(true)
                .recordedAt(LocalDateTime.now().minusMinutes(15))
                .fetchedAt(LocalDateTime.now().minusMinutes(5))
                .dataSource("Test Provider")
                .build();
    }
}
