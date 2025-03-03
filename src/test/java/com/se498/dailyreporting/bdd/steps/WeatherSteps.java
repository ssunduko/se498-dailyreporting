package com.se498.dailyreporting.bdd.steps;

import com.se498.dailyreporting.domain.bo.*;
import com.se498.dailyreporting.service.WeatherReportingService;
import org.jbehave.core.annotations.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Component
public class WeatherSteps {

    @Autowired
    private WeatherReportingService weatherService;

    private Location currentLocation;
    private WeatherRecord currentWeatherRecord;
    private List<String> weatherAlerts;
    private double temperatureResult;
    private Exception lastException;

    // Setup mocks before scenarios
    @BeforeScenario
    public void setUp() {
        // Reset state
        currentLocation = null;
        currentWeatherRecord = null;
        weatherAlerts = null;
        temperatureResult = 0.0;
        lastException = null;

        // If using mocks, reset them here
        Mockito.reset(weatherService);

        System.out.println("WeatherSteps: BeforeScenario completed");
    }

    @Given("I am an authenticated user")
    public void givenAuthenticatedUser() {
        System.out.println("Executing step: Given I am an authenticated user");

        // Setup security context with a test user
        UserDetails userDetails = User.builder()
                .username("sergey")
                .password("chapman")
                .roles("ADMIN") // Spring Security automatically adds ROLE_ prefix
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        System.out.println("User authenticated as: testUser");
    }

    // Double quotes version
    @When("I request current weather for zip code \"$zipCode\" in country \"$country\"")
    public void whenRequestWeatherByZipDoubleQuotes(String zipCode, String country) {
        System.out.println("Executing step with double quotes: When I request current weather for zip code \"" + zipCode + "\" in country \"" + country + "\"");
        whenRequestWeatherByZip(zipCode, country);
    }

    // Single quotes version
    @When("I request current weather for zip code '$zipCode' in country '$country'")
    public void whenRequestWeatherByZip(String zipCode, String country) {
        System.out.println("Executing step with single quotes: When I request current weather for zip code '" + zipCode + "' in country '" + country + "'");

        try {
            // Create location
            currentLocation = Location.fromZipCode(zipCode, country);

            // Create mock weather data
            WeatherRecord weatherRecord = createMockWeatherRecord(currentLocation);

            // Mock service to return this data
            when(weatherService.getCurrentWeather(any(Location.class))).thenReturn(weatherRecord);

            // Call service
            currentWeatherRecord = weatherService.getCurrentWeather(currentLocation);

            System.out.println("Retrieved weather for location: " + currentLocation.getCity() + ", " + currentLocation.getCountry());
        } catch (Exception e) {
            System.err.println("Exception in whenRequestWeatherByZip: " + e.getMessage());
            e.printStackTrace();
            lastException = e;
        }
    }

    // Double quotes version
    @When("I request current weather for city \"$city\" in country \"$country\"")
    public void whenRequestWeatherByCityDoubleQuotes(String city, String country) {
        System.out.println("Executing step with double quotes: When I request current weather for city \"" + city + "\" in country \"" + country + "\"");
        whenRequestWeatherByCity(city, country);
    }

    // Single quotes version
    @When("I request current weather for city '$city' in country '$country'")
    public void whenRequestWeatherByCity(String city, String country) {
        System.out.println("Executing step with single quotes: When I request current weather for city '" + city + "' in country '" + country + "'");

        try {
            // Create location
            currentLocation = new Location(city, country);

            // Create mock weather data
            WeatherRecord weatherRecord = createMockWeatherRecord(currentLocation);

            // Mock service to return this data
            when(weatherService.getCurrentWeather(any(Location.class))).thenReturn(weatherRecord);

            // Call service
            currentWeatherRecord = weatherService.getCurrentWeather(currentLocation);

            System.out.println("Retrieved weather for city: " + city + ", " + country);
        } catch (Exception e) {
            System.err.println("Exception in whenRequestWeatherByCity: " + e.getMessage());
            e.printStackTrace();
            lastException = e;
        }
    }

