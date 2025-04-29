package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.TestDailyReportingApplication;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TestDailyReportingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WeatherExternalMockControllerTest {

    @Test
    void testGetWeatherByZip() throws JSONException {
        // This uses an external mock API (replace with your actual mock API endpoint)
        String expectedJson = "{\"location\":\"Beverly Hills, US\",\"temperature\":{\"fahrenheit\":72.5,\"celsius\":22.5},\"condition\":{\"description\":\"Clear\",\"iconCode\":\"01d\"}}";

        ExtractableResponse<Response> response = RestAssured
                .given()
                .filter(new RequestLoggingFilter())
                .auth().basic("sergey", "chapman")
                .contentType(ContentType.JSON)
                .when()
                .get("https://mp0161ded52d574dc769.free.beeceptor.com/weather/current/zip/90210")
                .then()
                .statusCode(200)
                .extract();

        JSONAssert.assertEquals(expectedJson, response.body().asPrettyString(), false);
    }

    @Test
    void testGetWeatherAlerts() throws JSONException {
        String expectedJson = "{\"location\":\"Beverly Hills, US\",\"alertCount\":0,\"hasSevereConditions\":false}";

        ExtractableResponse<Response> response = RestAssured
                .given()
                .filter(new RequestLoggingFilter())
                .auth().basic("sergey", "chapman")
                .contentType(ContentType.JSON)
                .when()
                .get("https://mp76d7e6ab1ed9b7609d.free.beeceptor.com/weather/alerts?zip=90210")
                .then()
                .statusCode(200)
                .extract();

        JSONAssert.assertEquals(expectedJson, response.body().asPrettyString(), false);
    }
}