package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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
@Disabled
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

    /*@TestConfiguration
    static class SoapPostmanTestConfig {
        @Bean(name = "postmanTestEndpoint")
        public Endpoint soapPostmanEndpoint(Bus bus, DailyReportSoapController soapController) {
            EndpointImpl endpoint = new EndpointImpl(bus, soapController);
            // Use a specific endpoint for this test
            endpoint.publish("/postman-test/dailyReport");
            return endpoint;
        }
    }*/

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
    void testCreateReportPostmanStyle() throws Exception {
        // Arrange - Build a SOAP envelope for createReport (similar to what you would do in Postman)
        String soapEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                  xmlns:soap="http://se498.com/dailyreporting/soap">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <soap:createReport>
                         <createReportRequest>
                            <projectId>%s</projectId>
                            <reportDate>%s</reportDate>
                            <notes>Test Notes</notes>
                            <username>%s</username>
                         </createReportRequest>
                      </soap:createReport>
                   </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(TEST_PROJECT_ID, TEST_DATE.format(DATE_FORMATTER), TEST_USERNAME);

        // Act - Send the SOAP request
        HttpResponse<String> response = sendSoapRequest(soapEnvelope, "createReport");

        // Assert - Parse and verify the response
        assertEquals(200, response.statusCode(), "Should return HTTP 200 OK");

        // Parse the SOAP response
        Document document = parseXml(response.body());

        // Use XPath to extract values
        XPath xpath = XPathFactory.newInstance().newXPath();
        String reportId = (String) xpath.evaluate(
                "//reportResponse/id/text()", document, XPathConstants.STRING);
        String projectId = (String) xpath.evaluate(
                "//reportResponse/projectId/text()", document, XPathConstants.STRING);
        String status = (String) xpath.evaluate(
                "//reportResponse/status/text()", document, XPathConstants.STRING);

        // Verify the response
        assertEquals(TEST_REPORT_ID, reportId, "Report ID should match");
        assertEquals(TEST_PROJECT_ID, projectId, "Project ID should match");
        assertEquals("DRAFT", status, "Status should be DRAFT");

        // Verify the service was called
        verify(reportingService).createReport(eq(TEST_PROJECT_ID), any(LocalDate.class), eq(TEST_USERNAME));
    }

    @Test
    void testGetReportPostmanStyle() throws Exception {
        // Arrange - Build a SOAP envelope for getReport
        String soapEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                  xmlns:soap="http://se498.com/dailyreporting/soap">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <soap:getReport>
                         <reportId>%s</reportId>
                      </soap:getReport>
                   </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(TEST_REPORT_ID);

        // Act - Send the SOAP request
        HttpResponse<String> response = sendSoapRequest(soapEnvelope, "getReport");

        // Assert - Parse and verify the response
        assertEquals(200, response.statusCode(), "Should return HTTP 200 OK");

        // Parse the SOAP response
        Document document = parseXml(response.body());

        // Use XPath to extract values
        XPath xpath = XPathFactory.newInstance().newXPath();
        String reportId = (String) xpath.evaluate(
                "//reportResponse/id/text()", document, XPathConstants.STRING);
        String projectId = (String) xpath.evaluate(
                "//reportResponse/projectId/text()", document, XPathConstants.STRING);

        // Verify the response
        assertEquals(TEST_REPORT_ID, reportId, "Report ID should match");
        assertEquals(TEST_PROJECT_ID, projectId, "Project ID should match");

        // Verify the service was called
        verify(reportingService).getReport(eq(TEST_REPORT_ID));
    }

    @Test
    void testUpdateReportPostmanStyle() throws Exception {
        // Arrange - Build a SOAP envelope for updateReport
        String soapEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                  xmlns:soap="http://se498.com/dailyreporting/soap">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <soap:updateReport>
                         <updateReportRequest>
                            <reportId>%s</reportId>
                            <notes>Updated Notes</notes>
                            <username>%s</username>
                         </updateReportRequest>
                      </soap:updateReport>
                   </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(TEST_REPORT_ID, TEST_USERNAME);

        // Act - Send the SOAP request
        HttpResponse<String> response = sendSoapRequest(soapEnvelope, "updateReport");

        // Assert - Parse and verify the response
        assertEquals(200, response.statusCode(), "Should return HTTP 200 OK");

        // Parse the SOAP response
        Document document = parseXml(response.body());

        // Use XPath to extract values
        XPath xpath = XPathFactory.newInstance().newXPath();
        String reportId = (String) xpath.evaluate(
                "//reportResponse/id/text()", document, XPathConstants.STRING);

        // Verify the response
        assertEquals(TEST_REPORT_ID, reportId, "Report ID should match");

        // Verify the service was called
        verify(reportingService).updateReport(eq(TEST_REPORT_ID), eq("Updated Notes"), eq(TEST_USERNAME));
    }

    @Test
    void testDeleteReportPostmanStyle() throws Exception {
        // Arrange - Build a SOAP envelope for deleteReport
        String soapEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                                  xmlns:soap="http://se498.com/dailyreporting/soap">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <soap:deleteReport>
                         <reportId>%s</reportId>
                      </soap:deleteReport>
                   </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(TEST_REPORT_ID);

        // Act - Send the SOAP request
        HttpResponse<String> response = sendSoapRequest(soapEnvelope, "deleteReport");

        // Assert - Parse and verify the response
        assertEquals(200, response.statusCode(), "Should return HTTP 200 OK");

        // Parse the SOAP response
        Document document = parseXml(response.body());

        // Use XPath to extract values
        XPath xpath = XPathFactory.newInstance().newXPath();
        String successText = (String) xpath.evaluate(
                "//success/text()", document, XPathConstants.STRING);

        // Verify the response
        assertEquals("true", successText, "Success should be true");

        // Verify the service was called
        verify(reportingService).deleteReport(eq(TEST_REPORT_ID));
    }

    /**
     * Helper method to send a SOAP request
     *
     * @param soapEnvelope The SOAP envelope XML
     * @param soapAction The SOAP action to call
     * @return The HTTP response
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     */
    private HttpResponse<String> sendSoapRequest(String soapEnvelope, String soapAction)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(soapEndpointUrl))
                .header("Content-Type", "text/xml;charset=UTF-8")
                .header("SOAPAction", "http://se498.com/dailyreporting/soap/" + soapAction)
                .POST(HttpRequest.BodyPublishers.ofString(soapEnvelope))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Helper method to parse XML
     *
     * @param xml The XML string to parse
     * @return The parsed XML document
     * @throws Exception If parsing fails
     */
    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);  // Important for XPath
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    /**
     * Helper method to pretty print XML for debugging
     *
     * @param xml The XML string to format
     * @return A formatted XML string
     */
    private String prettyPrintXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            return "Error formatting XML: " + e.getMessage();
        }
    }
}