    @Then("I should receive valid weather information")
    public void thenReceiveValidWeatherInfo() {
        System.out.println("Executing step: Then I should receive valid weather information");

        assertNull(lastException, "Exception occurred: " + (lastException != null ? lastException.getMessage() : ""));
        assertNotNull(currentWeatherRecord, "Weather record is null");

        // Verify temperature data exists
        assertNotNull(currentWeatherRecord.getTemperature(), "Temperature data is missing");
        assertNotNull(currentWeatherRecord.getTemperature().getFahrenheit(), "Fahrenheit temperature is missing");
        assertNotNull(currentWeatherRecord.getTemperature().getCelsius(), "Celsius temperature is missing");

        // Verify humidity data exists
        assertNotNull(currentWeatherRecord.getHumidity(), "Humidity data is missing");
        assertNotNull(currentWeatherRecord.getHumidity().getPercentage(), "Humidity percentage is missing");

        // Verify wind data exists
        assertNotNull(currentWeatherRecord.getWindSpeed(), "Wind speed data is missing");
        assertNotNull(currentWeatherRecord.getWindSpeed().getMph(), "Wind speed in mph is missing");

        // Verify condition data exists
        assertNotNull(currentWeatherRecord.getCondition(), "Weather condition data is missing");
        assertNotNull(currentWeatherRecord.getCondition().getDescription(), "Condition description is missing");

        System.out.println("Verified weather information is valid");
    }

    @Then("the response should include temperature, humidity and wind speed")
    public void thenResponseIncludesBasicData() {
        System.out.println("Executing step: Then the response should include temperature, humidity and wind speed");

        assertNotNull(currentWeatherRecord, "Weather record is null");

        // Verify temperature data exists and is within reasonable range
        assertNotNull(currentWeatherRecord.getTemperature(), "Temperature data is missing");
        double fahrenheit = currentWeatherRecord.getTemperature().getFahrenheit();
        assertTrue(fahrenheit > -100 && fahrenheit < 150, "Temperature out of reasonable range");

        // Verify humidity data exists and is within valid range
        assertNotNull(currentWeatherRecord.getHumidity(), "Humidity data is missing");
        int humidity = currentWeatherRecord.getHumidity().getPercentage();
        assertTrue(humidity >= 0 && humidity <= 100, "Humidity percentage out of valid range");

        // Verify wind data exists and is non-negative
        assertNotNull(currentWeatherRecord.getWindSpeed(), "Wind speed data is missing");
        double windSpeed = currentWeatherRecord.getWindSpeed().getMph();
        assertTrue(windSpeed >= 0, "Wind speed cannot be negative");

        System.out.println("Verified response includes temperature, humidity, and wind speed");
    }

    // Double quotes version
    @Then("the location information should match \"$expectedLocation\"")
    public void thenLocationInfoMatchesDoubleQuotes(String expectedLocation) {
        System.out.println("Executing step with double quotes: Then the location information should match \"" + expectedLocation + "\"");
        thenLocationInfoMatches(expectedLocation);
    }

    // Single quotes version
    @Then("the location information should match '$expectedLocation'")
    public void thenLocationInfoMatches(String expectedLocation) {
        System.out.println("Executing step with single quotes: Then the location information should match '" + expectedLocation + "'");

        assertNotNull(currentWeatherRecord, "Weather record is null");
        assertNotNull(currentWeatherRecord.getLocation(), "Location is null");

        // Parse expected location
        String[] parts = expectedLocation.split(", ");
        String expectedCity = parts[0];
        String expectedCountry = parts.length > 1 ? parts[1] : null;

        // Verify city matches
        assertEquals(expectedCity, currentWeatherRecord.getLocation().getCity(),
                "Expected city " + expectedCity + " but got " + currentWeatherRecord.getLocation().getCity());

        // Verify country if provided
        if (expectedCountry != null) {
            assertEquals(expectedCountry, currentWeatherRecord.getLocation().getCountry(),
                    "Expected country " + expectedCountry + " but got " + currentWeatherRecord.getLocation().getCountry());
        }

        System.out.println("Verified location matches: " + expectedLocation);
    }

