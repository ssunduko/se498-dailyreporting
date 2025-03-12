package com.se498.dailyreporting.dto;

import com.se498.dailyreporting.domain.bo.*;
import org.instancio.*;
import org.instancio.junit.*;
import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InstancioExtension.class)
@DisplayName("WeatherMapper Tests")
class WeatherMapperDtoTest {

    @WithSettings
    private final Settings settings = Settings.create()
            .set(Keys.BEAN_VALIDATION_ENABLED, true)
            .set(Keys.COLLECTION_MIN_SIZE, 1)
            .set(Keys.COLLECTION_MAX_SIZE, 3)
            .set(Keys.STRING_NULLABLE, false)
            .set(Keys.DOUBLE_MIN, 0.0)
            .set(Keys.DOUBLE_MAX, 100.0);

    private final WeatherMapper weatherMapper = new WeatherMapper();

    @Test
    @DisplayName("toResponseDto should properly map WeatherRecord to WeatherResponse")
    @Seed(123)
    void toResponseDtoShouldMapWeatherRecordCorrectly() {
        // Given
        WeatherRecord record = createWeatherRecord();

        // When
        WeatherResponse response = weatherMapper.toResponseDto(record);

        // Then
        assertNotNull(response);
        assertEquals(record.getId(), response.getId());
        assertEquals(record.getLocation().getCity(), response.getCity());
        assertEquals(record.getLocation().getCountry(), response.getCountry());
        assertEquals(record.getLocation().getStateOrProvince(), response.getStateOrProvince());
        assertEquals(record.getLocation().getZipCode(), response.getZipCode());
        assertEquals(record.getTemperature().getFahrenheit(), response.getTemperatureF());
        assertEquals(record.getTemperature().getCelsius(), response.getTemperatureC());
        assertEquals(record.getFeelsLikeTemperature(), response.getFeelsLikeF());
        assertEquals(record.getHumidity().getPercentage(), response.getHumidity());
        assertEquals(record.getWindSpeed().getMph(), response.getWindSpeedMph());
        assertEquals(record.getWindSpeed().getKph(), response.getWindSpeedKph());
        assertEquals(record.getCondition().getDescription(), response.getCondition());
        assertEquals(record.getCondition().getIconCode(), response.getConditionIcon());
        assertEquals(record.getCondition().isClear(), response.getIsClear());
        assertEquals(record.getCondition().isRainy(), response.getIsRainy());
        assertEquals(record.getCondition().isSnowy(), response.getIsSnowy());
        assertEquals(record.getPressureInHg(), response.getPressureInHg());
        assertEquals(record.getVisibilityMiles(), response.getVisibilityMiles());
        assertEquals(record.getUvIndex(), response.getUvIndex());
        assertEquals(record.hasSevereConditions(), response.getSevereWeather());
        assertEquals(record.isFavorableForOutdoorActivities(), response.getFavorableForOutdoor());
        assertEquals(record.getRecordedAt(), response.getRecordedAt());
        assertEquals(record.getFetchedAt(), response.getFetchedAt());
        assertEquals(record.getDataSource(), response.getDataSource());
        assertEquals(record.getLocation().toString(), response.getLocationString());
    }

    @Test
    @DisplayName("toLocation should create Location from WeatherRequest with city")
    @Seed(456)
    void toLocationShouldCreateLocationFromRequestWithCity() {
        // Given
        WeatherRequest request = Instancio.of(WeatherRequest.class)
                .set(field("city"), "Seattle")
                .set(field("country"), "US")
                .set(field("stateOrProvince"), "WA")
                .set(field("zipCode"), null)
                .create();

        // When
        Location location = weatherMapper.toLocation(request);

        // Then
        assertNotNull(location);
        assertEquals("Seattle", location.getCity());
        assertEquals("US", location.getCountry());
        assertEquals("WA", location.getStateOrProvince());
        assertNull(location.getZipCode());
        assertFalse(location.isZipBased());
    }

    @Test
    @DisplayName("toLocation should create Location from WeatherRequest with zip code")
    @Seed(789)
    void toLocationShouldCreateLocationFromRequestWithZipCode() {
        // Given
        WeatherRequest request = Instancio.of(WeatherRequest.class)
                .set(field("city"), null)
                .set(field("country"), "US")
                .set(field("zipCode"), "98101")
                .create();

        // When
        Location location = weatherMapper.toLocation(request);

        // Then
        assertNotNull(location);
        assertNull(location.getCity());
        assertEquals("US", location.getCountry());
        assertEquals("98101", location.getZipCode());
        assertTrue(location.isZipBased());
    }

