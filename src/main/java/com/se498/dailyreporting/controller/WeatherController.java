package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.WeatherRecord;
import com.se498.dailyreporting.dto.*;
import com.se498.dailyreporting.service.WeatherReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for weather service operations
 */
@Slf4j
@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
@Validated
@Tag(name = "Weather Service API", description = "Endpoints for retrieving weather data and performing temperature conversions")
public class WeatherController {

    @Autowired
    private final WeatherReportingService weatherService;
    @Autowired
    private final WeatherMapper mapper;

    /**
     * Get current weather for a location
     */
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get current weather",
            description = "Retrieves the current weather data for the specified location (by city or zip)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Weather data retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WeatherResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Weather service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WeatherResponse> getCurrentWeather(
            @Parameter(description = "City name (required if zip not provided)")
            @RequestParam(required = false) String city,

            @Parameter(description = "Zip/Postal code (required if city not provided)")
            @RequestParam(required = false) String zip,

            @Parameter(description = "Country code (ISO 3166 alpha-2)")
            @RequestParam(required = false, defaultValue = "US") String country,

            @Parameter(description = "State or province (optional)")
            @RequestParam(required = false) String stateOrProvince,

            @Parameter(description = "Whether to include alerts in response")
            @RequestParam(required = false, defaultValue = "false") boolean includeAlerts) {

        // Validate that either city or zip is provided
        if (!StringUtils.hasText(city) && !StringUtils.hasText(zip)) {
            throw new IllegalArgumentException("Either city or zip must be provided");
        }

        log.debug("Request for current weather: city={}, zip={}, country={}",
                city, zip, country);

        Location location;
        if (StringUtils.hasText(zip)) {
            location = Location.fromZipCode(zip, country);
        } else {
            location = new Location(city, country, stateOrProvince);
        }

        WeatherRecord record = weatherService.getCurrentWeather(location);
        WeatherResponse response = mapper.toResponseDto(record);

        if (includeAlerts) {
            List<String> alerts = weatherService.analyzeForAlerts(record);
            response.setAlerts(alerts);
            response.setHasAlerts(!alerts.isEmpty());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get current weather by zip code
     */
    @GetMapping(value = "/current/zip/{zipCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get current weather by zip code",
            description = "Retrieves the current weather data for the specified zip/postal code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Weather data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid zip code format"),
            @ApiResponse(responseCode = "503", description = "Weather service unavailable")
    })
    public ResponseEntity<WeatherResponse> getCurrentWeatherByZip(
            @Parameter(description = "Zip/Postal code", required = true)
            @PathVariable @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid US zip code format") String zipCode,

            @Parameter(description = "Country code (ISO 3166 alpha-2)")
            @RequestParam(required = false, defaultValue = "US") String country,

            @Parameter(description = "Whether to include alerts in response")
            @RequestParam(required = false, defaultValue = "false") boolean includeAlerts) {

        log.debug("Request for current weather by zip: zip={}, country={}", zipCode, country);

        Location location = Location.fromZipCode(zipCode, country);
        WeatherRecord record = weatherService.getCurrentWeather(location);
        WeatherResponse response = mapper.toResponseDto(record);

        if (includeAlerts) {
            List<String> alerts = weatherService.analyzeForAlerts(record);
            response.setAlerts(alerts);
            response.setHasAlerts(!alerts.isEmpty());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get current weather using POST method with request body
     */
    @PostMapping(value = "/current", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get current weather (POST)",
            description = "Retrieves current weather using request body parameters")
    public ResponseEntity<WeatherResponse> getCurrentWeatherPost(
            @RequestBody @Valid WeatherRequest requestDto) {

        log.debug("POST request for current weather: {}", requestDto);

        // Validate that either city or zip is provided
        if (!StringUtils.hasText(requestDto.getCity()) && !StringUtils.hasText(requestDto.getZipCode())) {
            throw new IllegalArgumentException("Either city or zipCode must be provided");
        }

        Location location = mapper.toLocation(requestDto);
        WeatherRecord record = weatherService.getCurrentWeather(location);
        WeatherResponse response = mapper.toResponseDto(record);

        if (requestDto.isIncludeAlerts()) {
            List<String> alerts = weatherService.analyzeForAlerts(record);
            response.setAlerts(alerts);
            response.setHasAlerts(!alerts.isEmpty());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get historical weather data
     */
    @GetMapping("/history")
    @Operation(summary = "Get historical weather data",
            description = "Retrieves historical weather data for a location within a date range")
    public ResponseEntity<List<WeatherResponse>> getHistoricalWeather(
            @Parameter(description = "City name (required if zip not provided)")
            @RequestParam(required = false) String city,

            @Parameter(description = "Zip/Postal code (required if city not provided)")
            @RequestParam(required = false) String zip,

            @Parameter(description = "Country code")
            @RequestParam(required = false, defaultValue = "US") String country,

            @Parameter(description = "State or province")
            @RequestParam(required = false) String stateOrProvince,

            @Parameter(description = "Start date/time (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent LocalDateTime start,

            @Parameter(description = "End date/time (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent LocalDateTime end) {

        // Validate that either city or zip is provided
        if (!StringUtils.hasText(city) && !StringUtils.hasText(zip)) {
            throw new IllegalArgumentException("Either city or zip must be provided");
        }

        log.debug("Request for historical weather: city={}, zip={}, start={}, end={}",
                city, zip, start, end);

        Location location;
        if (StringUtils.hasText(zip)) {
            location = Location.fromZipCode(zip, country);
        } else {
            location = new Location(city, country, stateOrProvince);
        }

        List<WeatherRecord> records = weatherService.getHistoricalWeather(location, start, end);

        if (records.isEmpty()) {
            log.info("No historical weather data found for {} between {} and {}",
                    location, start, end);
            return ResponseEntity.notFound().build();
        }

        List<WeatherResponse> response = records.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get alerts for a location
     */
    @GetMapping("/alerts")
    @Operation(summary = "Get weather alerts",
            description = "Analyzes current weather conditions and returns any active alerts")
    public ResponseEntity<WeatherAlertResponse> getWeatherAlerts(
            @Parameter(description = "City name (required if zip not provided)")
            @RequestParam(required = false) String city,

            @Parameter(description = "Zip/Postal code (required if city not provided)")
            @RequestParam(required = false) String zip,

            @Parameter(description = "Country code")
            @RequestParam(required = false, defaultValue = "US") String country,

            @Parameter(description = "State or province")
            @RequestParam(required = false) String stateOrProvince) {

        // Validate that either city or zip is provided
        if (!StringUtils.hasText(city) && !StringUtils.hasText(zip)) {
            throw new IllegalArgumentException("Either city or zip must be provided");
        }

        log.debug("Request for weather alerts: city={}, zip={}, country={}", city, zip, country);

        Location location;
        if (StringUtils.hasText(zip)) {
            location = Location.fromZipCode(zip, country);
        } else {
            location = new Location(city, country, stateOrProvince);
        }

        WeatherRecord record = weatherService.getCurrentWeather(location);
        List<String> alerts = weatherService.analyzeForAlerts(record);

        WeatherAlertResponse response = WeatherAlertResponse.builder()
                .location(location.toString())
                .recordedAt(record.getRecordedAt())
                .fetchedAt(record.getFetchedAt())
                .alertCount(alerts.size())
                .alerts(alerts)
                .temperature(record.getTemperature().getFahrenheit())
                .condition(record.getCondition().getDescription())
                .hasSevereConditions(record.hasSevereConditions())
                .build();

        return ResponseEntity.ok(response);
    }

    // Rest of the controller remains unchanged...

    /**
     * Convert temperature from Fahrenheit to Celsius
     */
    @GetMapping("/convert/ftoc")
    @Operation(summary = "Convert Fahrenheit to Celsius",
            description = "Converts a temperature value from Fahrenheit to Celsius")
    public ResponseEntity<Double> convertFahrenheitToCelsius(
            @Parameter(description = "Temperature in Fahrenheit", required = true)
            @RequestParam double temperature) {

        double celsius = weatherService.convertFahrenheitToCelsius(temperature);
        // Round to 2 decimal places
        celsius = Math.round(celsius * 100.0) / 100.0;

        return ResponseEntity.ok(celsius);
    }

    /**
     * Convert temperature from Celsius to Fahrenheit
     */
    @GetMapping("/convert/ctof")
    @Operation(summary = "Convert Celsius to Fahrenheit",
            description = "Converts a temperature value from Celsius to Fahrenheit")
    public ResponseEntity<Double> convertCelsiusToFahrenheit(
            @Parameter(description = "Temperature in Celsius", required = true)
            @RequestParam double temperature) {

        double fahrenheit = weatherService.convertCelsiusToFahrenheit(temperature);
        // Round to 2 decimal places
        fahrenheit = Math.round(fahrenheit * 100.0) / 100.0;

        return ResponseEntity.ok(fahrenheit);
    }

    /**
     * Get service status
     */
    @GetMapping("/status")
    @Operation(summary = "Get service status",
            description = "Checks if the weather service is operational")
    public ResponseEntity<String> getServiceStatus() {
        return ResponseEntity.ok("Weather Service is operational");
    }
}