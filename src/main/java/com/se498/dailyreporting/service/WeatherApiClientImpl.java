package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.*;
import com.se498.dailyreporting.dto.WeatherServiceResponse;
import com.se498.dailyreporting.dto.weather.WeatherData;
import com.se498.dailyreporting.dto.weather.WeatherInfo;
import com.se498.dailyreporting.dto.weather.WeatherMain;
import com.se498.dailyreporting.exception.WeatherApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Implementation of the WeatherApiClient interface that fetches data from OpenWeatherMap
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherApiClientImpl implements WeatherApiClient {

    private final WebClient webClient;

    @Value("${weather.api.url}")
    private String apiBaseUrl;

    @Value("${weather.api.appid}")
    private String apiKey;

    @Value("${weather.api.default-country:US}")
    private String defaultCountry;

    @Value("${weather.api.units:imperial}")
    private String units;

    @Value("${weather.api.cache-control-enabled:true}")
    private boolean cacheControlEnabled;

    /**
     * {@inheritDoc}
     */
    @Override
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "fetchWeatherDataFallback")
    public WeatherRecord fetchWeatherData(Location location) {
        log.debug("Fetching weather data for location: {}", location);

        if (location == null) {
            throw new IllegalArgumentException("Invalid location provided");
        }

        String uri = UriComponentsBuilder.fromUriString(apiBaseUrl)
                .path("/forecast")
                .queryParam("zip", location.getZipCode())
                .queryParam("mode", "json")
                .queryParam("appid", apiKey)
                .build()
                .toUriString();

        try {
            WeatherServiceResponse response = webClient.get()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .retrieve()
                    .onStatus(
                            status -> status != HttpStatus.OK,
                            clientResponse -> handleErrorResponse(clientResponse, location)
                    )
                    .bodyToMono(WeatherServiceResponse.class)
                    .block();

            if (response == null || response.getList() == null || response.getList().isEmpty()) {
                throw new WeatherApiException("Empty or invalid response from OpenWeatherMap", "OpenWeatherMap");
            }

            return mapToWeatherRecord(response, location);

        } catch (WeatherApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching weather data for {}: {}", location, e.getMessage());
            throw new WeatherApiException("Failed to fetch weather data: " + e.getMessage(), "OpenWeatherMap");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isServiceAvailable() {
        try {
            String uri = UriComponentsBuilder.fromUriString(apiBaseUrl)
                    .path("/ping")
                    .queryParam("appid", apiKey)
                    .build()
                    .toUriString();

            Integer status = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getStatusCode().value())
                    .onErrorReturn(500)
                    .block();

            return status != null && status < 400;
        } catch (Exception e) {
            log.warn("Weather service availability check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderName() {
        return "OpenWeatherMap";
    }

    /**
     * Handle error response from the API
     */
    private Mono<? extends Throwable> handleErrorResponse(org.springframework.web.reactive.function.client.ClientResponse clientResponse, Location location) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    int statusCode = clientResponse.statusCode().value();
                    String message = String.format("API error: %d - %s", statusCode, errorBody);
                    log.error("Error fetching weather for {}: {}", location, message);

                    if (statusCode == 401 || statusCode == 403) {
                        return Mono.error(new WeatherApiException("Authentication error - invalid API key",
                                "OpenWeatherMap", statusCode));
                    } else if (statusCode == 404) {
                        return Mono.error(new WeatherApiException("Location not found: " + location,
                                "OpenWeatherMap", statusCode));
                    } else if (statusCode == 429) {
                        return Mono.error(new WeatherApiException("Rate limit exceeded",
                                "OpenWeatherMap", statusCode));
                    } else if (statusCode >= 500) {
                        return Mono.error(new WeatherApiException("Weather service unavailable",
                                "OpenWeatherMap", statusCode));
                    }

                    return Mono.error(new WeatherApiException(message, "OpenWeatherMap", statusCode));
                });
    }

    /**
     * Build location query string based on provided location
     */
    private String buildLocationQuery(Location location) {
        String query = location.getCity();

        if (location.getStateOrProvince() != null && !location.getStateOrProvince().isEmpty()) {
            query += "," + location.getStateOrProvince();
        }

        if (location.getCountry() != null && !location.getCountry().isEmpty()) {
            query += "," + location.getCountry();
        } else {
            query += "," + defaultCountry;
        }

        return query;
    }

    /**
     * Map OpenWeatherMap response to domain WeatherRecord
     */
    private WeatherRecord mapToWeatherRecord(WeatherServiceResponse response, Location location) {
        // Get most recent forecast (first item in list)
        WeatherData currentData = response.getList().getFirst();
        WeatherMain main = currentData.getMain();

        // Create or update location with more complete data
        Location enhancedLocation = new Location(
                response.getCity().getName(),
                response.getCity().getCountry(),
                location.getStateOrProvince(),
                location.getZipCode()
        );

        // Create temperature from Kelvin or other units
        double tempF = main.getTemp();
        if (!"imperial".equals(units)) {
            // Convert from Celsius to Fahrenheit if units are metric
            tempF = "metric".equals(units) ? celsiusToFahrenheit(main.getTemp()) : kelvinToFahrenheit(main.getTemp());
        }
        Temperature temperature = Temperature.fromFahrenheit(tempF);

        // Create weather condition
        WeatherInfo weatherInfo = currentData.getWeather().isEmpty() ?
                new WeatherInfo() : currentData.getWeather().get(0);
        WeatherCondition condition = new WeatherCondition(
                weatherInfo.getDescription(),
                weatherInfo.getIcon()
        );

        // Create humidity
        Humidity humidity = Humidity.of(main.getHumidity());

        // Create wind speed
        double windSpeedMph = currentData.getWind().getSpeed();
        if (!"imperial".equals(units)) {
            // Convert from m/s to mph
            windSpeedMph = "metric".equals(units) ? mpsToMph(currentData.getWind().getSpeed()) : currentData.getWind().getSpeed();
        }
        WindSpeed windSpeed = WindSpeed.fromMph(windSpeedMph);

        // Calculate visibility in miles
        Double visibilityMiles = null;
        if (currentData.getVisibility() != null && currentData.getVisibility() > 0) {
            // Convert visibility from meters to miles
            visibilityMiles = currentData.getVisibility() / 1609.34;
        }

        // Convert timestamp to LocalDateTime
        LocalDateTime recordedAt = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(currentData.getDt()),
                ZoneId.systemDefault()
        );

        return WeatherRecord.builder()
                .location(enhancedLocation)
                .temperature(temperature)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .condition(condition)
                .pressureInHg(convertPressureToInHg(main.getPressure()))
                .visibilityMiles(visibilityMiles)
                .uvIndex(null) // Not provided by the API
                .recordedAt(recordedAt)
                .fetchedAt(LocalDateTime.now())
                .dataSource(getProviderName())
                .build();
    }

    /**
     * Fallback method for circuit breaker
     */
    private WeatherRecord fetchWeatherDataFallback(Location location, Exception e) {
        log.warn("Using fallback for weather data request. Original error: {}", e.getMessage());

        // Try to build a minimal record with basic location data
        return WeatherRecord.builder()
                .location(location)
                .temperature(null)
                .humidity(null)
                .windSpeed(null)
                .condition(new WeatherCondition("Data unavailable (service down)", null))
                .recordedAt(null)
                .fetchedAt(LocalDateTime.now())
                .dataSource(getProviderName() + " (FALLBACK)")
                .build();
    }

    /* Unit Conversion Helpers */

    private double kelvinToFahrenheit(double kelvin) {
        return (kelvin - 273.15) * 9/5 + 32;
    }

    private double celsiusToFahrenheit(double celsius) {
        return celsius * 9/5 + 32;
    }

    private double mpsToMph(double mps) {
        return mps * 2.23694;
    }

    private Double convertPressureToInHg(Integer hPa) {
        if (hPa == null) return null;
        return hPa * 0.02953;
    }
}