package com.se498.dailyreporting.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DailyReportGraphQLControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DailyReportingService reportingService;

    @Autowired
    private EntityManager entityManager;

    private ObjectMapper objectMapper;
    private String testProjectId;
    private String testReportId;
    private String testActivityId;
    private DateTimeFormatter dateTimeFormatter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

        // Create test data with unique identifiers
        testProjectId = "test-project-" + UUID.randomUUID();

        // Create a test report
        DailyReport report = reportingService.createReport(
                testProjectId,
                LocalDate.now(),
                "test-user"
        );
        testReportId = report.getId();

        // Create a test activity
        ActivityEntry activity = new ActivityEntry();
        activity.setId(UUID.randomUUID().toString());
        activity.setReportId(testReportId);
        activity.setDescription("Test Activity");
        activity.setCategory("Test Category");
        activity.setStartTime(LocalDateTime.now().minusHours(2));
        activity.setEndTime(LocalDateTime.now().minusHours(1));
        activity.setProgress(50.0);
        activity.setStatus(ActivityStatus.IN_PROGRESS);
        activity.setNotes("Test Notes");
        activity.setCreatedBy("test-user");
        activity.setCreatedAt(LocalDateTime.now());

        Set<String> personnel = new HashSet<>();
        personnel.add("person1");
        activity.setPersonnel(personnel);

        ActivityEntry savedActivity = reportingService.addActivityToReport(testReportId, activity);
        testActivityId = savedActivity.getId();
    }

    @Test
    @WithMockUser(username = "test-user")
    void testQueryReportById() throws Exception {
        // GraphQL query
        String query = String.format("""
            {
              "query": "query { report(id: \\"%s\\") { id projectId status createdBy } }"
            }
            """, testReportId);

        // Execute query
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(query))
                .andExpect(status().isOk())
                .andReturn();

        // Parse and verify response
        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);

        assertEquals(testReportId, root.path("data").path("report").path("id").asText());
        assertEquals(testProjectId, root.path("data").path("report").path("projectId").asText());
        assertEquals("DRAFT", root.path("data").path("report").path("status").asText());
        assertEquals("test-user", root.path("data").path("report").path("createdBy").asText());
    }

    @Test
    @WithMockUser(username = "test-user")
    void testQueryReportsByProject() throws Exception {
        // GraphQL query
        String query = String.format("""
            {
              "query": "query { reportsByProject(projectId: \\"%s\\") { id status } }"
            }
            """, testProjectId);

        // Execute query
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(query))
                .andExpect(status().isOk())
                .andReturn();

        // Parse and verify response
        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);

        JsonNode reports = root.path("data").path("reportsByProject");
        assertTrue(reports.isArray());
        assertEquals(testReportId, reports.get(0).path("id").asText());
        assertEquals("DRAFT", reports.get(0).path("status").asText());
    }

    @Test
    @WithMockUser(username = "test-user")
    void testQueryActivity() throws Exception {
        // GraphQL query
        String query = String.format("""
            {
              "query": "query { activity(id: \\"%s\\") { id description category status progress } }"
            }
            """, testActivityId);

        // Execute query
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(query))
                .andExpect(status().isOk())
                .andReturn();

        // Parse and verify response
        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);

        JsonNode activity = root.path("data").path("activity");
        assertEquals(testActivityId, activity.path("id").asText());
        assertEquals("Test Activity", activity.path("description").asText());
        assertEquals("Test Category", activity.path("category").asText());
        assertEquals("IN_PROGRESS", activity.path("status").asText());
        assertEquals(50.0, activity.path("progress").asDouble());
    }

    @Test
    @WithMockUser(username = "test-user")
    void testCreateAndUpdateReport() throws Exception {
        // Create a new report
        String newProjectId = "new-project-" + UUID.randomUUID();
        LocalDate reportDate = LocalDate.now().plusDays(1);

        String createMutation = String.format("""
            {
              "query": "mutation { createReport(input: { projectId: \\"%s\\", reportDate: \\"%s\\", notes: \\"Test Notes\\" }) { id projectId reportDate status notes } }"
            }
            """, newProjectId, reportDate);

        MvcResult createResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createMutation))
                .andExpect(status().isOk())
                .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        JsonNode createRoot = objectMapper.readTree(createResponseBody);
        String newReportId = createRoot.path("data").path("createReport").path("id").asText();

        // Update the newly created report
        String updateMutation = String.format("""
            {
              "query": "mutation { updateReport(id: \\"%s\\", input: { projectId: \\"%s\\", reportDate: \\"%s\\", notes: \\"Updated Notes\\" }) { id notes } }"
            }
            """, newReportId, newProjectId, reportDate);

        MvcResult updateResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateMutation))
                .andExpect(status().isOk())
                .andReturn();

        String updateResponseBody = updateResult.getResponse().getContentAsString();
        JsonNode updateRoot = objectMapper.readTree(updateResponseBody);

        assertEquals(newReportId, updateRoot.path("data").path("updateReport").path("id").asText());
        assertEquals("Updated Notes", updateRoot.path("data").path("updateReport").path("notes").asText());
    }

    @Test
    @WithMockUser(username = "test-user")
    void testReportWorkflow() throws Exception {
        // Submit the report
        String submitMutation = String.format("""
            {
              "query": "mutation { submitReport(id: \\"%s\\") { id status } }"
            }
            """, testReportId);

        MvcResult submitResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(submitMutation))
                .andExpect(status().isOk())
                .andReturn();

        String submitResponseBody = submitResult.getResponse().getContentAsString();
        JsonNode submitRoot = objectMapper.readTree(submitResponseBody);

        assertEquals(testReportId, submitRoot.path("data").path("submitReport").path("id").asText());
        assertEquals("SUBMITTED", submitRoot.path("data").path("submitReport").path("status").asText());

        // Approve the report
        String approveMutation = String.format("""
            {
              "query": "mutation { approveReport(id: \\"%s\\") { id status } }"
            }
            """, testReportId);

        MvcResult approveResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(approveMutation))
                .andExpect(status().isOk())
                .andReturn();

        String approveResponseBody = approveResult.getResponse().getContentAsString();
        JsonNode approveRoot = objectMapper.readTree(approveResponseBody);

        assertEquals(testReportId, approveRoot.path("data").path("approveReport").path("id").asText());
        assertEquals("APPROVED", approveRoot.path("data").path("approveReport").path("status").asText());
    }

    @Test
    @WithMockUser(username = "test-user")
    void testAddActivity() throws Exception {
        // Truncate times to millisecond precision
        LocalDateTime startTime = LocalDateTime.now().minusHours(4).truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime endTime = LocalDateTime.now().minusHours(3).truncatedTo(ChronoUnit.MILLIS);

        // Using ISO_OFFSET_DATE_TIME for RFC 3339 compliance (with offset)
        ZonedDateTime zonedDateTimeStart = startTime.atOffset(ZoneOffset.UTC).toZonedDateTime();
        ZonedDateTime zonedDateEnd = startTime.atOffset(ZoneOffset.UTC).toZonedDateTime();

        String addMutation = String.format("""
        {
          "query": "mutation { addActivity(reportId: \\"%s\\", input: { description: \\"New Activity\\", category: \\"New Category\\", startTime: \\"%s\\", endTime: \\"%s\\", progress: 25.0, status: PLANNED, notes: \\"New Notes\\", personnel: [\\"person1\\", \\"person2\\"] }) { id description category progress status } }"
        }
        """, testReportId, zonedDateTimeStart.format(dateTimeFormatter), zonedDateEnd.format(dateTimeFormatter));

        MvcResult addResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(addMutation))
                .andExpect(status().isOk())
                .andReturn();

        String addResponseBody = addResult.getResponse().getContentAsString();
        System.out.println("RESPONSE: " + addResponseBody);

        JsonNode addRoot = objectMapper.readTree(addResponseBody);

        // Check for errors
        if (addRoot.has("errors")) {
            fail("GraphQL returned errors: " + addRoot.path("errors").toString());
        }

        // Verify response data
        assertNotNull(addRoot.path("data").path("addActivity").path("id").asText());
        assertEquals("New Activity", addRoot.path("data").path("addActivity").path("description").asText());
    }

    @Test
    @WithMockUser(username = "test-user")
    void testUpdateActivityProgress() throws Exception {
        // Instead of creating a new activity via GraphQL, use the activity we created in setUp()
        // This activity (testActivityId) should already be properly persisted

        // For debugging, verify the activity exists
        Optional<ActivityEntry> activityOpt = reportingService.getActivity(testActivityId);
        assertTrue(activityOpt.isPresent(), "Test activity should exist before update");

        // Update this existing activity's progress
        String progressMutation = String.format("""
        {
          "query": "mutation { updateActivityProgress(id: \\"%s\\", input: { progress: 90.0 }) { id progress status } }"
        }
        """, testActivityId);  // Use testActivityId created in setUp

        MvcResult progressResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(progressMutation))
                .andExpect(status().isOk())
                .andReturn();

        String progressResponseBody = progressResult.getResponse().getContentAsString();
        JsonNode progressRoot = objectMapper.readTree(progressResponseBody);

        assertEquals(testActivityId, progressRoot.path("data").path("updateActivityProgress").path("id").asText());
        assertEquals(90.0, progressRoot.path("data").path("updateActivityProgress").path("progress").asDouble());
    }

    @Test
    @WithMockUser(username = "test-user")
    void testAddAndUpdateActivity() throws Exception {
        // Add a new activity
        // Truncate times to millisecond precision
        LocalDateTime startTime = LocalDateTime.now().minusHours(4).truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime endTime = LocalDateTime.now().minusHours(3).truncatedTo(ChronoUnit.MILLIS);

        // Using ISO_OFFSET_DATE_TIME for RFC 3339 compliance (with offset)
        ZonedDateTime zonedDateTimeStart = startTime.atOffset(ZoneOffset.UTC).toZonedDateTime();
        ZonedDateTime zonedDateEnd = startTime.atOffset(ZoneOffset.UTC).toZonedDateTime();

        String addMutation = String.format("""
        {
          "query": "mutation { addActivity(reportId: \\"%s\\", input: { description: \\"New Activity\\", category: \\"New Category\\", startTime: \\"%s\\", endTime: \\"%s\\", progress: 25.0, status: PLANNED, notes: \\"New Notes\\", personnel: [\\"person1\\", \\"person2\\"] }) { id description category progress status } }"
        }
        """, testReportId, zonedDateTimeStart.format(dateTimeFormatter), zonedDateEnd.format(dateTimeFormatter));

        MvcResult addResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(addMutation))
                .andExpect(status().isOk())
                .andReturn();

        String addResponseBody = addResult.getResponse().getContentAsString();
        JsonNode addRoot = objectMapper.readTree(addResponseBody);
        String newActivityId = addRoot.path("data").path("addActivity").path("id").asText();

        // Force a flush and clear the persistence context
        entityManager.flush();
        entityManager.clear();

        // Verify activity exists using service directly
        Optional<ActivityEntry> activityOpt = reportingService.getActivity(newActivityId);
        assertTrue(activityOpt.isPresent(), "Activity should exist before update");
        ActivityEntry activity = activityOpt.get();

        // Use service method directly instead of GraphQL for the update
        ActivityEntry updatedActivity = reportingService.updateActivityProgress(
                newActivityId, 90.0, "test-user");

        // Verify update was successful
        assertEquals(90.0, updatedActivity.getProgress());
        assertEquals(ActivityStatus.IN_PROGRESS, updatedActivity.getStatus());

        // Now you can optionally test via GraphQL to confirm the API works too
        String progressQuery = String.format("""
        {
          "query": "query { activity(id: \\"%s\\") { id progress status } }"
        }
        """, newActivityId);

        MvcResult queryResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(progressQuery))
                .andExpect(status().isOk())
                .andReturn();

        String queryResponseBody = queryResult.getResponse().getContentAsString();
        JsonNode queryRoot = objectMapper.readTree(queryResponseBody);

        assertEquals(90.0, queryRoot.path("data").path("activity").path("progress").asDouble());
        assertEquals("IN_PROGRESS", queryRoot.path("data").path("activity").path("status").asText());
    }

    @Test
    @WithMockUser(username = "test-user")
    void testReportAnalytics() throws Exception {
        // Query progress
        String progressQuery = String.format("""
            {
              "query": "query { reportProgress(reportId: \\"%s\\") }"
            }
            """, testReportId);

        MvcResult progressResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(progressQuery))
                .andExpect(status().isOk())
                .andReturn();

        String progressResponseBody = progressResult.getResponse().getContentAsString();
        JsonNode progressRoot = objectMapper.readTree(progressResponseBody);
        double progress = progressRoot.path("data").path("reportProgress").asDouble();

        assertEquals(50.0, progress);

        // Query completion status
        String completeQuery = String.format("""
            {
              "query": "query { isReportComplete(reportId: \\"%s\\") }"
            }
            """, testReportId);

        MvcResult completeResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(completeQuery))
                .andExpect(status().isOk())
                .andReturn();

        String completeResponseBody = completeResult.getResponse().getContentAsString();
        JsonNode completeRoot = objectMapper.readTree(completeResponseBody);
        boolean isComplete = completeRoot.path("data").path("isReportComplete").asBoolean();

        assertFalse(isComplete);
    }

    @Test
    @WithMockUser(username = "test-user")
    void testDeleteActivity() throws Exception {
        // Delete the activity
        String deleteMutation = String.format("""
            {
              "query": "mutation { deleteActivity(id: \\"%s\\") }"
            }
            """, testActivityId);

        MvcResult deleteResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/graphql")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(deleteMutation))
                .andExpect(status().isOk())
                .andReturn();

        String deleteResponseBody = deleteResult.getResponse().getContentAsString();
        JsonNode deleteRoot = objectMapper.readTree(deleteResponseBody);
        boolean deleted = deleteRoot.path("data").path("deleteActivity").asBoolean();

        assertTrue(deleted);

        // Verify activity is deleted
        assertTrue(reportingService.getActivity(testActivityId).isEmpty());
    }
}