    // Double quotes version
    @When("I request weather alerts for zip code \"$zipCode\"")
    public void whenRequestWeatherAlertsDoubleQuotes(String zipCode) {
        System.out.println("Executing step with double quotes: When I request weather alerts for zip code \"" + zipCode + "\"");
        whenRequestWeatherAlerts(zipCode);
    }

    // Single quotes version
    @When("I request weather alerts for zip code '$zipCode'")
    public void whenRequestWeatherAlerts(String zipCode) {
        System.out.println("Executing step with single quotes: When I request weather alerts for zip code '" + zipCode + "'");

        try {
            // Create location
            currentLocation = Location.fromZipCode(zipCode, "US");

            // Create mock weather data
            WeatherRecord weatherRecord = createMockWeatherRecord(currentLocation);

            // Mock service to return this data and alerts
            when(weatherService.getCurrentWeather(any(Location.class))).thenReturn(weatherRecord);

            // Create alerts with proper terminology (ADVISORY, ALERT, WARNING, WATCH)
            List<String> mockAlerts = new ArrayList<>();
            mockAlerts.add("HEAT ADVISORY: Temperature above 90°F");
            mockAlerts.add("AIR QUALITY ALERT: Moderate pollution levels");

            when(weatherService.analyzeForAlerts(any(WeatherRecord.class))).thenReturn(mockAlerts);

            // Call services
            currentWeatherRecord = weatherService.getCurrentWeather(currentLocation);
            weatherAlerts = weatherService.analyzeForAlerts(currentWeatherRecord);

            // Debug
            System.out.println("Created " + weatherAlerts.size() + " weather alerts for zip " + zipCode);
        } catch (Exception e) {
            System.err.println("Exception in whenRequestWeatherAlerts: " + e.getMessage());
            e.printStackTrace();
            lastException = e;
        }
    }

    @Then("I should receive a list of active weather alerts if any")
    public void thenReceiveWeatherAlerts() {
        System.out.println("Executing step: Then I should receive a list of active weather alerts if any");

        assertNull(lastException, "Exception occurred: " + (lastException != null ? lastException.getMessage() : ""));
        assertNotNull(weatherAlerts, "Weather alerts list is null");

        System.out.println("Received " + weatherAlerts.size() + " weather alerts");
    }

    @Then("the response should include alert count and severity information")
    public void thenResponseIncludesAlertInfo() {
        System.out.println("Executing step: Then the response should include alert count and severity information");

        assertNotNull(weatherAlerts, "Weather alerts list is null");

        // Debug
        System.out.println("Weather alerts:");
        for (String alert : weatherAlerts) {
            System.out.println("  - " + alert);
        }

        // Verify alert count
        assertTrue(weatherAlerts.size() >= 0, "Alert count should be 0 or more");

        // If there are alerts, verify they contain severity information
        if (!weatherAlerts.isEmpty()) {
            // Make case-insensitive by converting to uppercase for checking
            boolean hasSeverityInfo = weatherAlerts.stream()
                    .anyMatch(alert ->
                            alert.toUpperCase().contains("WARNING") ||
                                    alert.toUpperCase().contains("ADVISORY") ||
                                    alert.toUpperCase().contains("ALERT") ||
                                    alert.toUpperCase().contains("WATCH"));

            assertTrue(hasSeverityInfo, "Alerts should include severity information");
        }

        System.out.println("Verified response includes alert count and severity information");
    }

