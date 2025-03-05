package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test for DailyReportSoapController that simulates Postman SOAP testing
 * This test directly constructs SOAP XML messages and sends them via HTTP
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DailyReportSoapControllerMockTest {

    @LocalServerPort
    private int serverPort;

    @MockBean
    private DailyReportingService reportingService;

    private final String TEST_PROJECT_ID = "project-123";
    private final String TEST_REPORT_ID = UUID.randomUUID().toString();
    private final String TEST_ACTIVITY_ID = UUID.randomUUID().toString();
    private final String TEST_USERNAME = "testuser";
    private final LocalDate TEST_DATE = LocalDate.now();
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private HttpClient httpClient;
    private String soapEndpointUrl;
    private String soapNamespace = "http://se498.com/dailyreporting/soap";

    private DailyReport testReport;
    private ActivityEntry testActivity;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        soapEndpointUrl = "http://localhost:" + serverPort + "/services/dailyReport";

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
        testActivity = new ActivityEntry();
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
    void testGetReport() throws Exception {
        // Test getReport (no namespace on inner elements)
        String getReportEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                 xmlns:soap="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                     <soap:getReport>
                        <reportId>%s</reportId>
                     </soap:getReport>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(soapNamespace, TEST_REPORT_ID);

        System.out.println("===== TESTING getReport OPERATION =====");
        System.out.println("SOAP Request Envelope:");
        System.out.println(getReportEnvelope);

        // No SOAPAction needed based on previous success
        HttpResponse<String> response = sendSoapRequest(getReportEnvelope, "");

        System.out.println("\nSOAP Response Status: " + response.statusCode());
        System.out.println("SOAP Response Body (abbreviated):");
        System.out.println(response.body().substring(0, Math.min(response.body().length(), 200)) + "...");

        assertEquals(200, response.statusCode(), "getReport should succeed with status 200");

        // Parse and verify the response
        Document document = parseXml(response.body());
        XPath xpath = XPathFactory.newInstance().newXPath();
        String reportId = (String) xpath.evaluate("//reportResponse/*[local-name()='id']/text()", document, XPathConstants.STRING);
        String projectId = (String) xpath.evaluate("//reportResponse/*[local-name()='projectId']/text()", document, XPathConstants.STRING);

        assertEquals(TEST_REPORT_ID, reportId, "Report ID should match");
        assertEquals(TEST_PROJECT_ID, projectId, "Project ID should match");
        verify(reportingService).getReport(eq(TEST_REPORT_ID));

        System.out.println("✅ getReport test PASSED!");
    }

    @Test
    void testCreateReport() throws Exception {
        // Test createReport (NO namespace on container element, but WITH namespace on inner elements)
        String createReportEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                  xmlns:soap="%s">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <soap:createReport>
                         <createReportRequest>
                            <soap:projectId>%s</soap:projectId>
                            <soap:reportDate>%s</soap:reportDate>
                            <soap:notes>Test Notes</soap:notes>
                            <soap:username>%s</soap:username>
                         </createReportRequest>
                      </soap:createReport>
                   </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(soapNamespace, TEST_PROJECT_ID, TEST_DATE.format(DATE_FORMATTER), TEST_USERNAME);

        System.out.println("\n===== TESTING createReport OPERATION =====");
        System.out.println("SOAP Request Envelope:");
        System.out.println(createReportEnvelope);

        // No SOAPAction needed based on previous test
        HttpResponse<String> response = sendSoapRequest(createReportEnvelope, "");

        System.out.println("\nSOAP Response Status: " + response.statusCode());
        System.out.println("SOAP Response Body (abbreviated):");
        if (response.statusCode() == 200) {
            System.out.println(response.body().substring(0, Math.min(response.body().length(), 200)) + "...");
        } else {
            System.out.println(response.body());
        }

        assertEquals(200, response.statusCode(), "createReport should succeed with status 200");

        // Parse and verify the response
        Document document = parseXml(response.body());
        XPath xpath = XPathFactory.newInstance().newXPath();
        String reportId = (String) xpath.evaluate("//reportResponse/*[local-name()='id']/text()", document, XPathConstants.STRING);
        String projectId = (String) xpath.evaluate("//reportResponse/*[local-name()='projectId']/text()", document, XPathConstants.STRING);

        // Verify service was called and response matches expectations
        verify(reportingService).createReport(eq(TEST_PROJECT_ID), any(LocalDate.class), eq(TEST_USERNAME));
        assertEquals(TEST_REPORT_ID, reportId, "Report ID should match");
        assertEquals(TEST_PROJECT_ID, projectId, "Project ID should match");

        System.out.println("✅ createReport test PASSED!");
    }

    @Test
    void testUpdateReport() throws Exception {
        // Test updateReport
        String updateReportEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                 xmlns:soap="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                     <soap:updateReport>
                        <updateReportRequest>
                           <soap:reportId>%s</soap:reportId>
                           <soap:notes>Updated Notes</soap:notes>
                           <soap:username>%s</soap:username>
                        </updateReportRequest>
                     </soap:updateReport>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(soapNamespace, TEST_REPORT_ID, TEST_USERNAME);

        System.out.println("\n===== TESTING updateReport OPERATION =====");
        System.out.println("SOAP Request Envelope:");
        System.out.println(updateReportEnvelope);

        HttpResponse<String> response = sendSoapRequest(updateReportEnvelope, "");

        System.out.println("\nSOAP Response Status: " + response.statusCode());
        System.out.println("SOAP Response Body (abbreviated):");
        System.out.println(response.body().substring(0, Math.min(response.body().length(), 200)) + "...");

        assertEquals(200, response.statusCode(), "updateReport should succeed with status 200");

        // Parse and verify the response
        Document document = parseXml(response.body());
        XPath xpath = XPathFactory.newInstance().newXPath();
        String reportId = (String) xpath.evaluate("//reportResponse/*[local-name()='id']/text()", document, XPathConstants.STRING);

        assertEquals(TEST_REPORT_ID, reportId, "Report ID should match");
        verify(reportingService).updateReport(eq(TEST_REPORT_ID), eq("Updated Notes"), eq(TEST_USERNAME));

        System.out.println("✅ updateReport test PASSED!");
    }

    @Test
    void testSubmitReport() throws Exception {
        // Test submitReport
        String submitReportEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                 xmlns:soap="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                     <soap:submitReport>
                        <submitReportRequest>
                           <soap:reportId>%s</soap:reportId>
                           <soap:username>%s</soap:username>
                        </submitReportRequest>
                     </soap:submitReport>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(soapNamespace, TEST_REPORT_ID, TEST_USERNAME);

        System.out.println("\n===== TESTING submitReport OPERATION =====");
        System.out.println("SOAP Request Envelope:");
        System.out.println(submitReportEnvelope);

        HttpResponse<String> response = sendSoapRequest(submitReportEnvelope, "");

        System.out.println("\nSOAP Response Status: " + response.statusCode());
        System.out.println("SOAP Response Body (abbreviated):");
        System.out.println(response.body().substring(0, Math.min(response.body().length(), 200)) + "...");

        assertEquals(200, response.statusCode(), "submitReport should succeed with status 200");

        // Parse and verify the response
        Document document = parseXml(response.body());
        XPath xpath = XPathFactory.newInstance().newXPath();
        String reportId = (String) xpath.evaluate("//reportResponse/*[local-name()='id']/text()", document, XPathConstants.STRING);
        String status = (String) xpath.evaluate("//reportResponse/*[local-name()='status']/text()", document, XPathConstants.STRING);

        assertEquals(TEST_REPORT_ID, reportId, "Report ID should match");
        assertEquals("SUBMITTED", status, "Report status should be SUBMITTED");
        verify(reportingService).submitReport(eq(TEST_REPORT_ID), eq(TEST_USERNAME));

        System.out.println("✅ submitReport test PASSED!");
    }

    @Test
    void testApproveReport() throws Exception {
        // Test approveReport
        String approveReportEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                 xmlns:soap="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                     <soap:approveReport>
                        <approveReportRequest>
                           <soap:reportId>%s</soap:reportId>
                           <soap:username>%s</soap:username>
                        </approveReportRequest>
                     </soap:approveReport>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(soapNamespace, TEST_REPORT_ID, TEST_USERNAME);

        System.out.println("\n===== TESTING approveReport OPERATION =====");
        System.out.println("SOAP Request Envelope:");
        System.out.println(approveReportEnvelope);

        HttpResponse<String> response = sendSoapRequest(approveReportEnvelope, "");

        System.out.println("\nSOAP Response Status: " + response.statusCode());
        System.out.println("SOAP Response Body (abbreviated):");
        System.out.println(response.body().substring(0, Math.min(response.body().length(), 200)) + "...");

        assertEquals(200, response.statusCode(), "approveReport should succeed with status 200");

        // Parse and verify the response
        Document document = parseXml(response.body());
        XPath xpath = XPathFactory.newInstance().newXPath();
        String reportId = (String) xpath.evaluate("//reportResponse/*[local-name()='id']/text()", document, XPathConstants.STRING);
        String status = (String) xpath.evaluate("//reportResponse/*[local-name()='status']/text()", document, XPathConstants.STRING);

        assertEquals(TEST_REPORT_ID, reportId, "Report ID should match");
        assertEquals("APPROVED", status, "Report status should be APPROVED");
        verify(reportingService).approveReport(eq(TEST_REPORT_ID), eq(TEST_USERNAME));

        System.out.println("✅ approveReport test PASSED!");
    }

    @Test
    void testAddActivity() throws Exception {
        // Test addActivity
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        LocalDateTime endTime = LocalDateTime.now().minusHours(1);

        String addActivityEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                 xmlns:soap="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                     <soap:addActivity>
                        <addActivityRequest>
                           <soap:reportId>%s</soap:reportId>
                           <soap:description>Test Activity</soap:description>
                           <soap:category>Test Category</soap:category>
                           <soap:startTime>%s</soap:startTime>
                           <soap:endTime>%s</soap:endTime>
                           <soap:progress>50.0</soap:progress>
                           <soap:status>IN_PROGRESS</soap:status>
                           <soap:notes>Activity Notes</soap:notes>
                           <soap:personnel>
                              <soap:person>Person 1</soap:person>
                              <soap:person>Person 2</soap:person>
                           </soap:personnel>
                           <soap:username>%s</soap:username>
                        </addActivityRequest>
                     </soap:addActivity>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(soapNamespace, TEST_REPORT_ID,
                startTime.format(DATETIME_FORMATTER),
                endTime.format(DATETIME_FORMATTER),
                TEST_USERNAME);

        System.out.println("\n===== TESTING addActivity OPERATION =====");
        System.out.println("SOAP Request Envelope:");
        System.out.println(addActivityEnvelope);

        HttpResponse<String> response = sendSoapRequest(addActivityEnvelope, "");

        System.out.println("\nSOAP Response Status: " + response.statusCode());
        System.out.println("SOAP Response Body (abbreviated):");
        System.out.println(response.body().substring(0, Math.min(response.body().length(), 200)) + "...");

        assertEquals(200, response.statusCode(), "addActivity should succeed with status 200");

        // Parse and verify the response
        Document document = parseXml(response.body());
        XPath xpath = XPathFactory.newInstance().newXPath();
        String activityId = (String) xpath.evaluate("//activityResponse/*[local-name()='id']/text()", document, XPathConstants.STRING);
        String description = (String) xpath.evaluate("//activityResponse/*[local-name()='description']/text()", document, XPathConstants.STRING);

        assertEquals(TEST_ACTIVITY_ID, activityId, "Activity ID should match");
        assertEquals("Test Activity", description, "Activity description should match");
        verify(reportingService).addActivityToReport(eq(TEST_REPORT_ID), any(ActivityEntry.class));

        System.out.println("✅ addActivity test PASSED!");
    }

    @Test
    void testGetActivitiesByReport() throws Exception {
        // Test getActivitiesByReport
        String getActivitiesEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                 xmlns:soap="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                     <soap:getActivitiesByReport>
                        <reportId>%s</reportId>
                     </soap:getActivitiesByReport>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(soapNamespace, TEST_REPORT_ID);

        System.out.println("\n===== TESTING getActivitiesByReport OPERATION =====");
        System.out.println("SOAP Request Envelope:");
        System.out.println(getActivitiesEnvelope);

        HttpResponse<String> response = sendSoapRequest(getActivitiesEnvelope, "");

        System.out.println("\nSOAP Response Status: " + response.statusCode());
        System.out.println("SOAP Response Body (abbreviated):");
        System.out.println(response.body().substring(0, Math.min(response.body().length(), 200)) + "...");

        assertEquals(200, response.statusCode(), "getActivitiesByReport should succeed with status 200");

        // Parse and verify the response
        Document document = parseXml(response.body());
        XPath xpath = XPathFactory.newInstance().newXPath();

        // Check if we have at least one activity
        // Updated XPath to match the actual response structure
        String activityId = (String) xpath.evaluate("//activityResponses/ns2:id/text()", document, XPathConstants.STRING);

        // If that doesn't work, try alternative XPath expressions
        if (activityId.isEmpty()) {
            // Try without namespace
            activityId = (String) xpath.evaluate("//activityResponses/*[local-name()='id']/text()", document, XPathConstants.STRING);
        }

        assertFalse(activityId.isEmpty(), "Should have at least one activity");
        assertEquals(TEST_ACTIVITY_ID, activityId, "Activity ID should match");
        verify(reportingService).getActivitiesByReport(eq(TEST_REPORT_ID));

        System.out.println("✅ getActivitiesByReport test PASSED!");
    }

    @Test
    void testUpdateActivityProgress() throws Exception {
        // Test updateActivityProgress
        String updateProgressEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                 xmlns:soap="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                     <soap:updateActivityProgress>
                        <updateActivityProgressRequest>
                           <soap:activityId>%s</soap:activityId>
                           <soap:progress>75.0</soap:progress>
                           <soap:username>%s</soap:username>
                        </updateActivityProgressRequest>
                     </soap:updateActivityProgress>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(soapNamespace, TEST_ACTIVITY_ID, TEST_USERNAME);

        System.out.println("\n===== TESTING updateActivityProgress OPERATION =====");
        System.out.println("SOAP Request Envelope:");
        System.out.println(updateProgressEnvelope);

        HttpResponse<String> response = sendSoapRequest(updateProgressEnvelope, "");

        System.out.println("\nSOAP Response Status: " + response.statusCode());
        System.out.println("SOAP Response Body (abbreviated):");
        System.out.println(response.body().substring(0, Math.min(response.body().length(), 200)) + "...");

        assertEquals(200, response.statusCode(), "updateActivityProgress should succeed with status 200");

        // Parse and verify the response
        Document document = parseXml(response.body());
        XPath xpath = XPathFactory.newInstance().newXPath();
        String activityId = (String) xpath.evaluate("//activityResponse/*[local-name()='id']/text()", document, XPathConstants.STRING);

        assertEquals(TEST_ACTIVITY_ID, activityId, "Activity ID should match");
        verify(reportingService).updateActivityProgress(eq(TEST_ACTIVITY_ID), eq(75.0), eq(TEST_USERNAME));

        System.out.println("✅ updateActivityProgress test PASSED!");
    }

    @Test
    void testIsReportComplete() throws Exception {
        // Test isReportComplete
        String isCompleteEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                 xmlns:soap="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                     <soap:isReportComplete>
                        <reportId>%s</reportId>
                     </soap:isReportComplete>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(soapNamespace, TEST_REPORT_ID);

        System.out.println("\n===== TESTING isReportComplete OPERATION =====");
        System.out.println("SOAP Request Envelope:");
        System.out.println(isCompleteEnvelope);

        HttpResponse<String> response = sendSoapRequest(isCompleteEnvelope, "");

        System.out.println("\nSOAP Response Status: " + response.statusCode());
        System.out.println("SOAP Response Body:");
        System.out.println(response.body());

        assertEquals(200, response.statusCode(), "isReportComplete should succeed with status 200");

        // Parse and verify the response
        Document document = parseXml(response.body());
        XPath xpath = XPathFactory.newInstance().newXPath();

        // Try multiple XPath patterns to find the isComplete element
        String isComplete = (String) xpath.evaluate("//*[local-name()='isReportCompleteResponse']/*[local-name()='isComplete']/text()", document, XPathConstants.STRING);

        // If not found, try direct path
        if (isComplete.isEmpty()) {
            isComplete = (String) xpath.evaluate("//isComplete/text()", document, XPathConstants.STRING);
        }

        assertEquals("false", isComplete, "Report should not be complete");
        verify(reportingService).isReportComplete(eq(TEST_REPORT_ID));

        System.out.println("✅ isReportComplete test PASSED!");
    }

    @Test
    void testGetReportProgress() throws Exception {
        // Test getReportProgress
        String getProgressEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                 xmlns:soap="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                     <soap:getReportProgress>
                        <reportId>%s</reportId>
                     </soap:getReportProgress>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(soapNamespace, TEST_REPORT_ID);

        System.out.println("\n===== TESTING getReportProgress OPERATION =====");
        System.out.println("SOAP Request Envelope:");
        System.out.println(getProgressEnvelope);

        HttpResponse<String> response = sendSoapRequest(getProgressEnvelope, "");

        System.out.println("\nSOAP Response Status: " + response.statusCode());
        System.out.println("SOAP Response Body:");
        System.out.println(response.body());

        assertEquals(200, response.statusCode(), "getReportProgress should succeed with status 200");

        // Parse and verify the response
        Document document = parseXml(response.body());
        XPath xpath = XPathFactory.newInstance().newXPath();

        // Try multiple XPath patterns to find the progress element
        String progress = (String) xpath.evaluate("//*[local-name()='getReportProgressResponse']/*[local-name()='progress']/text()", document, XPathConstants.STRING);

        // If not found, try direct path
        if (progress.isEmpty()) {
            progress = (String) xpath.evaluate("//progress/text()", document, XPathConstants.STRING);
        }

        assertEquals("50.0", progress, "Progress should be 50.0");
        verify(reportingService).calculateReportProgress(eq(TEST_REPORT_ID));

        System.out.println("✅ getReportProgress test PASSED!");
    }

    /**
     * Helper method to send a SOAP request with optional SOAPAction
     */
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
            System.out.println("Using SOAPAction: " + soapAction);
        } else {
            System.out.println("No SOAPAction header");
        }

        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(soapEnvelope))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Helper method to parse XML
     */
    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);  // Important for XPath
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }
}