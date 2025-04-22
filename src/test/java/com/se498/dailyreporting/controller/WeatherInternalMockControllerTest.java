package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.TestDailyReportingApplication;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(classes = TestDailyReportingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WeatherInternalMockControllerTest {

    @BeforeAll
    static void init() {
        ClientAndServer.startClientAndServer(1090);

        new MockServerClient("localhost", 1090)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/weather/current/zip/90210"),
                        Times.unlimited(),
                        TimeToLive.unlimited(),
                        0
                )
                .respond(
                        response()
                                .withBody("{\n \"location\" : \"Beverly Hills, US\", \"temperature\" : {\"fahrenheit\" : 72.5, \"celsius\" : 22.5}, \"condition\" : {\"description\" : \"Clear\", \"iconCode\" : \"01d\"}\n}")
                );
    }

    @Test
    void testGetWeatherByZip() throws JSONException {
        String expectedJson = "{\"location\":\"Beverly Hills, US\",\"temperature\":{\"fahrenheit\":72.5,\"celsius\":22.5},\"condition\":{\"description\":\"Clear\",\"iconCode\":\"01d\"}}";

        ExtractableResponse<Response> response = RestAssured
                .given()
                .filter(new RequestLoggingFilter())
                .auth().basic("sergey", "chapman")
                .contentType(ContentType.JSON)
                .when()
                .get("http://localhost:1090/weather/current/zip/90210")
                .then()
                .statusCode(200)
                .extract();

        JSONAssert.assertEquals(expectedJson, response.body().asPrettyString(), true);
    }
}