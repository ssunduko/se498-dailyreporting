package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.dto.soap.*;
import com.se498.dailyreporting.service.DailyReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Daily Report SOAP Controller Mock Tests")
public class DailyReportSoapControllerMockTest {

    @Mock
    private DailyReportingService reportingService;

    @InjectMocks
    private DailyReportSoapController soapController;

    private DailyReport testReport;
    private ActivityEntry testActivity;
    private String testReportId;
    private String testActivityId;
    private String testProjectId;
    private LocalDate testReportDate;
    private String testUsername;

    @BeforeEach
    void setUp() {
        // Create test data
        testReportId = UUID.randomUUID().toString();
        testActivityId = UUID.randomUUID().toString();
        testProjectId = "test-project-" + UUID.randomUUID();
        testReportDate = LocalDate.now();
        testUsername = "test-user";

        // Create test report
        testReport = new DailyReport();
        testReport.setId(testReportId);
        testReport.setProjectId(testProjectId);
        testReport.setReportDate(testReportDate);
        testReport.setStatus(ReportStatus.DRAFT);
        testReport.setNotes("Test notes");
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setCreatedBy(testUsername);

        // Create test activity
        testActivity = new ActivityEntry();
        testActivity.setId(testActivityId);
        testActivity.setReportId(testReportId);
        testActivity.setDescription("Test activity");
        testActivity.setCategory("Test category");
        testActivity.setStartTime(LocalDateTime.now().minusHours(2));
        testActivity.setEndTime(LocalDateTime.now().minusHours(1));
        testActivity.setProgress(50.0);
        testActivity.setStatus(ActivityStatus.IN_PROGRESS);
        testActivity.setPersonnel(new HashSet<>(Arrays.asList("person1", "person2")));
        testActivity.setCreatedAt(LocalDateTime.now());
        testActivity.setCreatedBy(testUsername);
    }

    @Test
    @DisplayName("Should return report when it exists")
    void getReport_shouldReturnReport_whenReportExists() {
        // Arrange
        when(reportingService.getReport(testReportId)).thenReturn(Optional.of(testReport));

        // Act
        DailyReportSoapResponse response = soapController.getReport(testReportId);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(testReportId, response.getId());
        assertEquals(testProjectId, response.getProjectId());
        assertEquals(testReportDate.toString(), response.getReportDate());
        assertEquals(ReportStatus.DRAFT.name(), response.getStatus());
        assertEquals(testUsername, response.getCreatedBy());

        verify(reportingService).getReport(testReportId);
    }

    @Test
    @DisplayName("Should return error when report does not exist")
    void getReport_shouldReturnError_whenReportDoesNotExist() {
        // Arrange
        when(reportingService.getReport(testReportId)).thenReturn(Optional.empty());

        // Act
        DailyReportSoapResponse response = soapController.getReport(testReportId);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getErrorMessage().contains("Report not found"));

