package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

@DisplayName("Temperature Conditional Tests")
class TemperatureConditionalTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testTemperatureOnWindows() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertEquals("72.0°F (22.2°C)", temp.toString());
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void testTemperatureOnMac() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertEquals("72.0°F (22.2°C)", temp.toString());
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void testTemperatureOnLinux() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertEquals("72.0°F (22.2°C)", temp.toString());
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void testTemperatureNotOnWindows() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertEquals("72.0°F (22.2°C)", temp.toString());
    }

    @Test
    @EnabledOnJre(JRE.JAVA_8)
    void testTemperatureOnJava8() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertTrue(temp.isModerate());
    }

    @Test
    @EnabledOnJre(JRE.JAVA_11)
    void testTemperatureOnJava11() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertTrue(temp.isModerate());
    }

    @Test
    @EnabledOnJre({JRE.JAVA_17, JRE.JAVA_21})
    void testTemperatureOnModernJava() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertTrue(temp.isModerate());
    }

    @Test
    @DisabledOnJre(JRE.JAVA_8)
    void testTemperatureNotOnJava8() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertTrue(temp.isModerate());
    }

    @Test
    @EnabledIfSystemProperty(named = "os.arch", matches = ".*64.*")
    void testTemperatureOn64BitSystem() {
        Temperature temp = Temperature.fromFahrenheit(212.0);
        assertTrue(temp.isHot());
    }

    @Test
    @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
    void testTemperatureNotOn32BitSystem() {
        Temperature temp = Temperature.fromFahrenheit(32.0);
        assertTrue(temp.isCold());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "TEMP_SCALE", matches = "FAHRENHEIT")
    void testTemperatureInFahrenheit() {
        Temperature temp = Temperature.fromFahrenheit(98.6);
        assertEquals(98.6, temp.getFahrenheit());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "TEMP_SCALE", matches = "CELSIUS")
    void testTemperatureInCelsius() {
        Temperature temp = Temperature.fromCelsius(37.0);
        assertEquals(37.0, temp.getCelsius());
    }

    @Test
    @EnabledIf("isWorkingHours")
    void testTemperatureDuringWorkHours() {
        Temperature temp = Temperature.fromFahrenheit(75.0);
        assertTrue(temp.isModerate());
    }

    static boolean isWorkingHours() {
        int hour = LocalDateTime.now().getHour();
        return hour >= 9 && hour <= 17;
    }

    @Test
    @DisabledIf("isWeekend")
    void testTemperatureDuringWeekday() {
        Temperature temp = Temperature.fromFahrenheit(75.0);
        assertTrue(temp.isModerate());
    }

    static boolean isWeekend() {
        int dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7;
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_11, max = JRE.JAVA_21)
    void testTemperatureOnJava11ToJava21() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertTrue(temp.isModerate());
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_11)
    void testTemperatureNotOnOldJava() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertTrue(temp.isModerate());
    }

    @Test
    @EnabledInNativeImage
    void testTemperatureInNativeImage() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertTrue(temp.isModerate());
    }

    @Test
    @DisabledInNativeImage
    void testTemperatureNotInNativeImage() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertTrue(temp.isModerate());
    }

    @Test
    @EnabledIfSystemProperties({
            @EnabledIfSystemProperty(named = "java.vendor", matches = "Oracle.*"),
            @EnabledIfSystemProperty(named = "os.arch", matches = ".*64.*")
    })
    void testTemperatureOnOracle64Bit() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertTrue(temp.isModerate());
    }

    @Test
    @EnabledIfEnvironmentVariables({
            @EnabledIfEnvironmentVariable(named = "TEMP_SCALE", matches = "FAHRENHEIT"),
            @EnabledIfEnvironmentVariable(named = "ENV", matches = "test")
    })
    void testTemperatureWithMultipleEnvConditions() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertTrue(temp.isModerate());
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @EnabledIfSystemProperty(named = "os.arch", matches = ".*64.*")
    @EnabledForJreRange(min = JRE.JAVA_11)
    void testTemperatureWithMultipleConditions() {
        Temperature temp = Temperature.fromFahrenheit(72.0);
        assertTrue(temp.isModerate());
    }
}