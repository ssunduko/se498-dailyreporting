package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test for DailyReportSoapController that simulates Postman SOAP testing
 * This test directly constructs SOAP XML messages and sends them via HTTP
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DailyReportSoapPostmanTest {

    @LocalServerPort
    private int serverPort;

    @MockBean
    private DailyReportingService reportingService;

    private final String TEST_PROJECT_ID = "project-123";
    private final String TEST_REPORT_ID = UUID.randomUUID().toString();
    private final String TEST_USERNAME = "testuser";
    private final LocalDate TEST_DATE = LocalDate.now();
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private HttpClient httpClient;
    private String soapEndpointUrl;
    private String soapNamespace = "http://se498.com/dailyreporting/soap";

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        soapEndpointUrl = "http://localhost:" + serverPort + "/services/dailyReport";

        // Setup mock service
        DailyReport testReport = new DailyReport();
        testReport.setId(TEST_REPORT_ID);
        testReport.setProjectId(TEST_PROJECT_ID);
        testReport.setReportDate(TEST_DATE);
        testReport.setStatus(ReportStatus.DRAFT);
        testReport.setNotes("Test Notes");
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setCreatedBy(TEST_USERNAME);

        when(reportingService.createReport(anyString(), any(LocalDate.class), anyString()))
                .thenReturn(testReport);
        when(reportingService.updateReport(anyString(), anyString(), anyString()))
                .thenReturn(testReport);
        when(reportingService.getReport(anyString())).thenReturn(Optional.of(testReport));
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