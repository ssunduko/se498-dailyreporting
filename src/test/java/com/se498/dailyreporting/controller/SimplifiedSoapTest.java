package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.DailyReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.client.core.WebServiceTemplate;
import jakarta.xml.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

import com.se498.dailyreporting.service.DailyReportingService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A simplified SOAP test to diagnose JAXB issues
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SimplifiedSoapTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DailyReportingService reportingService;

    private WebServiceTemplate webServiceTemplate;
    private String projectId;
    private String reportId;
    private String username = "test-user";

    // A simple JAXB class for testing the marshaller
    @XmlRootElement(name = "getReport", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GetReportRequest {
        @XmlElement(required = true)
        private String reportId;

        public GetReportRequest() {}

        public GetReportRequest(String reportId) {
            this.reportId = reportId;
        }

        public String getReportId() { return reportId; }
        public void setReportId(String reportId) { this.reportId = reportId; }
    }

    // A simple response class for testing
    @XmlRootElement(name = "getReportResponse", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GetReportResponse {
        @XmlElement
        private boolean success;

        @XmlElement
        private String message;

        public GetReportResponse() {}

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @BeforeEach
    public void setUp() {
        try {
            // Create a very simple marshaller with just the test classes
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

            // Only use classes defined in this test file to isolate the issue
            marshaller.setClassesToBeBound(
                    GetReportRequest.class,
                    GetReportResponse.class
            );

            marshaller.afterPropertiesSet();

            webServiceTemplate = new WebServiceTemplate(marshaller);
            webServiceTemplate.setDefaultUri("http://localhost:" + port + "/soap/DailyReportService");

            // Create test project ID
            projectId = "test-project-" + UUID.randomUUID();

            // Create a test report via service
            DailyReport report = reportingService.createReport(
                    projectId,
                    LocalDate.now(),
                    username
            );
            reportId = report.getId();

            System.out.println("Test setup successful!");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error during test setup: " + e.getMessage());
        }
    }

    @Test
    public void testMarshallerWorks() {
        // Create a simple request object
        GetReportRequest request = new GetReportRequest(reportId);

        // Just test that the marshaller can serialize this object
        try {
            String xml = marshalToXml(request);
            System.out.println("Successfully marshalled to XML: " + xml);
            assertTrue(xml.contains(reportId), "XML should contain the report ID");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to marshal request: " + e.getMessage());
        }
    }

    // Helper method to convert an object to XML string for debugging
    private String marshalToXml(Object obj) throws Exception {
        jakarta.xml.bind.JAXBContext context = jakarta.xml.bind.JAXBContext.newInstance(obj.getClass());
        jakarta.xml.bind.Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);

        java.io.StringWriter sw = new java.io.StringWriter();
        marshaller.marshal(obj, sw);
        return sw.toString();
    }
}