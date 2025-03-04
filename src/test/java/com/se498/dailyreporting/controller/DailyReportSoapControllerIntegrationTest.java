package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import lombok.Data;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DailyReportSoapControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DailyReportingService reportingService;

    private WebServiceTemplate webServiceTemplate;
    private String projectId;
    private String reportId;
    private String username = "test-user";
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

    // ==== SOAP Response Classes ====

    @Data
    @XmlRootElement(name = "dailyReportSoapResponse", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DailyReportSoapResponse {
        @XmlElement(required = true)
        private boolean success;

        @XmlElement
        private String errorMessage;

        @XmlElement
        private String id;

        @XmlElement
        private String projectId;

        @XmlElement
        private String reportDate;

        @XmlElement
        private String status;

        @XmlElement
        private String notes;

        @XmlElement
        private String createdAt;

        @XmlElement
        private String createdBy;

        @XmlElement
        private String updatedAt;

        @XmlElement
        private String updatedBy;

        @XmlElement
        private double progress;

        @XmlElement
        private boolean complete;
    }

    @Data
    @XmlRootElement(name = "activitySoapResponse", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ActivitySoapResponse {
        @XmlElement(required = true)
        private boolean success;

        @XmlElement
        private String errorMessage;

        @XmlElement
        private String id;

        @XmlElement
        private String reportId;

        @XmlElement
        private String description;

        @XmlElement
        private String category;

        @XmlElement
        private String startTime;

        @XmlElement
        private String endTime;

        @XmlElement
        private double progress;

        @XmlElement
        private String status;

        @XmlElement
        private String notes;

        @XmlElement
        private String personnel;

        @XmlElement
        private String createdAt;

        @XmlElement
        private String createdBy;

        @XmlElement
        private String updatedAt;

        @XmlElement
        private long durationMinutes;
    }

    @Data
    @XmlRootElement(name = "activitySoapRequest", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ActivitySoapRequest {
        @XmlElement
        private String activityId;

        @XmlElement(required = true)
        private String reportId;

        @XmlElement(required = true)
        private String description;

        @XmlElement(required = true)
        private String category;

        @XmlElement(required = true)
        private String startTime;

        @XmlElement(required = true)
        private String endTime;

        @XmlElement(required = true)
        private double progress;

        @XmlElement(required = true)
        private String status;

        @XmlElement
        private String notes;

        @XmlElement
        private String personnel;

        @XmlElement(required = true)
        private String username;
    }

    @Data
    @XmlRootElement(name = "serviceResponse", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ServiceResponse {
        @XmlElement(required = true)
        private boolean success;

        @XmlElement(required = true)
        private String message;

        public ServiceResponse() {}

        public ServiceResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    @Data
    @XmlRootElement(name = "reportListResponse", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ReportListResponse {
        @XmlElement(required = true)
        private boolean success;

        @XmlElement
        private String errorMessage;

        @XmlElement(required = true)
        private int reportCount;

        @XmlElement
        private List<DailyReportSoapResponse> reports;
    }

    @Data
    @XmlRootElement(name = "activityListResponse", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ActivityListResponse {
        @XmlElement(required = true)
        private boolean success;

        @XmlElement
        private String errorMessage;

        @XmlElement(required = true)
        private int activityCount;

        @XmlElement
        private List<ActivitySoapResponse> activities;
    }

    @Data
    @XmlRootElement(name = "progressResponse", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ProgressResponse {
        @XmlElement(required = true)
        private boolean success;

        @XmlElement(required = true)
        private double progress;

        @XmlElement
        private String message;
    }

    @Data
    @XmlRootElement(name = "completionResponse", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CompletionResponse {
        @XmlElement(required = true)
        private boolean success;

        @XmlElement(required = true)
        private boolean complete;

        @XmlElement
        private String message;
    }

    @Data
    @XmlRootElement(name = "durationResponse", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DurationResponse {
        @XmlElement(required = true)
        private boolean success;

        @XmlElement(required = true)
        private long durationMinutes;

        @XmlElement
        private String message;
    }

    // ==== SOAP Request Classes ====

    @Data
    @XmlRootElement(name = "getReport", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GetReportRequest {
        @XmlElement(required = true)
        private String reportId;

        public GetReportRequest() {}

        public GetReportRequest(String reportId) {
            this.reportId = reportId;
        }
    }

    @Data
    @XmlRootElement(name = "createReport", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CreateReportRequest {
        @XmlElement(required = true)
        private String projectId;

        @XmlElement(required = true)
        private String reportDate;

        @XmlElement
        private String notes;

        @XmlElement(required = true)
        private String username;

        public CreateReportRequest() {}

        public CreateReportRequest(String projectId, String reportDate, String notes, String username) {
            this.projectId = projectId;
            this.reportDate = reportDate;
            this.notes = notes;
            this.username = username;
        }
    }

    @Data
    @XmlRootElement(name = "updateReport", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UpdateReportRequest {
        @XmlElement(required = true)
        private String reportId;

        @XmlElement
        private String notes;

        @XmlElement(required = true)
        private String username;

        public UpdateReportRequest() {}

        public UpdateReportRequest(String reportId, String notes, String username) {
            this.reportId = reportId;
            this.notes = notes;
            this.username = username;
        }
    }

    @Data
    @XmlRootElement(name = "submitReport", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SubmitReportRequest {
        @XmlElement(required = true)
        private String reportId;

        @XmlElement(required = true)
        private String username;

        public SubmitReportRequest() {}

        public SubmitReportRequest(String reportId, String username) {
            this.reportId = reportId;
            this.username = username;
        }
    }

    @Data
    @XmlRootElement(name = "getActivitiesByReport", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GetActivitiesByReportRequest {
        @XmlElement(required = true)
        private String reportId;

        public GetActivitiesByReportRequest() {}

        public GetActivitiesByReportRequest(String reportId) {
            this.reportId = reportId;
        }
    }

    @Data
    @XmlRootElement(name = "getReportProgress", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GetReportProgressRequest {
        @XmlElement(required = true)
        private String reportId;

        public GetReportProgressRequest() {}

        public GetReportProgressRequest(String reportId) {
            this.reportId = reportId;
        }
    }

    @Data
    @XmlRootElement(name = "isReportComplete", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IsReportCompleteRequest {
        @XmlElement(required = true)
        private String reportId;

        public IsReportCompleteRequest() {}

        public IsReportCompleteRequest(String reportId) {
            this.reportId = reportId;
        }
    }

    @Data
    @XmlRootElement(name = "updateActivityProgress", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UpdateActivityProgressRequest {
        @XmlElement(required = true)
        private String activityId;

        @XmlElement(required = true)
        private double progress;

        @XmlElement(required = true)
        private String username;

        public UpdateActivityProgressRequest() {}

        public UpdateActivityProgressRequest(String activityId, double progress, String username) {
            this.activityId = activityId;
            this.progress = progress;
            this.username = username;
        }
    }

    @Data
    @XmlRootElement(name = "getReportsByProject", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GetReportsByProjectRequest {
        @XmlElement(required = true)
        private String projectId;

        public GetReportsByProjectRequest() {}

        public GetReportsByProjectRequest(String projectId) {
            this.projectId = projectId;
        }
    }

    @Data
    @XmlRootElement(name = "getReportsByStatus", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GetReportsByStatusRequest {
        @XmlElement(required = true)
        private String status;

        public GetReportsByStatusRequest() {}

        public GetReportsByStatusRequest(String status) {
            this.status = status;
        }
    }

    @Data
    @XmlRootElement(name = "deleteActivity", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DeleteActivityRequest {
        @XmlElement(required = true)
        private String activityId;

        public DeleteActivityRequest() {}

        public DeleteActivityRequest(String activityId) {
            this.activityId = activityId;
        }
    }

    @Data
    @XmlRootElement(name = "deleteReport", namespace = "http://reporting.construction.com/soap")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DeleteReportRequest {
        @XmlElement(required = true)
        private String reportId;

        public DeleteReportRequest() {}

        public DeleteReportRequest(String reportId) {
            this.reportId = reportId;
        }
    }

    @BeforeEach
    public void setUp() {
        // Set up WebServiceTemplate for SOAP requests
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        // Set classes to be bound explicitly
        marshaller.setClassesToBeBound(
                // Response classes
                DailyReportSoapResponse.class,
                ActivitySoapResponse.class,
                ActivitySoapRequest.class,
                ServiceResponse.class,
                ReportListResponse.class,
                ActivityListResponse.class,
                ProgressResponse.class,
                CompletionResponse.class,
                DurationResponse.class,

                // Request classes
                GetReportRequest.class,
                CreateReportRequest.class,
                UpdateReportRequest.class,
                SubmitReportRequest.class,
                GetActivitiesByReportRequest.class,
                GetReportProgressRequest.class,
                IsReportCompleteRequest.class,
                UpdateActivityProgressRequest.class,
                GetReportsByProjectRequest.class,
                GetReportsByStatusRequest.class,
                DeleteActivityRequest.class,
                DeleteReportRequest.class
        );

        try {
            marshaller.afterPropertiesSet();
            System.out.println("Marshaller setup successful");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to set up marshaller: " + e.getMessage());
        }

        webServiceTemplate = new WebServiceTemplate(marshaller);
        webServiceTemplate.setDefaultUri("http://localhost:" + port + "/soap/DailyReportService");

        // Create test project ID
        projectId = "test-project-" + UUID.randomUUID();

        // Create a test report via service
        DailyReport report = createTestReport();
        reportId = report.getId();

        System.out.println("Test setup complete. Project ID: " + projectId + ", Report ID: " + reportId);
    }

    /**
     * Helper method to create a test report via service layer
     */
    private DailyReport createTestReport() {
        return reportingService.createReport(
                projectId,
                LocalDate.now(),
                username
        );
    }

    /**
     * Helper method to create a test activity in a report
     */
    private ActivityEntry createTestActivity(String reportId) {
        ActivityEntry activity = new ActivityEntry();
        activity.setId(UUID.randomUUID().toString());
        activity.setReportId(reportId);
        activity.setDescription("Test Activity");
        activity.setCategory("Testing");
        activity.setStartTime(LocalDateTime.now().minusHours(2));
        activity.setEndTime(LocalDateTime.now().minusHours(1));
        activity.setProgress(50.0);
        activity.setStatus(ActivityStatus.IN_PROGRESS);
        activity.setNotes("Test notes");
        activity.setCreatedBy(username);
        activity.setCreatedAt(LocalDateTime.now());

        Set<String> personnel = new HashSet<>();
        personnel.add("tester1");
        personnel.add("tester2");
        activity.setPersonnel(personnel);

        return reportingService.addActivityToReport(reportId, activity);
    }

    /**
     * Helper method to marshal object to XML for debugging
     */
    private String marshalToXml(Object obj) {
        try {
            jakarta.xml.bind.JAXBContext context = jakarta.xml.bind.JAXBContext.newInstance(obj.getClass());
            jakarta.xml.bind.Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);

            java.io.StringWriter sw = new java.io.StringWriter();
            marshaller.marshal(obj, sw);
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to marshal: " + e.getMessage();
        }
    }

    @Test
    public void testGetReport() {
        try {
            // Create request object
            GetReportRequest request = new GetReportRequest(reportId);

            // Debug XML output
            System.out.println("Request XML: " + marshalToXml(request));

            // Execute the SOAP request
            Object responseObj = webServiceTemplate.marshalSendAndReceive(request);

            System.out.println("Response type: " + (responseObj != null ? responseObj.getClass().getName() : "null"));

            // Verify the response
            assertNotNull(responseObj, "Response should not be null");
            assertTrue(responseObj instanceof DailyReportSoapResponse, "Should return DailyReportSoapResponse");

            DailyReportSoapResponse response = (DailyReportSoapResponse) responseObj;
            assertTrue(response.isSuccess(), "Response should indicate success");
            assertEquals(reportId, response.getId(), "Response should contain the correct report ID");
            assertEquals(projectId, response.getProjectId(), "Response should contain the correct project ID");
            assertEquals(ReportStatus.DRAFT.name(), response.getStatus(), "New report should be in DRAFT status");
            assertEquals(username, response.getCreatedBy(), "Report should be created by the test user");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    public void testCreateReport() {
        try {
            // Create request data
            String newProjectId = "new-project-" + UUID.randomUUID();
            LocalDate reportDate = LocalDate.now();
            String notes = "Test notes for new report";

            // Create request object
            CreateReportRequest request = new CreateReportRequest(
                    newProjectId,
                    reportDate.toString(),
                    notes,
                    username
            );

            // Debug XML output
            System.out.println("Request XML: " + marshalToXml(request));

            // Execute the SOAP request
            Object responseObj = webServiceTemplate.marshalSendAndReceive(request);

            // Verify the response
            assertNotNull(responseObj, "Response should not be null");
            assertTrue(responseObj instanceof DailyReportSoapResponse, "Should return DailyReportSoapResponse");

            DailyReportSoapResponse response = (DailyReportSoapResponse) responseObj;
            assertTrue(response.isSuccess(), "Response should indicate success");
            assertNotNull(response.getId(), "Response should contain a report ID");
            assertEquals(newProjectId, response.getProjectId(), "Response should contain the correct project ID");
            assertEquals(reportDate.toString(), response.getReportDate(), "Response should contain the correct report date");
            assertEquals(notes, response.getNotes(), "Response should contain the correct notes");
            assertEquals(ReportStatus.DRAFT.name(), response.getStatus(), "New report should be in DRAFT status");
            assertEquals(username, response.getCreatedBy(), "Report should be created by the test user");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateReport() {
        try {
            // Create update data
            String updatedNotes = "Updated test notes";

            // Create request object
            UpdateReportRequest request = new UpdateReportRequest(reportId, updatedNotes, username);

            // Debug XML output
            System.out.println("Request XML: " + marshalToXml(request));

            // Execute the SOAP request
            Object responseObj = webServiceTemplate.marshalSendAndReceive(request);

            // Verify the response
            assertNotNull(responseObj, "Response should not be null");
            assertTrue(responseObj instanceof DailyReportSoapResponse, "Should return DailyReportSoapResponse");

            DailyReportSoapResponse response = (DailyReportSoapResponse) responseObj;
            assertTrue(response.isSuccess(), "Response should indicate success");
            assertEquals(reportId, response.getId(), "Response should contain the correct report ID");
            assertEquals(updatedNotes, response.getNotes(), "Response should contain the updated notes");
            assertEquals(username, response.getUpdatedBy(), "Report should be updated by the test user");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    public void testSubmitReport() {
        try {
            // Add an activity to the report to make it submittable
            createTestActivity(reportId);

            // Create request object
            SubmitReportRequest request = new SubmitReportRequest(reportId, username);

            // Debug XML output
            System.out.println("Request XML: " + marshalToXml(request));

            // Execute the SOAP request
            Object responseObj = webServiceTemplate.marshalSendAndReceive(request);

            // Verify the response
            assertNotNull(responseObj, "Response should not be null");
            assertTrue(responseObj instanceof DailyReportSoapResponse, "Should return DailyReportSoapResponse");

            DailyReportSoapResponse response = (DailyReportSoapResponse) responseObj;
            assertTrue(response.isSuccess(), "Response should indicate success");
            assertEquals(reportId, response.getId(), "Response should contain the correct report ID");
            assertEquals(ReportStatus.SUBMITTED.name(), response.getStatus(), "Report status should be SUBMITTED");
            assertEquals(username, response.getUpdatedBy(), "Report should be updated by the test user");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    public void testAddActivity() {
        try {
            // Create activity data
            LocalDateTime startTime = LocalDateTime.now().minusHours(3);
            LocalDateTime endTime = LocalDateTime.now().minusHours(2);
            String description = "Test Activity via SOAP";
            String category = "SOAP Testing";
            double progress = 75.0;
            String personnel = "tester1,tester2";
            String notes = "Activity notes from SOAP test";

            // Create an ActivitySoapRequest
            ActivitySoapRequest request = new ActivitySoapRequest();
            request.setReportId(reportId);
            request.setDescription(description);
            request.setCategory(category);
            request.setStartTime(startTime.format(dateTimeFormatter));
            request.setEndTime(endTime.format(dateTimeFormatter));
            request.setProgress(progress);
            request.setStatus(ActivityStatus.IN_PROGRESS.name());
            request.setNotes(notes);
            request.setPersonnel(personnel);
            request.setUsername(username);

            // Debug XML output
            System.out.println("Request XML: " + marshalToXml(request));

            // Execute the SOAP request
            Object responseObj = webServiceTemplate.marshalSendAndReceive(request);

            // Verify the response
            assertNotNull(responseObj, "Response should not be null");
            assertTrue(responseObj instanceof ActivitySoapResponse, "Should return ActivitySoapResponse");

            ActivitySoapResponse response = (ActivitySoapResponse) responseObj;
            assertTrue(response.isSuccess(), "Response should indicate success");
            assertNotNull(response.getId(), "Response should contain an activity ID");
            assertEquals(reportId, response.getReportId(), "Response should contain the correct report ID");
            assertEquals(description, response.getDescription(), "Response should contain the correct description");
            assertEquals(category, response.getCategory(), "Response should contain the correct category");
            assertEquals(progress, response.getProgress(), "Response should contain the correct progress");
            assertEquals(ActivityStatus.IN_PROGRESS.name(), response.getStatus(), "Activity status should be IN_PROGRESS");
            assertEquals(notes, response.getNotes(), "Response should contain the correct notes");
            assertEquals(personnel, response.getPersonnel(), "Response should contain the correct personnel");
            assertEquals(username, response.getCreatedBy(), "Activity should be created by the test user");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateActivityProgress() {
        try {
            // Create a test activity
            ActivityEntry activity = createTestActivity(reportId);
            double newProgress = 85.0;

            // Create request object
            UpdateActivityProgressRequest request = new UpdateActivityProgressRequest(
                    activity.getId(),
                    newProgress,
                    username
            );

            // Debug XML output
            System.out.println("Request XML: " + marshalToXml(request));

            // Execute the SOAP request
            Object responseObj = webServiceTemplate.marshalSendAndReceive(request);

            // Verify the response
            assertNotNull(responseObj, "Response should not be null");
            assertTrue(responseObj instanceof ActivitySoapResponse, "Should return ActivitySoapResponse");

            ActivitySoapResponse response = (ActivitySoapResponse) responseObj;
            assertTrue(response.isSuccess(), "Response should indicate success");
            assertEquals(activity.getId(), response.getId(), "Response should contain the correct activity ID");
            assertEquals(newProgress, response.getProgress(), 0.01, "Response should contain the updated progress");

            // Verify the update in the database
            Optional<ActivityEntry> updatedActivity = reportingService.getActivity(activity.getId());
            assertTrue(updatedActivity.isPresent(), "Activity should exist in the database");
            assertEquals(newProgress, updatedActivity.get().getProgress(), 0.01, "Activity progress should be updated in the database");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    public void testGetActivitiesByReport() {
        try {
            // Create a test activity
            ActivityEntry activity = createTestActivity(reportId);

            // Create request object
            GetActivitiesByReportRequest request = new GetActivitiesByReportRequest(reportId);

            // Debug XML output
            System.out.println("Request XML: " + marshalToXml(request));

            // Execute the SOAP request
            Object responseObj = webServiceTemplate.marshalSendAndReceive(request);

            // Verify the response
            assertNotNull(responseObj, "Response should not be null");
            assertTrue(responseObj instanceof ActivityListResponse, "Should return ActivityListResponse");

            ActivityListResponse response = (ActivityListResponse) responseObj;
            assertTrue(response.isSuccess(), "Response should indicate success");
            assertTrue(response.getActivityCount() > 0, "Response should contain at least one activity");
            assertNotNull(response.getActivities(), "Response should contain an activities list");
            assertFalse(response.getActivities().isEmpty(), "Activities list should not be empty");

            // Check the first activity
            ActivitySoapResponse activityResponse = response.getActivities().get(0);
            assertEquals(reportId, activityResponse.getReportId(), "Activity should belong to the correct report");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    public void testGetReportsByProject() {
        try {
            // Create request object
            GetReportsByProjectRequest request = new GetReportsByProjectRequest(projectId);

            // Debug XML output
            System.out.println("Request XML: " + marshalToXml(request));

            // Execute the SOAP request
            Object responseObj = webServiceTemplate.marshalSendAndReceive(request);

            // Verify the response
            assertNotNull(responseObj, "Response should not be null");
            assertTrue(responseObj instanceof ReportListResponse, "Should return ReportListResponse");

            ReportListResponse response = (ReportListResponse) responseObj;
            assertTrue(response.isSuccess(), "Response should indicate success");
            assertTrue(response.getReportCount() > 0, "Response should contain at least one report");
            assertNotNull(response.getReports(), "Response should contain a reports list");
            assertFalse(response.getReports().isEmpty(), "Reports list should not be empty");

            // Check if our test report is in the list
            boolean foundTestReport = response.getReports().stream()
                    .anyMatch(report -> report.getId().equals(reportId));
            assertTrue(foundTestReport, "Response should contain the test report");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteActivity() {
        try {
            // Create a test activity
            ActivityEntry activity = createTestActivity(reportId);

            // Create request object
            DeleteActivityRequest request = new DeleteActivityRequest(activity.getId());

            // Debug XML output
            System.out.println("Request XML: " + marshalToXml(request));

            // Execute the SOAP request
            Object responseObj = webServiceTemplate.marshalSendAndReceive(request);

            // Verify the response
            assertNotNull(responseObj, "Response should not be null");
            assertTrue(responseObj instanceof ServiceResponse, "Should return ServiceResponse");

            ServiceResponse response = (ServiceResponse) responseObj;
            assertTrue(response.isSuccess(), "Response should indicate success");

            // Verify the activity was deleted
            Optional<ActivityEntry> deletedActivity = reportingService.getActivity(activity.getId());
            assertFalse(deletedActivity.isPresent(), "Activity should be deleted from the database");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }
}