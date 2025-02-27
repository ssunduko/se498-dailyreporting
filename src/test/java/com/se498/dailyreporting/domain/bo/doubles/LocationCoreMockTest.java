package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.doubles.LocationStub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationCoreMockTest {

    @Mock
    private LocationStub locationStub;

    @Test
    void testCoreMockingMethods() {
        // 1. Define mock behavior with when/thenReturn
        when(locationStub.getCity()).thenReturn("MockCity");
        assertEquals("MockCity", locationStub.getCity());
        verify(locationStub, times(1)).getCity();

        // 2. Define mock behavior with doReturn/when
        doReturn("MockState").when(locationStub).getStateOrProvince();
        assertEquals("MockState", locationStub.getStateOrProvince());
        verify(locationStub, times(1)).getStateOrProvince();

        // 3. Verify with argument matchers
        when(locationStub.confirmLocation(any(Location.class))).thenReturn(true);
        assertTrue(locationStub.confirmLocation(new LocationStub()));
        verify(locationStub, times(1)).confirmLocation(any(Location.class));

        // 4. Verify order of invocations
        reset(locationStub);
        when(locationStub.getCity()).thenReturn("MockCity");
        when(locationStub.getStateOrProvince()).thenReturn("MockState");

        locationStub.getCity();
        locationStub.getStateOrProvince();

        InOrder inOrder = inOrder(locationStub);
        inOrder.verify(locationStub).getCity();
        inOrder.verify(locationStub).getStateOrProvince();

        // 5. Test exception throwing
        reset(locationStub);
        doThrow(new RuntimeException("Test Exception"))
                .when(locationStub).getCountry();
        assertThrows(RuntimeException.class, () -> locationStub.getCountry());

        // 6. Test with answer interface
        when(locationStub.getZipCode()).thenAnswer(invocation -> "12345");
        assertEquals("12345", locationStub.getZipCode());
        verify(locationStub, times(1)).getZipCode();
    }

    @Test
    void testVerificationModes() {
        // Setup
        when(locationStub.getCity()).thenReturn("MockCity");

        // At least once
        locationStub.getCity();
        verify(locationStub, atLeastOnce()).getCity();

        // Exact number of times
        locationStub.getCity();
        verify(locationStub, times(2)).getCity();

        // Never
        verify(locationStub, never()).getCountry();

        // At most
        verify(locationStub, atMost(2)).getCity();

        // At least
        verify(locationStub, atLeast(1)).getCity();
    }

    @Test
    void testArgumentMatchers() {
        // Any matcher
        when(locationStub.confirmLocation(any())).thenReturn(true);
        assertTrue(locationStub.confirmLocation(new LocationStub()));
        verify(locationStub, times(1)).confirmLocation(any());

        // Specific argument matcher
        reset(locationStub);
        Location specificLocation = new LocationStub();
        when(locationStub.confirmLocation(eq(specificLocation))).thenReturn(true);
        assertTrue(locationStub.confirmLocation(specificLocation));
        verify(locationStub, times(1)).confirmLocation(eq(specificLocation));
    }

    @Test
    void testConsecutiveCalls() {
        // Multiple return values
        when(locationStub.getCity())
                .thenReturn("First")
                .thenReturn("Second")
                .thenReturn("Third");

        assertEquals("First", locationStub.getCity());
        assertEquals("Second", locationStub.getCity());
        assertEquals("Third", locationStub.getCity());
        assertEquals("Third", locationStub.getCity()); // Subsequent calls return last value

        verify(locationStub, times(4)).getCity();
    }

    @Test
    void testSpying() {
        // Create spy of real object
        LocationStub spyLocation = spy(new LocationStub());

        // Override specific methods
        doReturn("SpyCity").when(spyLocation).getCity();

        // Verify spy behavior
        assertEquals("SpyCity", spyLocation.getCity());
        assertEquals("StubState", spyLocation.getStateOrProvince()); // Original behavior

        verify(spyLocation, times(1)).getCity();
        verify(spyLocation, times(1)).getStateOrProvince();
    }
}
