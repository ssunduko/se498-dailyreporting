package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.doubles.LocationStub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationFreshMockTest {

    @Mock
    private LocationStub locationStub;

    @Test
    void testResetMock() {
        // Given
        when(locationStub.getCity()).thenReturn("StubCity");
        locationStub.getCity();
        verify(locationStub).getCity();

        // When
        reset(locationStub);

        // Then
        verifyNoMoreInteractions(locationStub);
    }

    @Test
    void testResetAndNewStubbing() {
        // Given
        when(locationStub.getCity()).thenReturn("StubCity");
        assertEquals("StubCity", locationStub.getCity());
        verify(locationStub).getCity();

        // When
        reset(locationStub);
        when(locationStub.getCity()).thenReturn("NewCity");

        // Then
        assertEquals("NewCity", locationStub.getCity());
        verify(locationStub).getCity();
    }

    @Test
    void testClearInvocations() {
        // Given
        when(locationStub.getCity()).thenReturn("StubCity");
        locationStub.getCity();
        verify(locationStub).getCity();

        // When
        clearInvocations(locationStub);

        // Then
        verifyNoMoreInteractions(locationStub);
        assertEquals("StubCity", locationStub.getCity()); // Stubbing remains
        verify(locationStub).getCity();
    }

    @Test
    void testClearInvocationsButKeepStubbing() {
        // Given
        when(locationStub.getCity()).thenReturn("StubCity");
        when(locationStub.confirmLocation(any(Location.class))).thenReturn(false);

        locationStub.getCity();
        locationStub.confirmLocation(new LocationStub());

        verify(locationStub).getCity();
        verify(locationStub).confirmLocation(any(Location.class));

        // When
        clearInvocations(locationStub);

        // Then
        verifyNoMoreInteractions(locationStub);

        // Stubbing still works
        assertEquals("StubCity", locationStub.getCity());
        assertFalse(locationStub.confirmLocation(new LocationStub()));

        // New verifications after clearInvocations
        verify(locationStub).getCity();
        verify(locationStub).confirmLocation(any(Location.class));
    }

    @Test
    void testResetAllInteractions() {
        // Given
        when(locationStub.getCity()).thenReturn("StubCity");
        when(locationStub.getCountry()).thenReturn("SC");

        locationStub.getCity();
        locationStub.getCountry();

        verify(locationStub).getCity();
        verify(locationStub).getCountry();

        // When
        reset(locationStub);

        // Then
        verifyNoInteractions(locationStub);

        // Then
        verifyNoMoreInteractions(locationStub);

        // Previous stubbing is gone
        assertNull(locationStub.getCity());
        assertNull(locationStub.getCountry());
    }
}