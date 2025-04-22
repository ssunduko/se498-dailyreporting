package com.se498.dailyreporting.repository;

import com.se498.dailyreporting.TestDailyReportingApplication;
import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest(classes = {TestDailyReportingApplication.class})
@ActiveProfiles("test")
public class ActivityEntryRepositoryTest {

    @Mock
    private ActivityEntryRepository activityEntryRepository;

    private ActivityEntry activity1;
    private ActivityEntry activity2;
    private ActivityEntry activity3;

    @BeforeEach
    public void setup() {
        // Initialize test data
        activity1 = new ActivityEntry();
        activity1.setId("act-001");
        activity1.setReportId("rep-001");
        activity1.setCategory("Construction");
        activity1.setStatus(ActivityStatus.COMPLETED);
        activity1.setStartTime(LocalDateTime.of(2023, 1, 10, 9, 0));
        activity1.setEndTime(LocalDateTime.of(2023, 1, 10, 17, 0));

        activity2 = new ActivityEntry();
        activity2.setId("act-002");
        activity2.setReportId("rep-001");
        activity2.setCategory("Plumbing");
        activity2.setStatus(ActivityStatus.IN_PROGRESS);
        activity2.setStartTime(LocalDateTime.of(2023, 1, 11, 8, 0));
        activity2.setEndTime(LocalDateTime.of(2023, 1, 11, 16, 0));

        activity3 = new ActivityEntry();
        activity3.setId("act-003");
        activity3.setReportId("rep-002");
        activity3.setCategory("Construction");
        activity3.setStatus(ActivityStatus.COMPLETED);
        activity3.setStartTime(LocalDateTime.of(2023, 1, 12, 10, 0));
        activity3.setEndTime(LocalDateTime.of(2023, 1, 12, 18, 0));
    }

    @Test
    public void testFindByReportId() {
        // Given
        String reportId = "rep-001";
        List<ActivityEntry> expectedActivities = Arrays.asList(activity1, activity2);
        when(activityEntryRepository.findByReportId(reportId)).thenReturn(expectedActivities);

        // When
        List<ActivityEntry> actualActivities = activityEntryRepository.findByReportId(reportId);

        // Then
        assertEquals(2, actualActivities.size());
        assertTrue(actualActivities.contains(activity1));
        assertTrue(actualActivities.contains(activity2));
        verify(activityEntryRepository, times(1)).findByReportId(reportId);
    }

    @Test
    public void testFindByStatus() {
        // Given
        when(activityEntryRepository.findByStatus(ActivityStatus.COMPLETED))
                .thenReturn(Arrays.asList(activity1, activity3));

        // When
        List<ActivityEntry> completedActivities = activityEntryRepository.findByStatus(ActivityStatus.COMPLETED);

        // Then
        assertEquals(2, completedActivities.size());
        assertTrue(completedActivities.contains(activity1));
        assertTrue(completedActivities.contains(activity3));
        verify(activityEntryRepository, times(1)).findByStatus(ActivityStatus.COMPLETED);
    }

    @Test
    public void testDeleteByReportId() {
        // Given
        String reportId = "rep-001";
        doNothing().when(activityEntryRepository).deleteByReportId(reportId);

        // When
        activityEntryRepository.deleteByReportId(reportId);

        // Then
        verify(activityEntryRepository, times(1)).deleteByReportId(reportId);
    }

    @Test
    public void testFindByTimeRange() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2023, 1, 10, 8, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 1, 11, 18, 0);
        when(activityEntryRepository.findByTimeRange(startTime, endTime))
                .thenReturn(Arrays.asList(activity1, activity2));

        // When
        List<ActivityEntry> activitiesInRange = activityEntryRepository.findByTimeRange(startTime, endTime);

        // Then
        assertEquals(2, activitiesInRange.size());
        assertTrue(activitiesInRange.contains(activity1));
        assertTrue(activitiesInRange.contains(activity2));
        verify(activityEntryRepository, times(1)).findByTimeRange(startTime, endTime);
    }

    @Test
    public void testFindByReportIdAndCategory() {
        // Given
        String reportId = "rep-001";
        String category = "Construction";
        when(activityEntryRepository.findByReportIdAndCategory(reportId, category))
                .thenReturn(Collections.singletonList(activity1));

        // When
        List<ActivityEntry> filteredActivities = activityEntryRepository.findByReportIdAndCategory(reportId, category);

        // Then
        assertEquals(1, filteredActivities.size());
        assertEquals(activity1, filteredActivities.get(0));
        verify(activityEntryRepository, times(1)).findByReportIdAndCategory(reportId, category);
    }

    @Test
    public void testCountCompletedActivitiesByReportId() {
        // Given
        String reportId = "rep-001";
        when(activityEntryRepository.countCompletedActivitiesByReportId(reportId)).thenReturn(1L);

        // When
        long completedCount = activityEntryRepository.countCompletedActivitiesByReportId(reportId);

        // Then
        assertEquals(1L, completedCount);
        verify(activityEntryRepository, times(1)).countCompletedActivitiesByReportId(reportId);
    }

    @Test
    public void testFindByTimeRange_NoResults() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2023, 2, 1, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 2, 2, 17, 0);
        when(activityEntryRepository.findByTimeRange(startTime, endTime))
                .thenReturn(Collections.emptyList());

        // When
        List<ActivityEntry> activitiesInRange = activityEntryRepository.findByTimeRange(startTime, endTime);

        // Then
        assertTrue(activitiesInRange.isEmpty());
        verify(activityEntryRepository, times(1)).findByTimeRange(startTime, endTime);
    }

    @Test
    public void testFindByReportIdAndCategory_NoResults() {
        // Given
        String reportId = "rep-001";
        String category = "Electrical";
        when(activityEntryRepository.findByReportIdAndCategory(reportId, category))
                .thenReturn(Collections.emptyList());

        // When
        List<ActivityEntry> filteredActivities = activityEntryRepository.findByReportIdAndCategory(reportId, category);

        // Then
        assertTrue(filteredActivities.isEmpty());
        verify(activityEntryRepository, times(1)).findByReportIdAndCategory(reportId, category);
    }
}