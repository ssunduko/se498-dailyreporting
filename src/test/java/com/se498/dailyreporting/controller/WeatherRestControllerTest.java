package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.TestDailyReportingApplication;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TestDailyReportingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WeatherRestControllerTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static HttpHeaders headers;

    @BeforeAll
    static void init() {
        headers = new HttpHeaders();
        headers.setBasicAuth("sergey", "chapman");
    }

    @Test
    public void testGetWeatherByZip() throws IllegalStateException, JSONException {
        // This test assumes the service is running and the repository has data or can generate it

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/weather/current/zip/90210", HttpMethod.GET, new HttpEntity<String>(headers),
                String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        // Check that the response has the right structure but don't verify exact values
        // as they might change with real or mock weather data
        Assertions.assertTrue(response.getBody().contains("location"));
        Assertions.assertTrue(response.getBody().contains("temperature"));
    }

    @Test
    public void testTemperatureConversion() {
        double celsiusValue = 25.0;
        double expectedFahrenheit = 77.0; // 25°C = 77°F

        ResponseEntity<Double> response = restTemplate.exchange(
                "http://localhost:" + port + "/weather/convert/ctof?temperature=" + celsiusValue,
                HttpMethod.GET,
                new HttpEntity<String>(headers),
                Double.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(expectedFahrenheit, response.getBody(), 0.1); // Allow small rounding differences
    }
}