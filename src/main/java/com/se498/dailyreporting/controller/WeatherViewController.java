package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.WeatherRecord;
import com.se498.dailyreporting.dto.WeatherMapper;
import com.se498.dailyreporting.service.WeatherReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller for weather UI views
 */
@Slf4j
@Controller
@RequestMapping("/ui/weather")
@RequiredArgsConstructor
public class WeatherViewController {

    @Autowired
    private final WeatherReportingService weatherService;

    @Autowired
    private final WeatherMapper mapper;

    /**
     * Home page for weather reporting
     */
    @GetMapping
    public String weatherHome() {
        return "weather/index";
    }

    /**
     * Current weather page
     */
    @GetMapping("/current")
    public String currentWeatherForm(Model model) {
        model.addAttribute("pageTitle", "Current Weather");
        return "weather/current";
    }

    /**
     * Current weather results
     */
    @PostMapping("/current")
    public String getCurrentWeather(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zip,
            @RequestParam(required = false, defaultValue = "US") String country,
            @RequestParam(required = false) String stateOrProvince,
            @RequestParam(required = false, defaultValue = "false") boolean includeAlerts,
            Model model) {

        // Validate that either city or zip is provided
        if (!StringUtils.hasText(city) && !StringUtils.hasText(zip)) {
            model.addAttribute("error", "Either city or zip must be provided");
            return "weather/current";
        }

        try {
            Location location;
            if (StringUtils.hasText(zip)) {
                location = Location.fromZipCode(zip, country);
            } else {
                location = new Location(city, country, stateOrProvince);
            }

            WeatherRecord record = weatherService.getCurrentWeather(location);
            model.addAttribute("weather", mapper.toResponseDto(record));

            if (includeAlerts) {
                List<String> alerts = weatherService.analyzeForAlerts(record);
                model.addAttribute("alerts", alerts);
                model.addAttribute("hasAlerts", !alerts.isEmpty());
            }

            model.addAttribute("pageTitle", "Current Weather for " + location);
            return "weather/current-results";
        } catch (Exception e) {
            log.error("Error fetching weather data: {}", e.getMessage(), e);
            model.addAttribute("error", "Error fetching weather data: " + e.getMessage());
            return "weather/current";
        }
    }

    /**
     * Weather history form
     */
    @GetMapping("/history")
    public String historicalWeatherForm(Model model) {
        model.addAttribute("pageTitle", "Historical Weather");

        // Add default dates
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        model.addAttribute("startDate", yesterday.toString());
        model.addAttribute("endDate", today.toString());

        return "weather/history";
    }

    /**
     * Weather history results
     */
    @PostMapping("/history")
    public String getHistoricalWeather(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zip,
            @RequestParam(required = false, defaultValue = "US") String country,
            @RequestParam(required = false) String stateOrProvince,
            @RequestParam String startDate,
            @RequestParam String startTime,
            @RequestParam String endDate,
            @RequestParam String endTime,
            Model model) {

        // Validate that either city or zip is provided
        if (!StringUtils.hasText(city) && !StringUtils.hasText(zip)) {
            model.addAttribute("error", "Either city or zip must be provided");
            return "weather/history";
        }

        try {
            // Parse date and time inputs
            LocalDateTime start = LocalDateTime.parse(startDate + "T" + startTime);
            LocalDateTime end = LocalDateTime.parse(endDate + "T" + endTime);

            Location location;
            if (StringUtils.hasText(zip)) {
                location = Location.fromZipCode(zip, country);
            } else {
                location = new Location(city, country, stateOrProvince);
            }

            List<WeatherRecord> records;
            try {
                records = weatherService.getHistoricalWeather(location, start, end);
            } catch (NullPointerException e) {
                // Handle the null key issue
                log.warn("NullPointerException when getting historical data: {}", e.getMessage());
                records = new ArrayList<>(); // Return empty list to avoid template errors
            }

            model.addAttribute("weatherRecords", records.isEmpty() ?
                    Collections.emptyList() :
                    records.stream().map(mapper::toResponseDto).toList());
            model.addAttribute("location", location.toString());
            model.addAttribute("startDateTime", start);
            model.addAttribute("endDateTime", end);
            model.addAttribute("pageTitle", "Historical Weather for " + location);

            if (records.isEmpty()) {
                model.addAttribute("warning", "No historical weather data found for the specified time period. Try using current weather instead.");
            }

            return "weather/history-results";
        } catch (Exception e) {
            log.error("Error fetching historical weather data: {}", e.getMessage(), e);
            model.addAttribute("error", "Error fetching historical weather data: " + e.getMessage());

            // Add the form values back to the model
            model.addAttribute("city", city);
            model.addAttribute("zip", zip);
            model.addAttribute("country", country);
            model.addAttribute("stateOrProvince", stateOrProvince);
            model.addAttribute("startDate", startDate);
            model.addAttribute("startTime", startTime);
            model.addAttribute("endDate", endDate);
            model.addAttribute("endTime", endTime);

            return "weather/history";
        }
    }

