package com.se498.dailyreporting.controller;


import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.dto.soap.*;
import com.se498.dailyreporting.service.DailyReportingService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.ws.client.core.WebServiceTemplate;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Daily Report SOAP Controller Integration Tests with MockMvc")
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class DailyReportSoapControllerMvcMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DailyReportingService reportingService;

    private DailyReport testReport;
    private ActivityEntry testActivity;
    private String testReportId;
    private String testActivityId;
    private String testProjectId;
    private LocalDate testReportDate;
    private String testUsername;

    // JAXB contexts for marshalling/unmarshalling
    private JAXBContext jaxbContext;

    private WebServiceTemplate template;

    @BeforeEach
    void setUp() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.se498.dailyreporting.dto.soap");
        template = new WebServiceTemplate(marshaller);
        template.setDefaultUri("http://localhost:8080/soap/DailyReportService");
    }

    @Test
    @DisplayName("Should return report when it exists via SOAP")
    @WithMockUser(username = "sergey", password = "chapman", roles = "ADMIN")
    void getReport_shouldReturnReport_whenReportExists() throws Exception {
        // Arrange
        when(reportingService.getReport(testReportId)).thenReturn(Optional.of(testReport));

        // Create SOAP envelope for getReport request
        String soapRequest = createSoapEnvelope(
                "<getReport xmlns=\"http://reporting.construction.com/soap\">" +
                        "<reportId>" + testReportId + "</reportId>" +
                        "</getReport>"
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/soap/DailyReportService")
                        .contentType(MediaType.TEXT_XML)
                        .content(soapRequest)
                        .accept(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:getReportResponse/reportResponse/success")
                        .string("true"))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:getReportResponse/reportResponse/id")
                        .string(testReportId))
                .andReturn();

        // Verify service was called
        verify(reportingService).getReport(testReportId);

        // Additional validation by parsing the SOAP response
        String responseXml = result.getResponse().getContentAsString();
        assertTrue(responseXml.contains("<success>true</success>"));
        assertTrue(responseXml.contains("<id>" + testReportId + "</id>"));
    }

    @Test
    @DisplayName("Should create report when valid data provided via SOAP")
    @WithMockUser(username = "sergey", password = "chapman", roles = "ADMIN")
    void createReport_shouldCreateReport_whenValidDataProvided() throws Exception {
        // Arrange
        when(reportingService.createReport(eq(testProjectId), eq(testReportDate), eq(testUsername)))
                .thenReturn(testReport);
        when(reportingService.updateReport(eq(testReportId), anyString(), eq(testUsername)))
                .thenReturn(testReport);

        // Create SOAP envelope for createReport request
        String soapRequest = createSoapEnvelope(
                "<createReport xmlns=\"http://reporting.construction.com/soap\">" +
                        "<projectId>" + testProjectId + "</projectId>" +
                        "<reportDate>" + testReportDate + "</reportDate>" +
                        "<notes>Test Notes</notes>" +
                        "<username>" + testUsername + "</username>" +
                        "</createReport>"
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/soap/DailyReportService")
                        .contentType(MediaType.TEXT_XML)
                        .content(soapRequest)
                        .accept(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:createReportResponse/reportResponse/success")
                        .string("true"))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:createReportResponse/reportResponse/id")
                        .string(testReportId))
                .andReturn();

        // Verify services were called
        verify(reportingService).createReport(eq(testProjectId), eq(testReportDate), eq(testUsername));
        verify(reportingService).updateReport(eq(testReportId), anyString(), eq(testUsername));
    }

    @Test
    @DisplayName("Should submit report successfully via SOAP")
    @WithMockUser(username = "sergey", password = "chapman", roles = "ADMIN")
    void submitReport_shouldSubmitReport_whenReportExists() throws Exception {
        // Arrange
        when(reportingService.submitReport(testReportId, testUsername)).thenReturn(testReport);

        // Create SOAP envelope for submitReport request
        String soapRequest = createSoapEnvelope(
                "<submitReport xmlns=\"http://reporting.construction.com/soap\">" +
                        "<reportId>" + testReportId + "</reportId>" +
                        "<username>" + testUsername + "</username>" +
                        "</submitReport>"
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/soap/DailyReportService")
                        .contentType(MediaType.TEXT_XML)
                        .content(soapRequest)
                        .accept(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:submitReportResponse/reportResponse/success")
                        .string("true"))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:submitReportResponse/reportResponse/id")
                        .string(testReportId))
                .andReturn();

        // Verify service was called
        verify(reportingService).submitReport(testReportId, testUsername);
    }

    @Test
    @DisplayName("Should delete report successfully via SOAP")
    @WithMockUser(username = "sergey", password = "chapman", roles = "ADMIN")
    void deleteReport_shouldReturnSuccess_whenReportDeleted() throws Exception {
        // Arrange
        doNothing().when(reportingService).deleteReport(testReportId);

        // Create SOAP envelope for deleteReport request
        String soapRequest = createSoapEnvelope(
                "<deleteReport xmlns=\"http://reporting.construction.com/soap\">" +
                        "<reportId>" + testReportId + "</reportId>" +
                        "</deleteReport>"
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/soap/DailyReportService")
                        .contentType(MediaType.TEXT_XML)
                        .content(soapRequest)
                        .accept(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:deleteReportResponse/serviceResponse/success")
                        .string("true"))
                .andReturn();

        // Verify service was called
        verify(reportingService).deleteReport(testReportId);
    }

    @Test
    @DisplayName("Should handle error when report not found via SOAP")
    @WithMockUser(username = "sergey", password = "chapman", roles = "ADMIN")
    void getReport_shouldReturnError_whenReportNotFound() throws Exception {
        // Arrange
        when(reportingService.getReport(testReportId)).thenReturn(Optional.empty());

        // Create SOAP envelope for getReport request
        String soapRequest = createSoapEnvelope(
                "<getReport xmlns=\"http://reporting.construction.com/soap\">" +
                        "<reportId>" + testReportId + "</reportId>" +
                        "</getReport>"
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/soap/DailyReportService")
                        .contentType(MediaType.TEXT_XML)
                        .content(soapRequest)
                        .accept(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:getReportResponse/reportResponse/success")
                        .string("false"))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:getReportResponse/reportResponse/errorMessage")
                        .exists())
                .andReturn();

        // Verify service was called
        verify(reportingService).getReport(testReportId);
    }

    @Test
    @DisplayName("Should get report progress via SOAP")
    @WithMockUser(username = "sergey", password = "chapman", roles = "ADMIN")
    void getReportProgress_shouldReturnProgress_whenReportExists() throws Exception {
        // Arrange
        when(reportingService.calculateReportProgress(testReportId)).thenReturn(75.0);

        // Create SOAP envelope for getReportProgress request
        String soapRequest = createSoapEnvelope(
                "<getReportProgress xmlns=\"http://reporting.construction.com/soap\">" +
                        "<reportId>" + testReportId + "</reportId>" +
                        "</getReportProgress>"
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/soap/DailyReportService")
                        .contentType(MediaType.TEXT_XML)
                        .content(soapRequest)
                        .accept(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:getReportProgressResponse/progressResponse/success")
                        .string("true"))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:getReportProgressResponse/progressResponse/progress")
                        .string("75.0"))
                .andReturn();

        // Verify service was called
        verify(reportingService).calculateReportProgress(testReportId);
    }

    @Test
    @DisplayName("Should add activity via SOAP")
    @WithMockUser(username = "sergey", password = "chapman", roles = "ADMIN")
    void addActivity_shouldAddActivity_whenValidDataProvided() throws Exception {
        // Arrange
        when(reportingService.addActivityToReport(eq(testReportId), any(ActivityEntry.class)))
                .thenReturn(testActivity);

        // Create SOAP envelope for addActivity request with ActivitySoapRequest
        LocalDateTime startTime = LocalDateTime.now().minusHours(3);
        LocalDateTime endTime = LocalDateTime.now().minusHours(2);

        String soapRequest = createSoapEnvelope(
                "<addActivity xmlns=\"http://reporting.construction.com/soap\">" +
                        "<activity>" +
                        "<reportId>" + testReportId + "</reportId>" +
                        "<description>New activity</description>" +
                        "<category>New category</category>" +
                        "<startTime>" + startTime + "</startTime>" +
                        "<endTime>" + endTime + "</endTime>" +
                        "<progress>25.0</progress>" +
                        "<status>PLANNED</status>" +
                        "<notes>Activity notes</notes>" +
                        "<personnel>person1,person2</personnel>" +
                        "<username>" + testUsername + "</username>" +
                        "</activity>" +
                        "</addActivity>"
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/soap/DailyReportService")
                        .contentType(MediaType.TEXT_XML)
                        .content(soapRequest)
                        .accept(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:addActivityResponse/activityResponse/success")
                        .string("true"))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:addActivityResponse/activityResponse/id")
                        .string(testActivityId))
                .andReturn();

        // Verify service was called
        verify(reportingService).addActivityToReport(eq(testReportId), any(ActivityEntry.class));
    }

    @Test
    @DisplayName("Should get activities by report via SOAP")
    @WithMockUser(username = "sergey", password = "chapman", roles = "ADMIN")
    void getActivitiesByReport_shouldReturnActivities_whenReportExists() throws Exception {
        // Arrange
        List<ActivityEntry> activities = Collections.singletonList(testActivity);
        when(reportingService.getActivitiesByReport(testReportId)).thenReturn(activities);

        // Create SOAP envelope for getActivitiesByReport request
        String soapRequest = createSoapEnvelope(
                "<getActivitiesByReport xmlns=\"http://reporting.construction.com/soap\">" +
                        "<reportId>" + testReportId + "</reportId>" +
                        "</getActivitiesByReport>"
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/soap/DailyReportService")
                        .contentType(MediaType.TEXT_XML)
                        .content(soapRequest)
                        .accept(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:getActivitiesByReportResponse/activityListResponse/success")
                        .string("true"))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:getActivitiesByReportResponse/activityListResponse/activityCount")
                        .string("1"))
                .andReturn();

        // Verify service was called
        verify(reportingService).getActivitiesByReport(testReportId);
    }

    @Test
    @DisplayName("Should check if report is complete via SOAP")
    @WithMockUser(username = "sergey", password = "chapman", roles = "ADMIN")
    void isReportComplete_shouldReturnCompletionStatus_whenReportExists() throws Exception {
        // Arrange
        when(reportingService.isReportComplete(testReportId)).thenReturn(true);

        // Create SOAP envelope for isReportComplete request
        String soapRequest = createSoapEnvelope(
                "<isReportComplete xmlns=\"http://reporting.construction.com/soap\">" +
                        "<reportId>" + testReportId + "</reportId>" +
                        "</isReportComplete>"
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/soap/DailyReportService")
                        .contentType(MediaType.TEXT_XML)
                        .content(soapRequest)
                        .accept(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_XML))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:isReportCompleteResponse/completionResponse/success")
                        .string("true"))
                .andExpect(MockMvcResultMatchers.xpath(
                                "//ns2:isReportCompleteResponse/completionResponse/complete")
                        .string("true"))
                .andReturn();

        // Verify service was called
        verify(reportingService).isReportComplete(testReportId);
    }

    // Helper method to create a SOAP envelope
    private String createSoapEnvelope(String body) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                body +
                "</soap:Body>" +
                "</soap:Envelope>";
    }

    // Helper method to marshal an object to XML string
    private <T> String marshalObject(T obj) throws JAXBException {
        StringWriter writer = new StringWriter();
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(obj, writer);
        return writer.toString();
    }

    // Helper method to unmarshal XML string to an object
    private <T> T unmarshalObject(String xml, Class<T> clazz) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (T) unmarshaller.unmarshal(new StringReader(xml));
    }
}