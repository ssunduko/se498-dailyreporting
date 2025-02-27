package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.doubles.LocationStub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherRecordSpyTest {

    @Spy
    private LocationStub locationStub;

    @Test
    void testRealMethodCall() {
        // Given
        doCallRealMethod().when(locationStub).getCity();

        // When
        String result = locationStub.getCity();

        // Then
        assertEquals("StubCity", result);
        verify(locationStub, times(1)).getCity();
    }

    @Test
    void testPartialMocking() {
        // Given
        doCallRealMethod().when(locationStub).getCity();
        doReturn("MockState").when(locationStub).getStateOrProvince();

        // When
        String city = locationStub.getCity();
        String state = locationStub.getStateOrProvince();

        // Then
        assertEquals("StubCity", city);  // Real method call
        assertEquals("MockState", state); // Mocked call
        verify(locationStub).getCity();
        verify(locationStub).getStateOrProvince();
    }

    @Test
    void testRealMethodWithConfirmLocation() {
        // Given
        Location testLocation = new LocationStub();
        doCallRealMethod().when(locationStub).confirmLocation(any(Location.class));

        // When
        boolean result = locationStub.confirmLocation(testLocation);

        // Then
        assertFalse(result); // Real method always returns false
        verify(locationStub).confirmLocation(testLocation);
    }

    @Test
    void testAllRealMethods() {
        // Given
        doCallRealMethod().when(locationStub).getCity();
        doCallRealMethod().when(locationStub).getStateOrProvince();
        doCallRealMethod().when(locationStub).getCountry();
        doCallRealMethod().when(locationStub).getZipCode();
        doCallRealMethod().when(locationStub).toString();

        // When
        String city = locationStub.getCity();
        String state = locationStub.getStateOrProvince();
        String country = locationStub.getCountry();
        String zipCode = locationStub.getZipCode();
        String toString = locationStub.toString();

        // Then
        assertEquals("StubCity", city);
        assertEquals("StubState", state);
        assertEquals("SC", country);
        assertEquals("12345", zipCode);
        assertEquals("StubCity, StubState 12345, SC", toString);

        verify(locationStub).getCity();
        verify(locationStub).getStateOrProvince();
        verify(locationStub).getCountry();
        verify(locationStub).getZipCode();
    }

    @Test
    void testMixedRealAndMockedMethods() {
        // Given
        doCallRealMethod().when(locationStub).getCity();
        doCallRealMethod().when(locationStub).getCountry();
        doReturn("MockState").when(locationStub).getStateOrProvince();
        doReturn("99999").when(locationStub).getZipCode();

        // When
        String city = locationStub.getCity();
        String state = locationStub.getStateOrProvince();
        String country = locationStub.getCountry();
        String zipCode = locationStub.getZipCode();

        // Then
        assertEquals("StubCity", city);    // Real method
        assertEquals("MockState", state);   // Mocked
        assertEquals("SC", country);        // Real method
        assertEquals("99999", zipCode);     // Mocked

        verify(locationStub).getCity();
        verify(locationStub).getStateOrProvince();
        verify(locationStub).getCountry();
        verify(locationStub).getZipCode();
    }
}