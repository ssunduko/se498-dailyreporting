package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Location;
import com.se498.dailyreporting.domain.bo.doubles.LocationStub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationVerificationModesMockTest {

    @Mock
    private LocationStub locationStub;

    @Test
    void testVerifyAtLeastOnce() {
        // Given
        when(locationStub.getCity()).thenReturn("StubCity");

        // When
        locationStub.getCity();

        // Then
        verify(locationStub, atLeastOnce()).getCity();
    }

    @Test
    void testVerifyAtLeast() {
        // Given
        when(locationStub.getCity()).thenReturn("StubCity");

        // When
        locationStub.getCity();
        locationStub.getCity();
        locationStub.getCity();

        // Then
        verify(locationStub, atLeast(2)).getCity();
    }

    @Test
    void testVerifyAtMost() {
        // Given
        when(locationStub.getCity()).thenReturn("StubCity");

        // When
        locationStub.getCity();
        locationStub.getCity();

        // Then
        verify(locationStub, atMost(3)).getCity();
    }

    @Test
    void testVerifyTimes() {
        // Given
        when(locationStub.getCity()).thenReturn("StubCity");

        // When
        locationStub.getCity();
        locationStub.getCity();

        // Then
        verify(locationStub, times(2)).getCity();
    }


    @Test
    void testVerifyNoInteractions() {
        // Then
        verifyNoInteractions(locationStub);
    }


    @Test
    void testVerifyTimeout() {
        // Given
        when(locationStub.confirmLocation(any(Location.class))).thenReturn(true);

        // When
        locationStub.confirmLocation(new LocationStub());

        // Then
        verify(locationStub, timeout(100)).confirmLocation(any(Location.class));
    }

    @Test
    void testVerifyDescription() {
        // Given
        when(locationStub.getCity()).thenReturn("StubCity");

        // When
        locationStub.getCity();

        // Then
        verify(locationStub, description("City method should be called once")).getCity();
    }
}