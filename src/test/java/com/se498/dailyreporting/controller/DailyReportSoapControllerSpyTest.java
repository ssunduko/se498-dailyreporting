package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.TestDailyReportingApplication;
import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test for DailyReportSoapController that simulates Postman SOAP testing
 * This test directly constructs SOAP XML messages and sends them via HTTP
 */
@Slf4j
@SpringBootTest(classes = {TestDailyReportingApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DailyReportSoapControllerSpyTest {

    @LocalServerPort
    private int serverPort;

    @MockBean
    private DailyReportingService reportingService;

    @SpyBean
    private DailyReportSoapController soapController;

    private final String TEST_PROJECT_ID = "project-123";
    private final String TEST_REPORT_ID = UUID.randomUUID().toString();
    private final String TEST_ACTIVITY_ID = UUID.randomUUID().toString();
    private final String TEST_USERNAME = "testuser";
    private final LocalDate TEST_DATE = LocalDate.now();

    private HttpClient httpClient;
    private String soapEndpointUrl;

    private DailyReport testReport;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        // Note the URL pattern: based on CXF default of /services/{path}
        soapEndpointUrl = "http://localhost:" + serverPort + "/services/dailyReport";
        log.info("Using SOAP endpoint URL: {}", soapEndpointUrl);

        // Setup test report
        testReport = new DailyReport();
        testReport.setId(TEST_REPORT_ID);
        testReport.setProjectId(TEST_PROJECT_ID);
        testReport.setReportDate(TEST_DATE);
        testReport.setStatus(ReportStatus.DRAFT);
        testReport.setNotes("Test Notes");
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setCreatedBy(TEST_USERNAME);

        // Setup test activity
        ActivityEntry testActivity = new ActivityEntry();
        testActivity.setId(TEST_ACTIVITY_ID);
        testActivity.setReportId(TEST_REPORT_ID);
        testActivity.setDescription("Test Activity");
        testActivity.setCategory("Test Category");
        testActivity.setStartTime(LocalDateTime.now().minusHours(2));
        testActivity.setEndTime(LocalDateTime.now().minusHours(1));
        testActivity.setProgress(50.0);
        testActivity.setStatus(ActivityStatus.IN_PROGRESS);
        testActivity.setNotes("Test Notes");
        testActivity.setCreatedBy(TEST_USERNAME);
        testActivity.setCreatedAt(LocalDateTime.now());

        // Add activity to report
        List<ActivityEntry> activities = new ArrayList<>();
        activities.add(testActivity);
        testReport.setActivities(activities);

        // Setup mock service
        when(reportingService.createReport(anyString(), any(LocalDate.class), anyString()))
                .thenReturn(testReport);
        when(reportingService.updateReport(anyString(), anyString(), anyString()))
                .thenReturn(testReport);
        when(reportingService.getReport(anyString())).thenReturn(Optional.of(testReport));
        when(reportingService.getActivity(anyString())).thenReturn(Optional.of(testActivity));
        when(reportingService.submitReport(anyString(), anyString())).thenAnswer(invocation -> {
            DailyReport report = testReport;
            report.setStatus(ReportStatus.SUBMITTED);
            return report;
        });
        when(reportingService.approveReport(anyString(), anyString())).thenAnswer(invocation -> {
            DailyReport report = testReport;
            report.setStatus(ReportStatus.APPROVED);
            return report;
        });
        when(reportingService.rejectReport(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            DailyReport report = testReport;
            report.setStatus(ReportStatus.REJECTED);
            return report;
        });
        when(reportingService.addActivityToReport(anyString(), any(ActivityEntry.class)))
                .thenReturn(testActivity);
        when(reportingService.getActivitiesByReport(anyString()))
                .thenReturn(Collections.singletonList(testActivity));
        when(reportingService.updateActivityProgress(anyString(), anyDouble(), anyString()))
                .thenReturn(testActivity);
        when(reportingService.calculateReportProgress(anyString())).thenReturn(50.0);
        when(reportingService.isReportComplete(anyString())).thenReturn(false);
        when(reportingService.getTotalActivityDurationMinutes(anyString())).thenReturn(60L);
    }

    @Test
    void testDirectGetReport() {
        // Test direct invocation of the controller method
        DailyReportSoapController.DailyReportSoapResponse response = soapController.getReport(TEST_REPORT_ID);

        // Verify response
        assertNotNull(response, "Response should not be null");
        assertEquals(TEST_REPORT_ID, response.getId(), "Report ID should match");
        assertEquals(TEST_PROJECT_ID, response.getProjectId(), "Project ID should match");

        // Verify service was called
        verify(reportingService).getReport(eq(TEST_REPORT_ID));
    }

    @Test
    void testGetReportSoapRequest() throws Exception {

        String soapNamespace = "http://se498.com/dailyreporting/soap";
        String getReportEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\s
                                 xmlns:soap="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                     <soap:getReport>
                        <reportId>%s</reportId>
                     </soap:getReport>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(soapNamespace, TEST_REPORT_ID);

        log.info("Sending SOAP request to: {}", soapEndpointUrl);
        log.info("SOAP request: {}", getReportEnvelope);

        HttpResponse<String> response = sendSoapRequest(getReportEnvelope, "");
        log.info("SOAP response status: {}", response.statusCode());
        log.info("SOAP response (truncated): {}",
                response.body().substring(0, Math.min(response.body().length(), 100)) + "...");

        assertEquals(200, response.statusCode(), "getReport should succeed with status 200");

        // Parse and verify the response
        Document document = parseXml(response.body());
        XPath xpath = XPathFactory.newInstance().newXPath();
        String reportId = (String) xpath.evaluate("//reportResponse/*[local-name()='id']/text()", document, XPathConstants.STRING);

        assertEquals(TEST_REPORT_ID, reportId, "Report ID should match");

        // Verify the controller method was called (via the spy)
        verify(soapController).getReport(eq(TEST_REPORT_ID));
    }

    @Test
    void testDirectSubmitReport() {
        // Create request object
        DailyReportSoapController.SubmitReportRequest request = new DailyReportSoapController.SubmitReportRequest();
        request.setReportId(TEST_REPORT_ID);
        request.setUsername(TEST_USERNAME);

        // Test direct invocation
        DailyReportSoapController.DailyReportSoapResponse response = soapController.submitReport(request);

        // Verify response
        assertNotNull(response, "Response should not be null");
        assertEquals(TEST_REPORT_ID, response.getId(), "Report ID should match");
        assertEquals("SUBMITTED", response.getStatus(), "Status should be SUBMITTED");

        // Verify service was called
        verify(reportingService).submitReport(eq(TEST_REPORT_ID), eq(TEST_USERNAME));
    }

    @Test
    void testDirectGetActivitiesByReport() {
        // Test direct invocation
        List<DailyReportSoapController.ActivityEntrySoapResponse> responses =
                soapController.getActivitiesByReport(TEST_REPORT_ID);

        // Verify response
        assertNotNull(responses, "Response should not be null");
        assertFalse(responses.isEmpty(), "Response should contain activities");
        assertEquals(TEST_ACTIVITY_ID, responses.getFirst().getId(), "Activity ID should match");

        // Verify service was called
        verify(reportingService).getActivitiesByReport(eq(TEST_REPORT_ID));
    }

    @Test
    void testDirectIsReportComplete() {
        // Test direct invocation
        boolean complete = soapController.isReportComplete(TEST_REPORT_ID);

        // Verify response
        assertFalse(complete, "Report should not be complete");

        // Verify service was called
        verify(reportingService).isReportComplete(eq(TEST_REPORT_ID));
    }

    private HttpResponse<String> sendSoapRequest(String soapEnvelope, String soapAction)
            throws Exception {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(soapEndpointUrl))
                .header("Content-Type", "text/xml;charset=UTF-8");

        // Add SOAPAction header if specified
        if (soapAction != null && !soapAction.isEmpty()) {
            // Add quotes if needed
            if (!soapAction.startsWith("\"")) {
                soapAction = "\"" + soapAction + "\"";
            }
            requestBuilder.header("SOAPAction", soapAction);
        }

        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(soapEnvelope))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);  // Important for XPath
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }
}