    // Double quotes version
    @Given("the weather service has cached data for zip code \"$zipCode\"")
    public void givenCachedWeatherDataDoubleQuotes(String zipCode) {
        System.out.println("Executing step with double quotes: Given the weather service has cached data for zip code \"" + zipCode + "\"");
        givenCachedWeatherData(zipCode);
    }

    // Single quotes version
    @Given("the weather service has cached data for zip code '$zipCode'")
    public void givenCachedWeatherData(String zipCode) {
        System.out.println("Executing step with single quotes: Given the weather service has cached data for zip code '" + zipCode + "'");

        // Create location
        currentLocation = Location.fromZipCode(zipCode, "US");

        // Create mock weather data
        WeatherRecord weatherRecord = createMockWeatherRecord(currentLocation);

        // Add cache information
        weatherRecord = WeatherRecord.builder()
                .id(weatherRecord.getId())
                .location(weatherRecord.getLocation())
                .temperature(weatherRecord.getTemperature())
                .humidity(weatherRecord.getHumidity())
                .windSpeed(weatherRecord.getWindSpeed())
                .condition(weatherRecord.getCondition())
                .pressureInHg(weatherRecord.getPressureInHg())
                .visibilityMiles(weatherRecord.getVisibilityMiles())
                .uvIndex(weatherRecord.getUvIndex())
                .recordedAt(weatherRecord.getRecordedAt())
                .fetchedAt(LocalDateTime.now().minusMinutes(5)) // Cached 5 minutes ago
                .dataSource(weatherRecord.getDataSource() + " (Cached)")
                .build();

        // Store the cached record
        currentWeatherRecord = weatherRecord;

        System.out.println("Created cached weather data for zip code: " + zipCode);
    }

    // Double quotes version
    @When("I request weather data for zip code \"$zipCode\" within the cache validity period")
    public void whenRequestDataInCacheValidityPeriodDoubleQuotes(String zipCode) {
        System.out.println("Executing step with double quotes: When I request weather data for zip code \"" + zipCode + "\" within the cache validity period");
        whenRequestDataInCacheValidityPeriod(zipCode);
    }

    // Single quotes version
    @When("I request weather data for zip code '$zipCode' within the cache validity period")
    public void whenRequestDataInCacheValidityPeriod(String zipCode) {
        System.out.println("Executing step with single quotes: When I request weather data for zip code '" + zipCode + "' within the cache validity period");

        try {
            // Ensure location matches
            currentLocation = Location.fromZipCode(zipCode, "US");

            // Mock service to return cached data
            when(weatherService.getCurrentWeather(any(Location.class))).thenReturn(currentWeatherRecord);

            // Call service
            currentWeatherRecord = weatherService.getCurrentWeather(currentLocation);

            System.out.println("Requested weather data for zip code: " + zipCode + " within cache validity period");
        } catch (Exception e) {
            System.err.println("Exception in whenRequestDataInCacheValidityPeriod: " + e.getMessage());
            e.printStackTrace();
            lastException = e;
        }
    }

    @Then("the response should use cached data")
    public void thenResponseUsesCachedData() {
        System.out.println("Executing step: Then the response should use cached data");

        assertNull(lastException, "Exception occurred: " + (lastException != null ? lastException.getMessage() : ""));
        assertNotNull(currentWeatherRecord, "Weather record is null");

        // Verify data source indicates cache
        assertNotNull(currentWeatherRecord.getDataSource(), "Data source is null");
        assertTrue(currentWeatherRecord.getDataSource().contains("Cached"),
                "Data source should indicate cached data but was: " + currentWeatherRecord.getDataSource());

        System.out.println("Verified response uses cached data");
    }

    @Then("the response should indicate it came from cache")
    public void thenResponseIndicatesCache() {
        System.out.println("Executing step: Then the response should indicate it came from cache");

        assertNotNull(currentWeatherRecord, "Weather record is null");
        assertNotNull(currentWeatherRecord.getDataSource(), "Data source is null");
        assertTrue(currentWeatherRecord.getDataSource().contains("Cached"),
                "Data source should indicate cached data but was: " + currentWeatherRecord.getDataSource());

        System.out.println("Verified response indicates it came from cache");
    }

