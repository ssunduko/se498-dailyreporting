package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import com.se498.dailyreporting.soap.gen.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.xml.transform.StringSource;

import javax.xml.transform.Source;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DailyReportSoapControllerTest {

    private static final String NAMESPACE_URI = "http://se498.com/dailyreporting/soap";

    @LocalServerPort
    private int port;

    @MockBean
    private DailyReportingService reportingService;

    @Autowired
    private WebServiceTemplate webServiceTemplate;

    private String serviceUrl;

    @BeforeEach
    public void setUp() {
        serviceUrl = "http://localhost:" + port + "/ws";

        // Set up WebServiceTemplate if not autowired
        if (webServiceTemplate == null) {
            webServiceTemplate = new WebServiceTemplate();
        }

        // Set up mock responses
        setupMockResponses();
    }

    private void setupMockResponses() {
        // Mock for getDailyReport
        DailyReport mockReport = createMockDailyReport("report123", "project123");
        when(reportingService.getReport(eq("report123"))).thenReturn(Optional.of(mockReport));

        // Mock for createDailyReport
        DailyReport createdReport = createMockDailyReport("newReport123", "project456");
        when(reportingService.createReport(anyString(), any(LocalDate.class), anyString()))
                .thenReturn(createdReport);
        when(reportingService.updateReport(eq("newReport123"), anyString(), anyString()))
                .thenReturn(createdReport);

        // Mock for submitDailyReport
        DailyReport submittedReport = createMockDailyReport("report123", "project123");
        submittedReport.setStatus(ReportStatus.SUBMITTED);
        when(reportingService.submitReport(eq("report123"), anyString()))
                .thenReturn(submittedReport);

        // Mock for addActivity
        ActivityEntry mockActivity = createMockActivity("activity123", "report123");
        when(reportingService.addActivityToReport(eq("report123"), any(ActivityEntry.class)))
                .thenReturn(mockActivity);

        // Mock for getReportsByProject
        List<DailyReport> mockReports = Arrays.asList(
                createMockDailyReport("report1", "project123"),
                createMockDailyReport("report2", "project123")
        );
        when(reportingService.getReportsByProject(eq("project123")))
                .thenReturn(mockReports);
        when(reportingService.getReportsByDateRange(
                eq("project123"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockReports);
    }

    @Test
    public void testGetDailyReport() {
        // Create request
        GetDailyReportRequest request = new GetDailyReportRequest();
        request.setReportId("report123");

        // Call SOAP service
        GetDailyReportResponse response = (GetDailyReportResponse) webServiceTemplate.marshalSendAndReceive(
                serviceUrl, request);

        // Assert response
        assertNotNull(response);
        assertNotNull(response.getDailyReport());
        assertEquals("report123", response.getDailyReport().getId());
        assertEquals("project123", response.getDailyReport().getProjectId());
        assertEquals(ReportStatus.DRAFT.name(), response.getDailyReport().getStatus().value());
    }

    @Test
    public void testCreateDailyReport() {
        // Create request
        CreateDailyReportRequest request = new CreateDailyReportRequest();
        request.setProjectId("project456");

        // Set date (will need calendar conversion)
        try {
            javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date());
            javax.xml.datatype.XMLGregorianCalendar xmlDate = datatypeFactory.newXMLGregorianCalendar(gc);
            request.setReportDate(xmlDate);
        } catch (Exception e) {
            fail("Failed to create XML date: " + e.getMessage());
        }

        request.setNotes("Test notes");
        request.setCreatedBy("testUser");

        // Call SOAP service
        CreateDailyReportResponse response = (CreateDailyReportResponse) webServiceTemplate.marshalSendAndReceive(
                serviceUrl, request);

        // Assert response
        assertNotNull(response);
        assertNotNull(response.getDailyReport());
        assertEquals("newReport123", response.getDailyReport().getId());
        assertEquals("project456", response.getDailyReport().getProjectId());
    }

    @Test
    public void testSubmitDailyReport() {
        // Create request
        SubmitDailyReportRequest request = new SubmitDailyReportRequest();
        request.setReportId("report123");
        request.setSubmittedBy("testUser");

        // Call SOAP service
        SubmitDailyReportResponse response = (SubmitDailyReportResponse) webServiceTemplate.marshalSendAndReceive(
                serviceUrl, request);

        // Assert response
        assertNotNull(response);
        assertNotNull(response.getDailyReport());
        assertEquals("report123", response.getDailyReport().getId());
        assertEquals(ReportStatus.SUBMITTED.name(), response.getDailyReport().getStatus().value());
    }

    @Test
    public void testAddActivity() {
        // Create request
        AddActivityRequest request = new AddActivityRequest();
        request.setReportId("report123");

        ActivityEntryType activity = new ActivityEntryType();
        activity.setDescription("Test activity");
        activity.setCategory("Testing");

        // Set dates
        try {
            javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();
            GregorianCalendar startGc = new GregorianCalendar();
            startGc.setTime(new Date());
            javax.xml.datatype.XMLGregorianCalendar startTime = datatypeFactory.newXMLGregorianCalendar(startGc);

            GregorianCalendar endGc = new GregorianCalendar();
            endGc.add(Calendar.HOUR, 2);
            javax.xml.datatype.XMLGregorianCalendar endTime = datatypeFactory.newXMLGregorianCalendar(endGc);

            activity.setStartTime(startTime);
            activity.setEndTime(endTime);
        } catch (Exception e) {
            fail("Failed to create XML dates: " + e.getMessage());
        }

        activity.setProgress(0.0);
        activity.setStatus(ActivityStatusType.PLANNED);
        activity.setCreatedBy("testUser");

        request.setActivity(activity);

        // Call SOAP service
        AddActivityResponse response = (AddActivityResponse) webServiceTemplate.marshalSendAndReceive(
                serviceUrl, request);

        // Assert response
        assertNotNull(response);
        assertNotNull(response.getActivityEntry());
        assertEquals("activity123", response.getActivityEntry().getId());
        assertEquals("report123", response.getActivityEntry().getReportId());
        assertEquals("Test Activity", response.getActivityEntry().getDescription());
    }

    @Test
    public void testGetReportsByProject() {
        // Create request
        GetReportsByProjectRequest request = new GetReportsByProjectRequest();
        request.setProjectId("project123");

        // Call SOAP service
        GetReportsByProjectResponse response = (GetReportsByProjectResponse) webServiceTemplate.marshalSendAndReceive(
                serviceUrl, request);

        // Assert response
        assertNotNull(response);
        assertNotNull(response.getDailyReports());
        assertEquals(2, response.getDailyReports().size());
        assertEquals("report1", response.getDailyReports().get(0).getId());
        assertEquals("report2", response.getDailyReports().get(1).getId());
    }

    @Test
    public void testGetReportsByProjectWithDateRange() {
        // Create request
        GetReportsByProjectRequest request = new GetReportsByProjectRequest();
        request.setProjectId("project123");

        // Set date range
        try {
            javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

            GregorianCalendar startGc = new GregorianCalendar();
            startGc.add(Calendar.DAY_OF_MONTH, -30);  // 30 days ago
            javax.xml.datatype.XMLGregorianCalendar startDate = datatypeFactory.newXMLGregorianCalendar(startGc);

            GregorianCalendar endGc = new GregorianCalendar();
            javax.xml.datatype.XMLGregorianCalendar endDate = datatypeFactory.newXMLGregorianCalendar(endGc);

            request.setStartDate(startDate);
            request.setEndDate(endDate);
        } catch (Exception e) {
            fail("Failed to create XML dates: " + e.getMessage());
        }

        // Call SOAP service
        GetReportsByProjectResponse response = (GetReportsByProjectResponse) webServiceTemplate.marshalSendAndReceive(
                serviceUrl, request);

        // Assert response
        assertNotNull(response);
        assertNotNull(response.getDailyReports());
        assertEquals(2, response.getDailyReports().size());
    }

    /**
     * Helper method to create a mock daily report
     */
    private DailyReport createMockDailyReport(String reportId, String projectId) {
        DailyReport report = new DailyReport(reportId, projectId, LocalDate.now());
        report.setStatus(ReportStatus.DRAFT);
        report.setNotes("Test notes");
        report.setCreatedBy("testUser");
        report.setCreatedAt(LocalDateTime.now());

        // Add an activity
        ActivityEntry activity = createMockActivity("activity123", reportId);
        report.addActivity(activity);

        return report;
    }

    /**
     * Helper method to create a mock activity
     */
    private ActivityEntry createMockActivity(String activityId, String reportId) {
        ActivityEntry activity = new ActivityEntry();
        activity.setId(activityId);
        activity.setReportId(reportId);
        activity.setDescription("Test Activity");
        activity.setCategory("Testing");
        activity.setStartTime(LocalDateTime.now());
        activity.setEndTime(LocalDateTime.now().plusHours(2));
        activity.setProgress(50.0);
        activity.setStatus(ActivityStatus.IN_PROGRESS);
        activity.setCreatedBy("testUser");
        activity.setCreatedAt(LocalDateTime.now());
        activity.setPersonnel(new HashSet<>(Arrays.asList("Worker1")));

        return activity;
    }
}