    @Test
    @DisplayName("toLocation should prioritize zip code over city if both are provided")
    @Seed(101112)
    void toLocationShouldPrioritizeZipCodeOverCity() {
        // Given
        WeatherRequest request = Instancio.of(WeatherRequest.class)
                .set(field("city"), "Seattle")
                .set(field("country"), "US")
                .set(field("stateOrProvince"), "WA")
                .set(field("zipCode"), "98101")
                .create();

        // When
        Location location = weatherMapper.toLocation(request);

        // Then
        assertNotNull(location);
        assertEquals("US", location.getCountry());
        assertEquals("98101", location.getZipCode());
        assertTrue(location.isZipBased());
    }

    @ParameterizedTest
    @CsvSource({
            "32.0, 0.0",
            "212.0, 100.0",
            "98.6, 37.0",
            "0.0, -17.78",
            "-40.0, -40.0"
    })
    @DisplayName("convertFToC should correctly convert Fahrenheit to Celsius")
    void convertFToCTest(double fahrenheit, double expectedCelsius) {
        // When
        double actualCelsius = weatherMapper.convertFToC(fahrenheit);

        // Then
        assertEquals(expectedCelsius, actualCelsius, 0.1); // Allow for small rounding differences
    }

    @Test
    @DisplayName("toResponseDto should handle null WeatherRecord")
    void toResponseDtoShouldHandleNullRecord() {
        // When
        WeatherResponse response = weatherMapper.toResponseDto(null);

        // Then
        assertNull(response);
    }

    @Test
    @DisplayName("toLocation should handle null WeatherRequest")
    void toLocationShouldHandleNullRequest() {
        // When
        Location location = weatherMapper.toLocation(null);

        // Then
        assertNull(location);
    }

    @Test
    @DisplayName("toLocation should use default country when none provided")
    @Seed(131415)
    void toLocationShouldUseDefaultCountryWhenNoneProvided() {
        // Given
        WeatherRequest request = Instancio.of(WeatherRequest.class)
                .set(field("country"), null)
                .create();

        // When
        Location location = weatherMapper.toLocation(request);

        // Then
        assertNotNull(location);
        assertEquals("US", location.getCountry()); // Default country should be US
    }

    /**
     * Helper method to create a complex WeatherRecord with all necessary data
     */
    private WeatherRecord createWeatherRecord() {
        return Instancio.of(WeatherRecord.class)
                .set(field(WeatherRecord::getId), "test-record-1")
                .supply(field(WeatherRecord::getLocation), () ->
                        Instancio.of(Location.class)
                                .set(field(Location::getCity), "Seattle")
                                .set(field(Location::getCountry), "US")
                                .set(field(Location::getStateOrProvince), "WA")
                                .set(field(Location::getZipCode), "98101")
                                .create())
                .supply(field(WeatherRecord::getTemperature), () ->
                        Instancio.of(Temperature.class)
                                .set(field(Temperature::getFahrenheit), 72.5)
                                .set(field(Temperature::getCelsius), 22.5)
                                .create())
                .supply(field(WeatherRecord::getHumidity), () ->
                        Instancio.of(Humidity.class)
                                .set(field(Humidity::getPercentage), 45)
                                .create())
                .supply(field(WeatherRecord::getWindSpeed), () ->
                        Instancio.of(WindSpeed.class)
                                .set(field(WindSpeed::getMph), 12.0)
                                .create())
                .supply(field(WeatherRecord::getCondition), () ->
                        Instancio.of(WeatherCondition.class)
                                .set(field(WeatherCondition::getDescription), "Partly Cloudy")
                                .set(field(WeatherCondition::getIconCode), "03d")
                                .create())
                .set(field(WeatherRecord::getPressureInHg), 29.92)
                .set(field(WeatherRecord::getVisibilityMiles), 10.0)
                .set(field(WeatherRecord::getUvIndex), 5)
                .generate(field(WeatherRecord::getRecordedAt), gen -> gen.temporal().localDateTime().past())
                .generate(field(WeatherRecord::getFetchedAt), gen -> gen.temporal().localDateTime().future())
                .set(field(WeatherRecord::getDataSource), "Test Provider")
                .create();
    }
}