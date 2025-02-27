package com.se498.dailyreporting.dto.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Weather data for a specific time
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData {

    @JsonProperty("dt")
    private Long dt;

    @JsonProperty("main")
    private WeatherMain main;

    @JsonProperty("weather")
    private List<WeatherInfo> weather = new ArrayList<>();

    @JsonProperty("clouds")
    private Clouds clouds;

    @JsonProperty("wind")
    private Wind wind;

    @JsonProperty("visibility")
    private Integer visibility;

    @JsonProperty("pop")
    private Double probabilityOfPrecipitation;

    @JsonProperty("rain")
    private Precipitation rain;

    @JsonProperty("snow")
    private Precipitation snow;

    @JsonProperty("sys")
    private SystemInfo sys;

    @JsonProperty("dt_txt")
    private String dtTxt;
}