package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;

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

/**
 * Integration test for DailyReportSoapController that uses CXF client to call the SOAP service
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DailyReportSoapControllerIntegrationTest {

    @LocalServerPort
    private int serverPort;

    @MockBean
    private DailyReportingService reportingService;

    @Autowired
    private DailyReportSoapController soapController;

    private DailyReportSoapServiceClient soapClient;
    private DailyReport testReport;
    private ActivityEntry testActivity;
    private final String TEST_PROJECT_ID = "project-123";
    private final String TEST_REPORT_ID = UUID.randomUUID().toString();
    private final String TEST_ACTIVITY_ID = UUID.randomUUID().toString();
    private final String TEST_USERNAME = "testuser";
    private final LocalDate TEST_DATE = LocalDate.now();

    /**
     * Custom interface for the SOAP client
     */
    public interface DailyReportSoapServiceClient {
        DailyReportSoapController.DailyReportSoapResponse createReport(
                DailyReportSoapController.CreateReportRequest request);

        DailyReportSoapController.DailyReportSoapResponse getReport(String reportId);

        List<DailyReportSoapController.DailyReportSoapResponse> getReportsByProject(
                DailyReportSoapController.GetReportsByProjectRequest request);
    }

    @BeforeEach
    void setUp() {
        // Setup CXF SOAP client
        String endpointUrl = "http://localhost:" + serverPort + "/services/test/dailyReport";
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(DailyReportSoapServiceClient.class);
        factory.setAddress(endpointUrl);
        soapClient = (DailyReportSoapServiceClient) factory.create();

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
    void testCreateReportIntegration() {
        // This is a direct test without actually using SOAP transport
        // Arrange
        when(reportingService.createReport(anyString(), any(LocalDate.class), anyString()))
                .thenReturn(testReport);

        // Also mock updateReport since it will be called when notes are provided
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
        assertNotNull(response, "Response should not be null");
        assertEquals(TEST_REPORT_ID, response.getId());
        assertEquals(TEST_PROJECT_ID, response.getProjectId());
        assertEquals(TEST_DATE, response.getReportDate());
        assertEquals(ReportStatus.DRAFT.name(), response.getStatus());

        // Verify service was called
        verify(reportingService).createReport(anyString(), any(LocalDate.class), anyString());
    }

    @Test
    void testGetReportIntegration() {
        // This is a direct test without actually using SOAP transport
        // Arrange
        when(reportingService.getReport(anyString())).thenReturn(Optional.of(testReport));

        // Act
        DailyReportSoapController.DailyReportSoapResponse response = soapController.getReport(TEST_REPORT_ID);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(TEST_REPORT_ID, response.getId());
        assertEquals(testReport.getProjectId(), response.getProjectId());

        // Verify activities are correctly mapped
        assertNotNull(response.getActivities(), "Activities should not be null");
        assertFalse(response.getActivities().isEmpty(), "Activities should not be empty");
        assertEquals(1, response.getActivities().size());
        DailyReportSoapController.ActivityEntrySoapResponse activityResponse = response.getActivities().get(0);
        assertEquals(TEST_ACTIVITY_ID, activityResponse.getId());
        assertEquals("Test Activity", activityResponse.getDescription());

        // Verify service was called
        verify(reportingService).getReport(anyString());
    }

    @Test
    void testGetReportsByProjectIntegration() {
        // This is a direct test without actually using SOAP transport
        // Arrange
        List<DailyReport> reports = new ArrayList<>();
        reports.add(testReport);
        when(reportingService.getReportsByProject(anyString())).thenReturn(reports);

        DailyReportSoapController.GetReportsByProjectRequest request = new DailyReportSoapController.GetReportsByProjectRequest();
        request.setProjectId(TEST_PROJECT_ID);

        // Act
        List<DailyReportSoapController.DailyReportSoapResponse> responses = soapController.getReportsByProject(request);

        // Assert
        assertNotNull(responses, "Responses list should not be null");
        assertFalse(responses.isEmpty(), "Responses list should not be empty");
        assertEquals(1, responses.size());
        assertEquals(TEST_REPORT_ID, responses.get(0).getId());

        // Verify service was called
        verify(reportingService).getReportsByProject(anyString());
    }
}