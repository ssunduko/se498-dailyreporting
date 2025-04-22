package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.TestDailyReportingApplication;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {TestDailyReportingApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthTest {

    @LocalServerPort
    private Integer port;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }

    @Test
    void testAuth() {
        RestAssured
                .given()
                .filter(new RequestLoggingFilter())
                .auth().basic("sergey", "chapman") // Use credentials from application properties
                .contentType(ContentType.JSON)
                .then()
                .statusCode(200);
    }

    @Test
    public void testRetrieveWeatherRecord() {
        RestAssured
                .given()
                .filter(new RequestLoggingFilter())
                .auth().basic("sergey", "chapman")
                .contentType(ContentType.JSON)
                .when()
                .get("/weather/current?zip=90210")
                .then()
                .statusCode(200)
                .extract();
    }
}