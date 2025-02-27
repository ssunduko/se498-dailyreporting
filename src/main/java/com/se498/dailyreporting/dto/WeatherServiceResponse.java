package com.se498.dailyreporting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.se498.dailyreporting.dto.weather.WeatherData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for OpenWeatherMap API response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherServiceResponse {

    @JsonProperty("cod")
    private String cod;

    @JsonProperty("message")
    private double message;

    @JsonProperty("cnt")
    private int count;

    @JsonProperty("list")
    private List<WeatherData> list = new ArrayList<>();

    @JsonProperty("city")
    private City city;

    /**
     * City information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class City {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("coord")
        private Coordinates coord;

        @JsonProperty("country")
        private String country;

        @JsonProperty("population")
        private Long population;

        @JsonProperty("timezone")
        private Integer timezone;

        @JsonProperty("sunrise")
        private Long sunrise;

        @JsonProperty("sunset")
        private Long sunset;
    }

    /**
     * Coordinates (latitude and longitude)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coordinates {

        @JsonProperty("lat")
        private Double lat;

        @JsonProperty("lon")
        private Double lon;
    }
}