package com.se498.dailyreporting.service.converter;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class TemperatureConverterTest {

    @ParameterizedTest
    @CsvSource({"0,32", "1, 33.8", "2, 35.6", "3, 37.4", "4, 39.2"})
    void convertNumberToFahrenheitTest(double numberInCelsius, double expectedNumberInFahrenheit){
        TemperatureConverter temperatureConverter = new TemperatureConverter();
        double convertedTemperature = temperatureConverter.convert(numberInCelsius);
        assertEquals(expectedNumberInFahrenheit, convertedTemperature);
    }
}
