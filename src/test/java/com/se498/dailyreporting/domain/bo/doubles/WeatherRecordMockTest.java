package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for WeatherRecord using Mockito mocks
 * This test demonstrates using a mocked LocationStub
 */
@ExtendWith(MockitoExtension.class)
public class WeatherRecordMockTest {

    // Using Mockito mocks for all dependencies
    @Mock
    private Temperature temperatureMock;

    @Mock
    private Humidity humidityMock;

    // Mocking LocationStub
    @Mock
    private LocationStub locationStub;

    @Mock
    private WeatherCondition conditionMock;

    @Mock
    private WindSpeed windSpeedMock;

    private WeatherRecord weatherRecord;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        createWeatherRecord();
    }


    @Test
    void testChangingLocationStubBehavior() {
        // This test demonstrates changing mock behavior

        // Arrange - Change the behavior for specific methods
        when(locationStub.getCity()).thenReturn("NewCity");
        when(locationStub.getZipCode()).thenReturn("99999");

        // Act
        String city = locationStub.getCity();
        String zipCode = locationStub.getZipCode();

        // Assert
        assertEquals("NewCity", city);
        assertEquals("99999", zipCode);

        // Verify
        verify(locationStub).getCity();
        verify(locationStub).getZipCode();
    }

    @Test
    void testLocationConfirmationMethod() {
        // This test shows mocking a method that takes parameters

        // Arrange
        LocationStub otherLocation = new LocationStub();
        when(locationStub.confirmLocation(any(Location.class))).thenReturn(true);

        // Act
        boolean result = locationStub.confirmLocation(otherLocation);

        // Assert
        assertTrue(result); // Our mock returns true

        // Verify
        verify(locationStub).confirmLocation(otherLocation);
    }

    @Test
    void testHasSevereConditions_withNormalConditions() {
        // Act
        boolean result = weatherRecord.hasSevereConditions();

        // Assert
        assertFalse(result, "Weather should not be severe under normal conditions");

        // Verify interactions
        verify(conditionMock).isSevere();
        verify(windSpeedMock).getMph();
    }

    @Test
    void testHasSevereConditions_withSevereCondition() {
        // Arrange
        when(conditionMock.isSevere()).thenReturn(true);
        createWeatherRecord();

        // Act
        boolean result = weatherRecord.hasSevereConditions();

        // Assert/Verify
        assertTrue(result, "Weather should be severe with severe condition");
        verify(conditionMock).isSevere();
        // Temperature and wind speed shouldn't be checked due to short-circuit evaluation
        verify(temperatureMock, never()).getFahrenheit();
        verify(windSpeedMock, never()).getMph();
    }

    @Test
    void testHasSevereConditions_withExtremeHeat() {
        // Arrange
        when(temperatureMock.getFahrenheit()).thenReturn(105.0);
        createWeatherRecord();

        // Act
        boolean result = weatherRecord.hasSevereConditions();

        // Assert/Verify
        assertTrue(result, "Weather should be severe with temperature above 100°F");
        verify(conditionMock).isSevere();
        verify(temperatureMock).getFahrenheit();
    }

    @Test
    void testGetHeatIndex_withHighTemperatureAndHumidity() {
        // Arrange
        when(temperatureMock.getFahrenheit()).thenReturn(90.0);
        when(humidityMock.getPercentage()).thenReturn(85);
        createWeatherRecord();

        // Act
        double heatIndex = weatherRecord.getHeatIndex();

        // Assert/Verify
        assertTrue(heatIndex > 90.0, "Heat index should be higher than temperature");
        assertEquals(117.15, heatIndex, 1);
        verify(temperatureMock).getFahrenheit();
        verify(humidityMock).getPercentage();
    }


    @Test
    void testGetWindChill_whenConditionsAreFavorable_shouldCalculateCorrectly() {
        // Arrange
        when(temperatureMock.getFahrenheit()).thenReturn(20.0);
        when(windSpeedMock.getMph()).thenReturn(15.0);
        createWeatherRecord();

        // Act
        double windChill = weatherRecord.getWindChill();

        // Assert/Verify
        assertTrue(windChill < 20.0, "Wind chill should be lower than actual temperature");
        assertEquals(6.2, windChill, 0.1);
        verify(temperatureMock).getFahrenheit();
        verify(windSpeedMock).getMph();
    }

    @Test
    void testGetFeelsLikeTemperature_forHotTemperature() {
        // Arrange
        when(temperatureMock.getFahrenheit()).thenReturn(90.0);
        when(humidityMock.getPercentage()).thenReturn(70);
        createWeatherRecord();

        WeatherRecord spyRecord = Mockito.spy(weatherRecord);

        // Act
        double feelsLike = spyRecord.getFeelsLikeTemperature();

        // Assert/Verify
        // For hot temperatures, should call getHeatIndex but not getWindChill
        verify(spyRecord, times(1)).getHeatIndex();
        verify(spyRecord, never()).getWindChill();
        assertTrue(feelsLike > 90.0, "Feels like temperature should be higher than actual in hot, humid conditions");
    }

    @Test
    void testVerifyNoMoreInteractions() {
        // This test demonstrates verifyNoMoreInteractions

        // Act
        String city = locationStub.getCity();

        // Assert
        assertNull(city);

        // Verify
        verify(locationStub).getCity();
        verifyNoMoreInteractions(locationStub);
    }

    @Test
    void testIsFresh_andGetAgeInMinutes() {
        // Arrange
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(15);

        WeatherRecord record = WeatherRecord.builder()
                .id("test-id")
                .location(locationStub)
                .temperature(temperatureMock)
                .humidity(humidityMock)
                .windSpeed(windSpeedMock)
                .condition(conditionMock)
                .fetchedAt(recentTime)
                .build();

        // Act & Assert
        assertTrue(record.isFresh(), "Weather record should be fresh when fetched 15 minutes ago");

        long age = record.getAgeInMinutes();
        // Allow 1 minute difference because of test execution time
        assertTrue(age >= 15 && age <= 16, "Age should be approximately 15 minutes");

        // Test with old data
        LocalDateTime oldTime = LocalDateTime.now().minusMinutes(45);

        record = WeatherRecord.builder()
                .id("test-id")
                .location(locationStub)
                .temperature(temperatureMock)
                .humidity(humidityMock)
                .windSpeed(windSpeedMock)
                .condition(conditionMock)
                .fetchedAt(oldTime)
                .build();

        assertFalse(record.isFresh(), "Weather record should not be fresh when fetched 45 minutes ago");
    }

    private void createWeatherRecord() {
        weatherRecord = WeatherRecord.builder()
                .id("test-id")
                .location(locationStub)
                .temperature(temperatureMock)
                .humidity(humidityMock)
                .windSpeed(windSpeedMock)
                .condition(conditionMock)
                .pressureInHg(30.0)
                .visibilityMiles(10.0)
                .uvIndex(5)
                .recordedAt(now.minusMinutes(10))
                .fetchedAt(now.minusMinutes(5))
                .dataSource("test-source")
                .build();
    }
}