    /**
     * Weather alerts form
     */
    @GetMapping("/alerts")
    public String alertsForm(Model model) {
        model.addAttribute("pageTitle", "Weather Alerts");
        return "weather/alerts";
    }

    /**
     * Weather alerts results
     */
    @PostMapping("/alerts")
    public String getWeatherAlerts(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zip,
            @RequestParam(required = false, defaultValue = "US") String country,
            @RequestParam(required = false) String stateOrProvince,
            Model model) {

        // Validate that either city or zip is provided
        if (!StringUtils.hasText(city) && !StringUtils.hasText(zip)) {
            model.addAttribute("error", "Either city or zip must be provided");
            return "weather/alerts";
        }

        try {
            Location location;
            if (StringUtils.hasText(zip)) {
                location = Location.fromZipCode(zip, country);
            } else {
                location = new Location(city, country, stateOrProvince);
            }

            WeatherRecord record = weatherService.getCurrentWeather(location);
            List<String> alerts = weatherService.analyzeForAlerts(record);

            model.addAttribute("location", location.toString());
            model.addAttribute("recordedAt", record.getRecordedAt());
            model.addAttribute("fetchedAt", record.getFetchedAt());
            model.addAttribute("alertCount", alerts.size());
            model.addAttribute("alerts", alerts);
            model.addAttribute("temperature", record.getTemperature().getFahrenheit());
            model.addAttribute("condition", record.getCondition().getDescription());
            model.addAttribute("hasSevereConditions", record.hasSevereConditions());
            model.addAttribute("pageTitle", "Weather Alerts for " + location);

            return "weather/alerts-results";
        } catch (Exception e) {
            log.error("Error fetching weather alerts: {}", e.getMessage(), e);
            model.addAttribute("error", "Error fetching weather alerts: " + e.getMessage());
            return "weather/alerts";
        }
    }

    /**
     * Temperature conversion tool
     */
    @GetMapping("/convert")
    public String temperatureConversion(Model model) {
        model.addAttribute("pageTitle", "Temperature Conversion");
        return "weather/convert";
    }

    /**
     * Temperature conversion results
     */
    @PostMapping("/convert")
    public String convertTemperature(
            @RequestParam String conversionType,
            @RequestParam double temperature,
            Model model) {

        double result;
        String fromUnit, toUnit;

        if ("ftoc".equals(conversionType)) {
            result = weatherService.convertFahrenheitToCelsius(temperature);
            fromUnit = "Fahrenheit";
            toUnit = "Celsius";
        } else {
            result = weatherService.convertCelsiusToFahrenheit(temperature);
            fromUnit = "Celsius";
            toUnit = "Fahrenheit";
        }

        // Round to 2 decimal places
        result = Math.round(result * 100.0) / 100.0;

        model.addAttribute("originalTemp", temperature);
        model.addAttribute("convertedTemp", result);
        model.addAttribute("fromUnit", fromUnit);
        model.addAttribute("toUnit", toUnit);
        model.addAttribute("pageTitle", "Temperature Conversion");

        return "weather/convert";
    }
}