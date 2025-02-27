package com.se498.dailyreporting.domain.bo;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Weather Measurement Test Suite")
@SelectPackages("com.se498.dailyreporting.domain.bo")
@SelectClasses({
        TemperatureTest.class,
        HumidityTest.class,
})
class WeatherTestSuite {
    // Test suite configuration only - no test methods here
}