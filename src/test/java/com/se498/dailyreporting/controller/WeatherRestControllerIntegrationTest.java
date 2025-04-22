package com.se498.dailyreporting.controller;


import com.se498.dailyreporting.TestDailyReportingApplication;
import com.se498.dailyreporting.domain.bo.*;
import com.se498.dailyreporting.dto.WeatherMapper;
import com.se498.dailyreporting.dto.WeatherRequest;
import com.se498.dailyreporting.dto.WeatherResponse;
import com.se498.dailyreporting.service.WeatherReportingService;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.instancio.Select.*;
import static org.instancio.generators.Generators.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestDailyReportingApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@DisplayName("Weather Controller Tests with Authentication")
class WeatherRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherReportingService weatherService;

    @MockBean
    private WeatherMapper weatherMapper;

    // Test models
    private Model<WeatherRecord> normalWeatherModel;
    private Model<WeatherRecord> severeWeatherModel;
    private Model<WeatherResponse> weatherResponseModel;

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
        // Use a fixed seed for deterministic test data generation
        final long SEED = 123456L;

        // Create location models using Instancio
        Model<Location> cityLocationModel = Instancio.of(Location.class)
                .set(field(Location::getCity), "Seattle")
                .set(field(Location::getCountry), "US")
                .set(field(Location::getStateOrProvince), "WA")
                .toModel();

        Model<Location> zipLocationModel = Instancio.of(Location.class)
                .set(field(Location::getZipCode), "98101")
                .set(field(Location::getCountry), "US")
                .set(field(Location::getCity), null)
                .toModel();

        cityLocation = Instancio.of(cityLocationModel)
                .withSeed(SEED)
                .create();

        zipLocation = Instancio.of(zipLocationModel)
                .withSeed(SEED)
                .create();

        // Create models for value objects
        Model<Temperature> normalTempModel = Instancio.of(Temperature.class)
                .generate(field(Temperature::getFahrenheit), gen -> gen.doubles().range(72.5, 100.0))
                .generate(field(Temperature::getCelsius), gen -> gen.doubles().range(22.5, 50.0))
                .toModel();

        Model<Temperature> coldTempModel = Instancio.of(Temperature.class)
                .generate(field(Temperature::getFahrenheit), gen -> gen.doubles().range(15.0, 20.0))
                .generate(field(Temperature::getCelsius), gen -> gen.doubles().range(-9.4, 1.0))
                .toModel();

        Model<Humidity> normalHumidityModel = Instancio.of(Humidity.class)
                .set(field(Humidity::getPercentage), 45)
                .toModel();

        Model<Humidity> highHumidityModel = Instancio.of(Humidity.class)
                .set(field(Humidity::getPercentage), 90)
                .toModel();

        Model<WindSpeed> normalWindModel = Instancio.of(WindSpeed.class)
                .set(field(WindSpeed::getMph), 12.0)
                .toModel();

        Model<WindSpeed> highWindModel = Instancio.of(WindSpeed.class)
                .set(field(WindSpeed::getMph), 35.0)
                .toModel();

        Model<WeatherCondition> clearConditionModel = Instancio.of(WeatherCondition.class)
                .set(field(WeatherCondition::getDescription), "Partly Cloudy")
                .set(field(WeatherCondition::getIconCode), "03d")
                .toModel();

        Model<WeatherCondition> severeConditionModel = Instancio.of(WeatherCondition.class)
                .set(field(WeatherCondition::getDescription), "Heavy Snowstorm")
                .set(field(WeatherCondition::getIconCode), "13d")
                .toModel();

        // Create a generator for recent timestamps
        LocalDateTime baseTime = LocalDateTime.now();
        Model<LocalDateTime> recentTimeModel = Instancio.of(LocalDateTime.class)
                .supply(Select.root(), () -> baseTime.minusMinutes(15))
                .toModel();

        Model<LocalDateTime> veryRecentTimeModel = Instancio.of(LocalDateTime.class)
                .supply(Select.root(), () -> baseTime.minusMinutes(5))
                .toModel();

        // Create normal weather record model
        normalWeatherModel = Instancio.of(WeatherRecord.class)
                .set(field(WeatherRecord::getId), "test-record-1")
                .set(field(WeatherRecord::getLocation), cityLocation)
                .supply(field(WeatherRecord::getTemperature), () -> Instancio.create(normalTempModel))
                .supply(field(WeatherRecord::getHumidity), () -> Instancio.create(normalHumidityModel))
                .supply(field(WeatherRecord::getWindSpeed), () -> Instancio.create(normalWindModel))
                .supply(field(WeatherRecord::getCondition), () -> Instancio.create(clearConditionModel))
                .set(field(WeatherRecord::getPressureInHg), 29.92)
                .set(field(WeatherRecord::getVisibilityMiles), 10.0)
                .set(field(WeatherRecord::getUvIndex), 5)
                .supply(field(WeatherRecord::getRecordedAt), () -> Instancio.create(recentTimeModel))
                .supply(field(WeatherRecord::getFetchedAt), () -> Instancio.create(veryRecentTimeModel))
                .set(field(WeatherRecord::getDataSource), "Test Provider")
                .toModel();

        // Create severe weather record model
        severeWeatherModel = Instancio.of(WeatherRecord.class)
                .set(field(WeatherRecord::getId), "test-record-2")
                .set(field(WeatherRecord::getLocation), cityLocation)
                .supply(field(WeatherRecord::getTemperature), () -> Instancio.create(coldTempModel))
                .supply(field(WeatherRecord::getHumidity), () -> Instancio.create(highHumidityModel))
                .supply(field(WeatherRecord::getWindSpeed), () -> Instancio.create(highWindModel))
                .supply(field(WeatherRecord::getCondition), () -> Instancio.create(severeConditionModel))
                .set(field(WeatherRecord::getPressureInHg), 28.5)
                .set(field(WeatherRecord::getVisibilityMiles), 0.5)
                .set(field(WeatherRecord::getUvIndex), 0)
                .supply(field(WeatherRecord::getRecordedAt), () -> Instancio.create(recentTimeModel))
                .supply(field(WeatherRecord::getFetchedAt), () -> Instancio.create(veryRecentTimeModel))
                .set(field(WeatherRecord::getDataSource), "Test Provider")
                .toModel();

        // Create weather response model
        weatherResponseModel = Instancio.of(WeatherResponse.class)
                .set(field("id"), "test-response-1")
                .set(field("city"), "Seattle")
                .set(field("country"), "US")
                .set(field("stateOrProvince"), "WA")
                .set(field("temperatureF"), 72.5)
                .set(field("temperatureC"), 22.5)
                .set(field("feelsLikeF"), 74.0)
                .set(field("feelsLikeC"), 23.3)
                .set(field("humidity"), 45)
                .set(field("windSpeedMph"), 12.0)
                .set(field("windSpeedKph"), 19.3)
                .set(field("condition"), "Partly Cloudy")
                .set(field("conditionIcon"), "03d")
                .set(field("isClear"), false)
                .set(field("isRainy"), false)
                .set(field("isSnowy"), false)
                .set(field("pressureInHg"), 29.92)
                .set(field("visibilityMiles"), 10.0)
                .set(field("uvIndex"), 5)
                .set(field("severeWeather"), false)
                .set(field("favorableForOutdoor"), true)
                .supply(field("recordedAt"), () -> Instancio.create(recentTimeModel))
                .supply(field("fetchedAt"), () -> Instancio.create(veryRecentTimeModel))
                .set(field("dataSource"), "Test Provider")
                .toModel();

        // Create test instances with seed for reproducibility
        testWeatherRecord = Instancio.of(normalWeatherModel)
                .withSeed(SEED)
                .create();

        testResponse = Instancio.of(weatherResponseModel)
                .withSeed(SEED)
                .create();

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

    // These helper methods are no longer needed as we're using Instancio models directly

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

            // Generate a list of weather records using Instancio's ofList method
            List<WeatherRecord> historicalRecords = Instancio.ofList(normalWeatherModel)
                    .size(2)
                    .generate(field(WeatherRecord::getId),
                            gen -> gen.oneOf("test-record-3", "test-record-4"))
                    .create();

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
}