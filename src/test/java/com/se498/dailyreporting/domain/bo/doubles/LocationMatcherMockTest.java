package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.doubles.LocationStub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationMatcherMockTest {

    @Mock
    private LocationStub locationStub;

    @Test
    void testAnyMatcher() {
        // Given
        when(locationStub.confirmLocation(any(Location.class))).thenReturn(true);

        // When
        boolean result = locationStub.confirmLocation(new LocationStub());

        // Then
        assertTrue(result);
        verify(locationStub).confirmLocation(any(Location.class));
    }

    @Test
    void testEqMatcher() {
        // Given
        Location specificLocation = new LocationStub();
        when(locationStub.confirmLocation(eq(specificLocation))).thenReturn(true);

        // When
        boolean result = locationStub.confirmLocation(specificLocation);

        // Then
        assertTrue(result);
        verify(locationStub).confirmLocation(eq(specificLocation));
    }

    @Test
    void testNullMatcher() {
        // Given
        when(locationStub.confirmLocation(isNull())).thenReturn(false);

        // When
        boolean result = locationStub.confirmLocation(null);

        // Then
        assertFalse(result);
        verify(locationStub).confirmLocation(isNull());
    }

    @Test
    void testNotNullMatcher() {
        // Given
        when(locationStub.confirmLocation(notNull())).thenReturn(true);

        // When
        boolean result = locationStub.confirmLocation(new LocationStub());

        // Then
        assertTrue(result);
        verify(locationStub).confirmLocation(notNull());
    }

    @Test
    void testCombinedMatchers() {
        // Given
        when(locationStub.getCity()).thenReturn("New York");
        when(locationStub.getCountry()).thenReturn("US");
        when(locationStub.getStateOrProvince()).thenReturn("NY");
        when(locationStub.getZipCode()).thenReturn("10001");

        // When & Then
        assertEquals("New York", locationStub.getCity());
        assertEquals("US", locationStub.getCountry());
        assertEquals("NY", locationStub.getStateOrProvince());
        assertEquals("10001", locationStub.getZipCode());

        // Verify with multiple matchers
        verify(locationStub, times(1)).getCity();
        verify(locationStub, atLeastOnce()).getCountry();
        verify(locationStub, atMost(1)).getStateOrProvince();
        verify(locationStub, times(1)).getZipCode();
    }

    @Test
    void testStringMatchers() {
        // Given
        when(locationStub.getCity()).thenReturn("TestCity");
        when(locationStub.getStateOrProvince()).thenReturn("TestState");

        // When
        String city = locationStub.getCity();
        String state = locationStub.getStateOrProvince();

        // Then
        assertEquals("TestCity", city);
        assertEquals("TestState", state);
        verify(locationStub).getCity();
        verify(locationStub).getStateOrProvince();
    }
}