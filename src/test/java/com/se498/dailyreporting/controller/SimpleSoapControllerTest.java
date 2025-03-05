package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SimpleSoapControllerTest {

    @Mock
    private DailyReportingService reportingService;

    @InjectMocks
    private DailyReportSoapController soapController;

    private DailyReport testReport;
    private ActivityEntry testActivity;
    private final String TEST_PROJECT_ID = "project-123";
    private final String TEST_REPORT_ID = UUID.randomUUID().toString();
    private final String TEST_ACTIVITY_ID = UUID.randomUUID().toString();
    private final String TEST_USERNAME = "testuser";
    private final LocalDate TEST_DATE = LocalDate.now();

    @BeforeEach
    void setUp() {
        // Create test report
        testReport = new DailyReport();
        testReport.setId(TEST_REPORT_ID);
        testReport.setProjectId(TEST_PROJECT_ID);
        testReport.setReportDate(TEST_DATE);
        testReport.setStatus(ReportStatus.DRAFT);
        testReport.setNotes("Test Notes");
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setCreatedBy(TEST_USERNAME);

        // Create test activity
        testActivity = new ActivityEntry();
        testActivity.setId(TEST_ACTIVITY_ID);
        testActivity.setReportId(TEST_REPORT_ID);
        testActivity.setDescription("Test Activity");
        testActivity.setCategory("Construction");
        testActivity.setStartTime(LocalDateTime.now().minusHours(2));
        testActivity.setEndTime(LocalDateTime.now());
        testActivity.setProgress(50.0);
        testActivity.setStatus(ActivityStatus.IN_PROGRESS);
        testActivity.setNotes("Activity notes");
        testActivity.setCreatedAt(LocalDateTime.now());
        testActivity.setCreatedBy(TEST_USERNAME);

        Set<String> personnel = new HashSet<>();
        personnel.add("Person 1");
        personnel.add("Person 2");
        testActivity.setPersonnel(personnel);

        // Add activity to report
        List<ActivityEntry> activities = new ArrayList<>();
        activities.add(testActivity);
        testReport.setActivities(activities);
    }

    @Test
    void testCreateReport() {
        // Arrange
        // Be more lenient with argument matching to ensure the mock responds
        when(reportingService.createReport(anyString(), any(LocalDate.class), anyString()))
                .thenReturn(testReport);

        // Also mock the updateReport since the controller calls it with notes
        when(reportingService.updateReport(anyString(), anyString(), anyString()))
                .thenReturn(testReport);

        DailyReportSoapController.CreateReportRequest request = new DailyReportSoapController.CreateReportRequest();
        request.setProjectId(TEST_PROJECT_ID);
        request.setReportDate(TEST_DATE);
        request.setNotes("Test Notes");
        request.setUsername(TEST_USERNAME);

        // Act
        DailyReportSoapController.DailyReportSoapResponse response = soapController.createReport(request);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_REPORT_ID, response.getId());
        assertEquals(TEST_PROJECT_ID, response.getProjectId());
        assertEquals(TEST_DATE, response.getReportDate());
        assertEquals(ReportStatus.DRAFT.name(), response.getStatus());

        // Verify service was called - use any() matchers to be consistent with mock setup
        verify(reportingService).createReport(anyString(), any(LocalDate.class), anyString());

        // Since we have notes, updateReport should be called too
        verify(reportingService).updateReport(anyString(), anyString(), anyString());
    }

    @Test
    void testGetReport() {
        // Arrange
        when(reportingService.getReport(TEST_REPORT_ID)).thenReturn(Optional.of(testReport));

        // Act
        DailyReportSoapController.DailyReportSoapResponse response = soapController.getReport(TEST_REPORT_ID);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_REPORT_ID, response.getId());
        assertNotNull(response.getActivities());
        assertEquals(1, response.getActivities().size());

        // Verify activity mapping
        DailyReportSoapController.ActivityEntrySoapResponse activityResponse = response.getActivities().get(0);
        assertEquals(TEST_ACTIVITY_ID, activityResponse.getId());
        assertEquals("Test Activity", activityResponse.getDescription());
        assertEquals(50.0, activityResponse.getProgress());
        assertEquals(ActivityStatus.IN_PROGRESS.name(), activityResponse.getStatus());

        // Verify service was called
        verify(reportingService).getReport(TEST_REPORT_ID);
    }

    @Test
    void testUpdateReport() {
        // Arrange
        when(reportingService.updateReport(eq(TEST_REPORT_ID), anyString(), eq(TEST_USERNAME)))
                .thenReturn(testReport);

        DailyReportSoapController.UpdateReportRequest request = new DailyReportSoapController.UpdateReportRequest();
        request.setReportId(TEST_REPORT_ID);
        request.setNotes("Updated Notes");
        request.setUsername(TEST_USERNAME);

        // Act
        DailyReportSoapController.DailyReportSoapResponse response = soapController.updateReport(request);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_REPORT_ID, response.getId());

        // Verify service was called
        verify(reportingService).updateReport(TEST_REPORT_ID, "Updated Notes", TEST_USERNAME);
    }

    @Test
    void testSubmitReport() {
        // Arrange
        testReport.setStatus(ReportStatus.SUBMITTED);
        when(reportingService.submitReport(eq(TEST_REPORT_ID), eq(TEST_USERNAME)))
                .thenReturn(testReport);

        DailyReportSoapController.SubmitReportRequest request = new DailyReportSoapController.SubmitReportRequest();
        request.setReportId(TEST_REPORT_ID);
        request.setUsername(TEST_USERNAME);

        // Act
        DailyReportSoapController.DailyReportSoapResponse response = soapController.submitReport(request);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_REPORT_ID, response.getId());
        assertEquals(ReportStatus.SUBMITTED.name(), response.getStatus());

        // Verify service was called
        verify(reportingService).submitReport(TEST_REPORT_ID, TEST_USERNAME);
    }

    @Test
    void testAddActivity() {
        // Arrange
        when(reportingService.addActivityToReport(eq(TEST_REPORT_ID), any(ActivityEntry.class)))
                .thenReturn(testActivity);

        DailyReportSoapController.AddActivityRequest request = new DailyReportSoapController.AddActivityRequest();
        request.setReportId(TEST_REPORT_ID);
        request.setDescription("Test Activity");
        request.setCategory("Construction");
        request.setStartTime(LocalDateTime.now().minusHours(2));
        request.setEndTime(LocalDateTime.now());
        request.setProgress(50.0);
        request.setStatus(ActivityStatus.IN_PROGRESS.name());
        request.setNotes("Activity notes");
        request.setUsername(TEST_USERNAME);

        List<String> personnel = new ArrayList<>();
        personnel.add("Person 1");
        personnel.add("Person 2");
        request.setPersonnel(personnel);

        // Act
        DailyReportSoapController.ActivityEntrySoapResponse response = soapController.addActivity(request);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_ACTIVITY_ID, response.getId());
        assertEquals(TEST_REPORT_ID, response.getReportId());
        assertEquals("Test Activity", response.getDescription());
        assertEquals(50.0, response.getProgress());

        // Verify service was called
        verify(reportingService).addActivityToReport(eq(TEST_REPORT_ID), any(ActivityEntry.class));
    }

    @Test
    void testGetReportsByProject() {
        // Arrange
        List<DailyReport> reports = new ArrayList<>();
        reports.add(testReport);

        when(reportingService.getReportsByProject(TEST_PROJECT_ID)).thenReturn(reports);

        DailyReportSoapController.GetReportsByProjectRequest request = new DailyReportSoapController.GetReportsByProjectRequest();
        request.setProjectId(TEST_PROJECT_ID);

        // Act
        List<DailyReportSoapController.DailyReportSoapResponse> responses = soapController.getReportsByProject(request);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        DailyReportSoapController.DailyReportSoapResponse response = responses.get(0);
        assertEquals(TEST_REPORT_ID, response.getId());

        // Verify service was called
        verify(reportingService).getReportsByProject(TEST_PROJECT_ID);
    }

    @Test
    void testGetActivitiesByReport() {
        // Arrange
        List<ActivityEntry> activities = new ArrayList<>();
        activities.add(testActivity);

        when(reportingService.getActivitiesByReport(TEST_REPORT_ID)).thenReturn(activities);

        // Act
        List<DailyReportSoapController.ActivityEntrySoapResponse> responses = soapController.getActivitiesByReport(TEST_REPORT_ID);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        DailyReportSoapController.ActivityEntrySoapResponse response = responses.get(0);
        assertEquals(TEST_ACTIVITY_ID, response.getId());
        assertEquals("Test Activity", response.getDescription());

        // Verify service was called
        verify(reportingService).getActivitiesByReport(TEST_REPORT_ID);
    }

    @Test
    void testGetReportProgress() {
        // Arrange
        double expectedProgress = 75.0;
        when(reportingService.calculateReportProgress(TEST_REPORT_ID)).thenReturn(expectedProgress);

        // Act
        double progress = soapController.getReportProgress(TEST_REPORT_ID);

        // Assert
        assertEquals(expectedProgress, progress);

        // Verify service was called
        verify(reportingService).calculateReportProgress(TEST_REPORT_ID);
    }

    @Test
    void testIsReportComplete() {
        // Arrange
        when(reportingService.isReportComplete(TEST_REPORT_ID)).thenReturn(true);

        // Act
        boolean isComplete = soapController.isReportComplete(TEST_REPORT_ID);

        // Assert
        assertTrue(isComplete);

        // Verify service was called
        verify(reportingService).isReportComplete(TEST_REPORT_ID);
    }

    @Test
    void testGetTotalDuration() {
        // Arrange
        long expectedDuration = 120;
        when(reportingService.getTotalActivityDurationMinutes(TEST_REPORT_ID)).thenReturn(expectedDuration);

        // Act
        long duration = soapController.getTotalDuration(TEST_REPORT_ID);

        // Assert
        assertEquals(expectedDuration, duration);

        // Verify service was called
        verify(reportingService).getTotalActivityDurationMinutes(TEST_REPORT_ID);
    }
}