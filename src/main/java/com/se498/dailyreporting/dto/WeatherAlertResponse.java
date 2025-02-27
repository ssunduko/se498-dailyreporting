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
 * DTO for weather alerts response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Weather alerts response")
public class WeatherAlertResponse {

    @Schema(description = "Location string", example = "Seattle, WA, US")
    private String location;

    @Schema(description = "When the weather data was recorded")
    private LocalDateTime recordedAt;

    @Schema(description = "When the weather data was fetched")
    private LocalDateTime fetchedAt;

    @Schema(description = "Number of active alerts", example = "2")
    private Integer alertCount;

    @Schema(description = "Current temperature in Fahrenheit", example = "87.5")
    private Double temperature;

    @Schema(description = "Current weather condition", example = "Thunderstorm")
    private String condition;

    @Schema(description = "Whether severe conditions exist", example = "true")
    private Boolean hasSevereConditions;

    @Schema(description = "List of active weather alerts")
    private List<String> alerts;
}