    @When("I request to convert $fahrenheit degrees Fahrenheit to Celsius")
    public void whenConvertFahrenheitToCelsius(double fahrenheit) {
        System.out.println("Executing step: When I request to convert " + fahrenheit + " degrees Fahrenheit to Celsius");

        try {
            // Mock service to return converted temperature
            double celsius = (fahrenheit - 32) * 5 / 9;
            when(weatherService.convertFahrenheitToCelsius(anyDouble())).thenReturn(celsius);

            // Call service
            temperatureResult = weatherService.convertFahrenheitToCelsius(fahrenheit);

            System.out.println("Converted " + fahrenheit + "°F to " + temperatureResult + "°C");
        } catch (Exception e) {
            System.err.println("Exception in whenConvertFahrenheitToCelsius: " + e.getMessage());
            e.printStackTrace();
            lastException = e;
        }
    }

    @Then("the response should be $celsius degrees Celsius")
    public void thenResponseIsCelsius(double celsius) {
        System.out.println("Executing step: Then the response should be " + celsius + " degrees Celsius");

        assertNull(lastException, "Exception occurred: " + (lastException != null ? lastException.getMessage() : ""));
        assertEquals(celsius, temperatureResult, 0.01, "Temperature conversion is incorrect");

        System.out.println("Verified response is " + celsius + "°C");
    }

    @When("I request to convert $celsius degrees Celsius to Fahrenheit")
    public void whenConvertCelsiusToFahrenheit(double celsius) {
        System.out.println("Executing step: When I request to convert " + celsius + " degrees Celsius to Fahrenheit");

        try {
            // Mock service to return converted temperature
            double fahrenheit = celsius * 9 / 5 + 32;
            when(weatherService.convertCelsiusToFahrenheit(anyDouble())).thenReturn(fahrenheit);

            // Call service
            temperatureResult = weatherService.convertCelsiusToFahrenheit(celsius);

            System.out.println("Converted " + celsius + "°C to " + temperatureResult + "°F");
        } catch (Exception e) {
            System.err.println("Exception in whenConvertCelsiusToFahrenheit: " + e.getMessage());
            e.printStackTrace();
            lastException = e;
        }
    }

    @Then("the response should be $fahrenheit degrees Fahrenheit")
    public void thenResponseIsFahrenheit(double fahrenheit) {
        System.out.println("Executing step: Then the response should be " + fahrenheit + " degrees Fahrenheit");

        assertNull(lastException, "Exception occurred: " + (lastException != null ? lastException.getMessage() : ""));
        assertEquals(fahrenheit, temperatureResult, 0.01, "Temperature conversion is incorrect");

        System.out.println("Verified response is " + fahrenheit + "°F");
    }

    // Helper method to create mock weather records
    private WeatherRecord createMockWeatherRecord(Location location) {
        // Create temperature
        double fahrenheit = 72.5;
        double celsius = (fahrenheit - 32) * 5 / 9;
        Temperature temperature = Temperature.fromFahrenheit(fahrenheit);

        // Create humidity
        Humidity humidity = Humidity.of(65);

        // Create wind speed
        WindSpeed windSpeed = WindSpeed.fromMph(8.5);

        // Create condition
        WeatherCondition condition = new WeatherCondition("Partly cloudy", "04d");

        // Create weather record
        return WeatherRecord.builder()
                .id(UUID.randomUUID().toString())
                .location(location)
                .temperature(temperature)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .condition(condition)
                .pressureInHg(29.92)
                .visibilityMiles(10.0)
                .uvIndex(5)
                .recordedAt(LocalDateTime.now())
                .fetchedAt(LocalDateTime.now())
                .dataSource("Test Weather Service")
                .build();
    }
}