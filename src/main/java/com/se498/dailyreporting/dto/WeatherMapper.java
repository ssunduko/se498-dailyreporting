package com.se498.dailyreporting.dto;


import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.WeatherRecord;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Mapper for converting between domain and DTO objects
 */
@Component
public class WeatherMapper {

    /**
     * Maps a domain WeatherRecord to a response DTO
     *
     * @param record Domain weather record
     * @return DTO representation
     */
    public WeatherResponse toResponseDto(WeatherRecord record) {
        if (record == null) {
            return null;
        }

        return WeatherResponse.builder()
                .id(record.getId())
                .city(record.getLocation().getCity())
                .country(record.getLocation().getCountry())
                .stateOrProvince(record.getLocation().getStateOrProvince())
                .zipCode(record.getLocation().getZipCode())
                .temperatureF(record.getTemperature().getFahrenheit())
                .temperatureC(record.getTemperature().getCelsius())
                .feelsLikeF(record.getFeelsLikeTemperature())
                .feelsLikeC(convertFToC(record.getFeelsLikeTemperature()))
                .humidity(record.getHumidity().getPercentage())
                .windSpeedMph(record.getWindSpeed().getMph())
                .windSpeedKph(record.getWindSpeed().getKph())
                .condition(record.getCondition().getDescription())
                .conditionIcon(record.getCondition().getIconCode())
                .isClear(record.getCondition().isClear())
                .isRainy(record.getCondition().isRainy())
                .isSnowy(record.getCondition().isSnowy())
                .pressureInHg(record.getPressureInHg())
                .visibilityMiles(record.getVisibilityMiles())
                .uvIndex(record.getUvIndex())
                .severeWeather(record.hasSevereConditions())
                .favorableForOutdoor(record.isFavorableForOutdoorActivities())
                .recordedAt(record.getRecordedAt())
                .fetchedAt(record.getFetchedAt())
                .dataSource(record.getDataSource())
                .locationString(record.getLocation().toString())
                .build();
    }

    /**
     * Maps a request DTO to a domain Location
     *
     * @param request Request DTO
     * @return Domain location
     */
    public Location toLocation(WeatherRequest request) {
        if (request == null) {
            return null;
        }

        // Prioritize zip code if provided
        if (StringUtils.hasText(request.getZipCode())) {
            return Location.fromZipCode(
                    request.getZipCode(),
                    StringUtils.hasText(request.getCountry()) ? request.getCountry() : "US"
            );
        }

        // Otherwise use city-based location
        return Location.builder()
                .city(request.getCity())
                .country(request.getCountry())
                .stateOrProvince(request.getStateOrProvince())
                .build();
    }

    /**
     * Helper method to convert Fahrenheit to Celsius
     *
     * @param fahrenheit Temperature in Fahrenheit
     * @return Temperature in Celsius
     */
    private double convertFToC(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }
}