        verify(reportingService).getReport(testReportId);
    }

    @Test
    @DisplayName("Should create report when valid data provided")
    void createReport_shouldCreateReport_whenValidDataProvided() {
        // Arrange
        when(reportingService.createReport(eq(testProjectId), eq(testReportDate), eq(testUsername)))
                .thenReturn(testReport);
        when(reportingService.updateReport(eq(testReportId), anyString(), eq(testUsername)))
                .thenReturn(testReport);

        // Act
        DailyReportSoapResponse response = soapController.createReport(
                testProjectId,
                testReportDate.toString(),
                "Test Notes",
                testUsername
        );

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(testReportId, response.getId());
        assertEquals(testProjectId, response.getProjectId());
        assertEquals(testReportDate.toString(), response.getReportDate());

        verify(reportingService).createReport(eq(testProjectId), eq(testReportDate), eq(testUsername));
        verify(reportingService).updateReport(eq(testReportId), anyString(), eq(testUsername));
    }

    @Test
    @DisplayName("Should handle exception when creating report")
    void createReport_shouldHandleException_whenServiceThrowsException() {
        // Arrange
        when(reportingService.createReport(any(), any(), any()))
                .thenThrow(new IllegalStateException("A report already exists for this project and date"));

        // Act
        DailyReportSoapResponse response = soapController.createReport(
                testProjectId,
                testReportDate.toString(),
                "Test Notes",
                testUsername
        );

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getErrorMessage().contains("Failed to create report"));

        verify(reportingService).createReport(eq(testProjectId), eq(testReportDate), eq(testUsername));
        verify(reportingService, never()).updateReport(any(), any(), any());
    }

    @Test
    @DisplayName("Should submit report successfully")
    void submitReport_shouldSubmitReport_whenReportExists() {
        // Arrange
        when(reportingService.submitReport(testReportId, testUsername)).thenReturn(testReport);

        // Act
        DailyReportSoapResponse response = soapController.submitReport(testReportId, testUsername);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(testReportId, response.getId());
        assertEquals(ReportStatus.DRAFT.name(), response.getStatus());

        verify(reportingService).submitReport(testReportId, testUsername);
    }

    @Test
    @DisplayName("Should delete report successfully")
    void deleteReport_shouldReturnSuccess_whenReportDeleted() {
        // Arrange
        doNothing().when(reportingService).deleteReport(testReportId);

        // Act
        ServiceResponse response = soapController.deleteReport(testReportId);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Report deleted successfully", response.getMessage());

        verify(reportingService).deleteReport(testReportId);
    }

    @Test
    @DisplayName("Should handle exception when deleting report")
    void deleteReport_shouldReturnError_whenDeleteFails() {
        // Arrange
        doThrow(new IllegalArgumentException("Report not found")).when(reportingService).deleteReport(testReportId);

        // Act
        ServiceResponse response = soapController.deleteReport(testReportId);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Failed to delete report"));

        verify(reportingService).deleteReport(testReportId);
    }

    @Test
    @DisplayName("Should return activity when it exists")
    void getActivity_shouldReturnActivity_whenActivityExists() {
        // Arrange
        when(reportingService.getActivity(testActivityId)).thenReturn(Optional.of(testActivity));

        // Act
        ActivitySoapResponse response = soapController.getActivity(testActivityId);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(testActivityId, response.getId());
        assertEquals(testReportId, response.getReportId());
        assertEquals("Test activity", response.getDescription());
        assertEquals("Test category", response.getCategory());
        assertEquals(ActivityStatus.IN_PROGRESS.name(), response.getStatus());
        assertEquals(50.0, response.getProgress());
        assertNotNull(response.getPersonnel());
        assertTrue(response.getPersonnel().contains("person1"));
        assertTrue(response.getPersonnel().contains("person2"));

        verify(reportingService).getActivity(testActivityId);
    }

    @Test
    @DisplayName("Should add activity successfully")
    void addActivity_shouldAddActivity_whenValidDataProvided() {
        // Arrange
        ActivitySoapRequest request = new ActivitySoapRequest();
        request.setReportId(testReportId);
        request.setDescription("New activity");
        request.setCategory("New category");
        request.setStartTime(LocalDateTime.now().minusHours(3).toString());
        request.setEndTime(LocalDateTime.now().minusHours(2).toString());
        request.setProgress(25.0);
        request.setStatus(ActivityStatus.PLANNED.name());
        request.setPersonnel("person1,person2");
        request.setUsername(testUsername);

        when(reportingService.addActivityToReport(eq(testReportId), any(ActivityEntry.class)))
                .thenReturn(testActivity);

        // Act
        ActivitySoapResponse response = soapController.addActivity(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(testActivityId, response.getId());
        assertEquals(testReportId, response.getReportId());

        verify(reportingService).addActivityToReport(eq(testReportId), any(ActivityEntry.class));
    }

    @Test
    @DisplayName("Should return report progress")
    void getReportProgress_shouldReturnProgress_whenReportExists() {
        // Arrange
        when(reportingService.calculateReportProgress(testReportId)).thenReturn(75.0);

        // Act
        ProgressResponse response = soapController.getReportProgress(testReportId);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(75.0, response.getProgress());
        assertTrue(response.getMessage().contains("calculated successfully"));

        verify(reportingService).calculateReportProgress(testReportId);
    }

    @Test
    @DisplayName("Should return report completion status")
    void isReportComplete_shouldReturnCompletionStatus_whenReportExists() {
        // Arrange
        when(reportingService.isReportComplete(testReportId)).thenReturn(true);

        // Act
        CompletionResponse response = soapController.isReportComplete(testReportId);

        // Assert
        assertTrue(response.isSuccess());
        assertTrue(response.isComplete());
        assertTrue(response.getMessage().contains("retrieved successfully"));

        verify(reportingService).isReportComplete(testReportId);
    }

    @Test
    @DisplayName("Should return activity duration")
    void getTotalActivityDuration_shouldReturnDuration_whenReportExists() {
        // Arrange
        long expectedDuration = 120; // 2 hours in minutes
        when(reportingService.getTotalActivityDurationMinutes(testReportId)).thenReturn(expectedDuration);

        // Act
        DurationResponse response = soapController.getTotalActivityDuration(testReportId);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(expectedDuration, response.getDurationMinutes());
        assertTrue(response.getMessage().contains("calculated successfully"));

        verify(reportingService).getTotalActivityDurationMinutes(testReportId);
    }

    @Test
    @DisplayName("Should return activities by report")
    void getActivitiesByReport_shouldReturnActivities_whenReportExists() {
        // Arrange
        List<ActivityEntry> activities = Collections.singletonList(testActivity);
        when(reportingService.getActivitiesByReport(testReportId)).thenReturn(activities);

        // Act
        ActivityListResponse response = soapController.getActivitiesByReport(testReportId);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(1, response.getActivityCount());
        assertEquals(1, response.getActivities().size());
        assertEquals(testActivityId, response.getActivities().get(0).getId());

        verify(reportingService).getActivitiesByReport(testReportId);
    }


}
