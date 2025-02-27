package com.se498.dailyreporting.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for weather data responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Weather data response")
public class WeatherResponse {

    @Schema(description = "Unique identifier")
    private String id;

    @Schema(description = "City name", example = "Seattle")
    private String city;

    @Schema(description = "Country code", example = "US")
    private String country;

    @Schema(description = "State or province", example = "WA")
    private String stateOrProvince;

    @Schema(description = "Zip/Postal code", example = "98101")
    private String zipCode;

    @Schema(description = "Formatted location string", example = "Seattle, WA 98101, US")
    private String locationString;

    @Schema(description = "Temperature in Fahrenheit", example = "72.5")
    private Double temperatureF;

    @Schema(description = "Temperature in Celsius", example = "22.5")
    private Double temperatureC;

    @Schema(description = "Feels like temperature in Fahrenheit", example = "75.2")
    private Double feelsLikeF;

    @Schema(description = "Feels like temperature in Celsius", example = "24.0")
    private Double feelsLikeC;

    @Schema(description = "Humidity percentage", example = "65")
    private Integer humidity;

    @Schema(description = "Wind speed in mph", example = "8.5")
    private Double windSpeedMph;

    @Schema(description = "Wind speed in kph", example = "13.7")
    private Double windSpeedKph;

    @Schema(description = "Weather condition description", example = "Partly cloudy")
    private String condition;

    @Schema(description = "Weather condition icon code", example = "04d")
    private String conditionIcon;

    @Schema(description = "Whether skies are clear", example = "false")
    private Boolean isClear;

    @Schema(description = "Whether it's currently raining", example = "false")
    private Boolean isRainy;

    @Schema(description = "Whether it's currently snowing", example = "false")
    private Boolean isSnowy;

    @Schema(description = "Barometric pressure in inches of mercury", example = "29.92")
    private Double pressureInHg;

    @Schema(description = "Visibility in miles", example = "10.0")
    private Double visibilityMiles;

    @Schema(description = "UV index", example = "5")
    private Integer uvIndex;

    @Schema(description = "Whether severe weather conditions exist", example = "false")
    private Boolean severeWeather;

    @Schema(description = "Whether conditions are favorable for outdoor activities", example = "true")
    private Boolean favorableForOutdoor;

    @Schema(description = "When the weather data was recorded by the source")
    private LocalDateTime recordedAt;

    @Schema(description = "When the weather data was fetched from the source")
    private LocalDateTime fetchedAt;

    @Schema(description = "Source of the weather data", example = "OpenWeatherMap")
    private String dataSource;

    @Schema(description = "Whether alerts are present", example = "false")
    private Boolean hasAlerts;

    @Schema(description = "Weather alerts if any")
    private List<String> alerts;

    @Schema(description = "Whether data is from cache", example = "true")
    private Boolean fromCache;

    @Schema(description = "Age of data in minutes", example = "5")
    private Long dataAgeMinutes;
}
