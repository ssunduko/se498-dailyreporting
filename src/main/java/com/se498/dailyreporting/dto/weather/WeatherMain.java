package com.se498.dailyreporting.dto.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Main weather data including temperature and humidity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherMain {

    @JsonProperty("temp")
    private Double temp;

    @JsonProperty("feels_like")
    private Double feelsLike;

    @JsonProperty("temp_min")
    private Double tempMin;

    @JsonProperty("temp_max")
    private Double tempMax;

    @JsonProperty("pressure")
    private Integer pressure;

    @JsonProperty("sea_level")
    private Integer seaLevel;

    @JsonProperty("grnd_level")
    private Integer groundLevel;

    @JsonProperty("humidity")
    private Integer humidity;

    @JsonProperty("temp_kf")
    private Double tempKf;
}