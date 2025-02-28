package com.se498.dailyreporting.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DailyReportGraphQLControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private DailyReportingServiceImpl reportingService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetReport() throws Exception {
        // Mock data
        DailyReport report = new DailyReport();
        report.setId("test-report-id");
        report.setProjectId("test-project");
        report.setReportDate(LocalDate.now());
        report.setStatus(ReportStatus.DRAFT);
        report.setCreatedAt(LocalDateTime.now());
        report.setCreatedBy("testuser");

        when(reportingService.getReport("test-report-id")).thenReturn(Optional.of(report));

        // GraphQL query
        String query = """
            {
              "query": "query { report(id: \\"test-report-id\\") { id projectId status createdBy } }"
            }
            """;

        // Execute and verify
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(query))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);

        assertEquals("test-report-id", root.path("data").path("report").path("id").asText());
        assertEquals("test-project", root.path("data").path("report").path("projectId").asText());
        assertEquals("DRAFT", root.path("data").path("report").path("status").asText());
        assertEquals("testuser", root.path("data").path("report").path("createdBy").asText());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetReportsByProject() throws Exception {
        // Mock data
        DailyReport report1 = new DailyReport();
        report1.setId("report-1");
        report1.setProjectId("test-project");
        report1.setReportDate(LocalDate.now().minusDays(1));
        report1.setStatus(ReportStatus.DRAFT);
        report1.setCreatedAt(LocalDateTime.now());
        report1.setCreatedBy("testuser");

        DailyReport report2 = new DailyReport();
        report2.setId("report-2");
        report2.setProjectId("test-project");
        report2.setReportDate(LocalDate.now());
        report2.setStatus(ReportStatus.SUBMITTED);
        report2.setCreatedAt(LocalDateTime.now());
        report2.setCreatedBy("testuser");

        when(reportingService.getReportsByProject("test-project"))
                .thenReturn(Arrays.asList(report1, report2));

        // GraphQL query
        String query = """
            {
              "query": "query { reportsByProject(projectId: \\"test-project\\") { id status } }"
            }
            """;

        // Execute and verify
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(query))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);

        JsonNode reports = root.path("data").path("reportsByProject");
        assertTrue(reports.isArray());
        assertEquals(2, reports.size());
        assertEquals("report-1", reports.get(0).path("id").asText());
        assertEquals("DRAFT", reports.get(0).path("status").asText());
        assertEquals("report-2", reports.get(1).path("id").asText());
        assertEquals("SUBMITTED", reports.get(1).path("status").asText());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateReport() throws Exception {
        // Mock data
        DailyReport report = new DailyReport();
        report.setId("new-report-id");
        report.setProjectId("test-project");
        report.setReportDate(LocalDate.parse("2023-05-15"));
        report.setStatus(ReportStatus.DRAFT);
        report.setCreatedAt(LocalDateTime.now());
        report.setCreatedBy("testuser");

        when(reportingService.createReport("test-project", LocalDate.parse("2023-05-15"), "testuser"))
                .thenReturn(report);
        when(reportingService.updateReport("new-report-id", "Test notes", "testuser"))
                .thenReturn(report);

        // GraphQL mutation
        String mutation = """
            {
              "query": "mutation { createReport(input: { projectId: \\"test-project\\", reportDate: \\"2023-05-15\\", notes: \\"Test notes\\" }) { id projectId reportDate status } }"
            }
            """;

        // Execute and verify
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mutation))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);

        JsonNode createdReport = root.path("data").path("createReport");
        assertEquals("new-report-id", createdReport.path("id").asText());
        assertEquals("test-project", createdReport.path("projectId").asText());
        assertEquals("2023-05-15", createdReport.path("reportDate").asText());
        assertEquals("DRAFT", createdReport.path("status").asText());
    }
}