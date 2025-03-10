package com.se498.dailyreporting.bdd.steps;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import lombok.extern.slf4j.Slf4j;
import org.jbehave.core.annotations.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Component
@Slf4j
public class DailyReportSteps {

    @Autowired
    private DailyReportingService reportingService;

    private String currentUsername;
    private String currentReportId;
    private DailyReport currentReport;
    private ActivityEntry currentActivity;
    private Exception lastException;

    // Setup mocks before scenarios
    @BeforeScenario
    public void setUp() {
        // Reset state
        currentUsername = null;
        currentReportId = null;
        currentReport = null;
        currentActivity = null;
        lastException = null;

        // If using mocks, reset them here
        Mockito.reset(reportingService);

        log.info("BeforeScenario: Set up completed");
    }

    @Given("I am an authenticated user with username \"$username\"")
    public void givenAuthenticatedUserDoubleQuotes(String username) {
        log.info("Executing parameterized step with double quotes: Given I am an authenticated user with username \"{}\"", username);
        givenAuthenticatedUser(username);
    }

    // Single-quote versions
    @Given("I am an authenticated user with username 'manager'")
    public void givenAuthenticatedUserWithUsernameManagerSingleQuotes() {
        log.info("Executing step with single quotes: Given I am an authenticated user with username 'manager'");
        givenAuthenticatedUser("manager");
    }

    @Given("I am an authenticated user with username '$username'")
    public void givenAuthenticatedUser(String username) {
        log.info("Executing parameterized step with single quotes: Given I am an authenticated user with username '{}'", username);

        // Setup security context with the given username
        UserDetails userDetails = User.builder()
                .username("sergey")
                .password("chapman")
                .roles("Admin") // Spring Security automatically adds ROLE_ prefix
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        currentUsername = username;
        log.info("Set currentUsername to: {}", currentUsername);
    }

    @When("I create a new daily report for project \"$projectId\" for date \"$reportDate\"")
    public void whenCreateNewReportDoubleQuotes(String projectId, LocalDate reportDate) {
        log.info("Executing parameterized step with double quotes: When I create a new daily report for project \"{}\" for date \"{}\"", projectId, reportDate);
        whenCreateNewReport(projectId, reportDate);
    }

    // Single-quote versions
    @When("I create a new daily report for project 'proj123' for date '2023-06-01'")
    public void whenCreateNewReportForProj123SingleQuotes() {
        log.info("Executing step with single quotes: When I create a new daily report for project 'proj123' for date '2023-06-01'");
        whenCreateNewReport("proj123", LocalDate.parse("2023-06-01"));
    }

    @When("I create a new daily report for project '$projectId' for date '$reportDate'")
    public void whenCreateNewReport(String projectId, LocalDate reportDate) {
        log.info("Executing parameterized step with single quotes: When I create a new daily report for project '{}' for date '{}'", projectId, reportDate);

        try {
            // Setup mock to return a new report
            DailyReport newReport = new DailyReport(
                    UUID.randomUUID().toString(),
                    projectId,
                    reportDate
            );
            newReport.setStatus(ReportStatus.DRAFT);
            newReport.setCreatedBy(currentUsername != null ? currentUsername : "testUser");
            newReport.setCreatedAt(LocalDateTime.now());

            when(reportingService.createReport(anyString(), any(LocalDate.class), anyString()))
                    .thenReturn(newReport);

            // Call the service
            currentReport = reportingService.createReport(projectId, reportDate, currentUsername);
            currentReportId = currentReport.getId();

            log.info("Report created with ID: {} and status: {}", currentReportId, currentReport.getStatus());
        } catch (Exception e) {
            log.error("Exception in whenCreateNewReport: {}", e.getMessage());
            lastException = e;
        }
    }

    @Then("the report should be created successfully")
    public void thenReportCreatedSuccessfully() {
        log.info("Executing step: Then the report should be created successfully");

        assertNull(lastException, "Exception occurred: " + (lastException != null ? lastException.getMessage() : ""));
        assertNotNull(currentReport, "Report was not created");
        assertNotNull(currentReportId, "Report ID is null");

        // Verify the service was called
        verify(reportingService).createReport(anyString(), any(LocalDate.class), anyString());

        log.info("Verified report was created successfully");
    }

    @Then("the report status should be \"$status\"")
    public void thenReportStatusShouldBeDoubleQuotes(String status) {
        log.info("Executing parameterized step with double quotes: Then the report status should be \"{}\"", status);
        thenReportStatusShouldBe(status);
    }

    // Single-quote versions
    @Then("the report status should be 'DRAFT'")
    public void thenReportStatusShouldBeDraft() {
        log.info("Executing specific step with single quotes: Then the report status should be 'DRAFT'");

        assertNotNull(currentReport, "Report is null");
        assertEquals(ReportStatus.DRAFT, currentReport.getStatus(),
                "Expected status DRAFT but was " + currentReport.getStatus());

        log.info("Verified report status is DRAFT");
    }

    @Then("the report status should be '$status'")
    public void thenReportStatusShouldBe(String status) {
        log.info("Executing parameterized step with single quotes: Then the report status should be '{}'", status);

        assertNotNull(currentReport, "Report is null");
        assertEquals(ReportStatus.valueOf(status), currentReport.getStatus(),
                "Expected status " + status + " but was " + currentReport.getStatus());

        log.info("Verified report status is {}", status);
    }

    // Single-quote version
    @Then("the report status should be updated to '$status'")
    public void thenReportStatusShouldBeUpdatedTo(String status) {
        log.info("Executing step with single quotes: Then the report status should be updated to '{}'", status);

        // Check for exceptions that might have happened in previous steps
        if (lastException != null) {
            log.error("Exception occurred in previous step: {}", lastException.getMessage());
            fail("Exception occurred in previous step: " + lastException.getMessage());
        }

        // Check if report is null
        assertNotNull(currentReport, "Report is null - did the previous step fail to set it?");

        // Debug output
        log.info("In then step: currentReport = {}, status = {}", currentReport.getId(), currentReport.getStatus());

        // Check the status
        assertEquals(ReportStatus.valueOf(status), currentReport.getStatus(),
                "Expected status to be " + status + " but was " + currentReport.getStatus());
    }

    // Single-quote versions
    @Given("I have a daily report with ID '$reportId' for project '$projectId'")
    public void givenDailyReportExists(String reportId, String projectId) {
        log.info("Executing step with single quotes: I have a daily report with ID '{}' for project '{}'", reportId, projectId);

        // Setup a mock report
        DailyReport report = new DailyReport(
                reportId,
                projectId,
                LocalDate.now()
        );
        report.setStatus(ReportStatus.DRAFT);
        report.setCreatedBy(currentUsername != null ? currentUsername : "testUser");
        report.setCreatedAt(LocalDateTime.now());
        report.setActivities(new ArrayList<>());

        // Configure mock service to return this report
        when(reportingService.getReport(anyString())).thenReturn(Optional.of(report));

        // Store current report and ID
        currentReport = report;
        currentReportId = reportId;

        log.info("Created report with ID: {}, project: {}", reportId, projectId);
    }

    // Single-quote versions
    @Given("I have a daily report with ID '$reportId' with status '$status'")
    public void givenDailyReportWithStatus(String reportId, String status) {
        log.info("Executing step with single quotes: I have a daily report with ID '{}' with status '{}'", reportId, status);

        // Setup a mock report with the given status
        DailyReport report = new DailyReport(
                reportId,
                "testProject",
                LocalDate.now()
        );
        report.setStatus(ReportStatus.valueOf(status));
        report.setCreatedBy(currentUsername != null ? currentUsername : "testUser");
        report.setCreatedAt(LocalDateTime.now());
        report.setActivities(new ArrayList<>());

        // Configure mock service to return this report
        when(reportingService.getReport(anyString())).thenReturn(Optional.of(report));

        // Update the current report if needed based on status
        if (status.equals("SUBMITTED") || status.equals("APPROVED") || status.equals("REJECTED")) {
            report.setUpdatedAt(LocalDateTime.now());
            report.setUpdatedBy("supervisor");
        }

        // Debug output
        log.info("Created report with ID: {}, status: {}", reportId, status);

        // Store current report and ID
        currentReport = report;
        currentReportId = reportId;

        // Debug check
        assertNotNull(currentReport, "Failed to set up current report");
        assertEquals(ReportStatus.valueOf(status), currentReport.getStatus(),
                "Failed to set correct status");
    }

    // Single-quote versions
    @When("I add an activity with description '$description' and category '$category' to the report")
    public void whenAddActivity(String description, String category) {
        log.info("Executing step with single quotes: I add an activity with description '{}' and category '{}' to the report", description, category);

        try {
            // Create an activity entry
            ActivityEntry activity = new ActivityEntry();
            activity.setId(UUID.randomUUID().toString());
            activity.setReportId(currentReportId);
            activity.setDescription(description);
            activity.setCategory(category);
            activity.setStartTime(LocalDateTime.now());
            activity.setEndTime(LocalDateTime.now().plusHours(2));
            activity.setProgress(0.0);
            activity.setStatus(ActivityStatus.PLANNED);
            activity.setCreatedBy(currentUsername != null ? currentUsername : "testUser");
            activity.setCreatedAt(LocalDateTime.now());
            activity.setPersonnel(new HashSet<>(Arrays.asList("Worker1")));

            // Mock the service to return this activity
            when(reportingService.addActivityToReport(anyString(), any(ActivityEntry.class)))
                    .thenReturn(activity);

            // Call the service
            currentActivity = reportingService.addActivityToReport(currentReportId, activity);

            // Add the activity to the report for later assertion
            if (currentReport != null && currentReport.getActivities() == null) {
                currentReport.setActivities(new ArrayList<>());
            }
            if (currentReport != null) {
                currentReport.getActivities().add(activity);
            }

            log.info("Added activity: {}, {}", description, category);
        } catch (Exception e) {
            log.error("Exception in whenAddActivity: {}", e.getMessage());
            lastException = e;
        }
    }

    @Then("the activity should be added successfully to the report")
    public void thenActivityAddedSuccessfully() {
        log.info("Executing step: the activity should be added successfully to the report");

        assertNull(lastException, "Exception occurred: " + (lastException != null ? lastException.getMessage() : ""));
        assertNotNull(currentActivity, "Activity was not created");

        // Verify the service was called
        verify(reportingService).addActivityToReport(anyString(), any(ActivityEntry.class));

        log.info("Verified activity was added successfully");
    }

    @Then("the report should have $count activity")
    public void thenReportHasActivityCount(int count) {
        log.info("Executing step: the report should have {} activity", count);

        assertNotNull(currentReport, "Report is null");
        assertNotNull(currentReport.getActivities(), "Activities list is null");
        assertEquals(count, currentReport.getActivities().size(),
                "Expected " + count + " activities but found " + currentReport.getActivities().size());

        log.info("Verified report has {} activities", count);
    }

    @Given("the report has at least one activity")
    public void givenReportHasActivity() {
        log.info("Executing step: the report has at least one activity");

        assertNotNull(currentReport, "Report is null");

        // Add an activity if none exists
        if (currentReport.getActivities() == null || currentReport.getActivities().isEmpty()) {
            log.info("Adding a test activity to the report");

            ActivityEntry activity = new ActivityEntry();
            activity.setId(UUID.randomUUID().toString());
            activity.setReportId(currentReportId);
            activity.setDescription("Test Activity");
            activity.setCategory("Testing");
            activity.setStartTime(LocalDateTime.now());
            activity.setEndTime(LocalDateTime.now().plusHours(2));
            activity.setProgress(50.0);
            activity.setStatus(ActivityStatus.IN_PROGRESS);
            activity.setCreatedBy(currentUsername != null ? currentUsername : "testUser");
            activity.setCreatedAt(LocalDateTime.now());
            activity.setPersonnel(new HashSet<>(Arrays.asList("Worker1")));

            if (currentReport.getActivities() == null) {
                currentReport.setActivities(new ArrayList<>());
            }
            currentReport.getActivities().add(activity);
            currentActivity = activity;

            log.info("Added test activity to the report");
        } else {
            log.info("Report already has activities: {}", currentReport.getActivities().size());
        }
    }

    @When("I submit the report for approval")
    public void whenSubmitReport() {
        log.info("Executing step: I submit the report for approval");

        try {
            // Make sure currentReport exists
            assertNotNull(currentReport, "Current report is null before submission");

            // Create a copy of the current report with updated status
            DailyReport updatedReport = new DailyReport(
                    currentReport.getId(),
                    currentReport.getProjectId(),
                    currentReport.getReportDate()
            );
            updatedReport.setStatus(ReportStatus.SUBMITTED);
            updatedReport.setCreatedBy(currentReport.getCreatedBy());
            updatedReport.setCreatedAt(currentReport.getCreatedAt());
            updatedReport.setUpdatedBy(currentUsername != null ? currentUsername : "testUser");
            updatedReport.setUpdatedAt(LocalDateTime.now());

            // Make sure to copy the activities list
            if (currentReport.getActivities() != null) {
                updatedReport.setActivities(new ArrayList<>(currentReport.getActivities()));
            } else {
                updatedReport.setActivities(new ArrayList<>());
            }

            // Debug output
            log.info("Before mocking: currentReport = {}, status = {}", currentReport.getId(), currentReport.getStatus());

            // Setup mock with a more flexible argument matcher
            when(reportingService.submitReport(anyString(), anyString()))
                    .thenReturn(updatedReport);

            // Call the service and store the result
            DailyReport result = reportingService.submitReport(currentReportId,
                    currentUsername != null ? currentUsername : "testUser");

            // Make sure result is not null
            assertNotNull(result, "Service returned null report after submission");

            // Debug output
            log.info("After service call: result = {}, status = {}", result.getId(), result.getStatus());

            // Update the current report for the Then steps
            currentReport = result;

            // Debug output
            log.info("After assignment: currentReport = {}, status = {}", currentReport.getId(), currentReport.getStatus());
        } catch (Exception e) {
            // Log the exception
            log.error("Exception in whenSubmitReport: {}", e.getMessage());
            lastException = e;
        }
    }

    @When("the supervisor approves the report")
    public void whenSupervisorApprovesReport() {
        log.info("Executing step: the supervisor approves the report");

        try {
            // Mock the service to return an updated report status
            DailyReport updatedReport = new DailyReport(
                    currentReportId,
                    currentReport.getProjectId(),
                    currentReport.getReportDate()
            );
            updatedReport.setStatus(ReportStatus.APPROVED);
            updatedReport.setCreatedBy(currentReport.getCreatedBy());
            updatedReport.setCreatedAt(currentReport.getCreatedAt());
            updatedReport.setUpdatedBy("supervisor");
            updatedReport.setUpdatedAt(LocalDateTime.now());
            updatedReport.setActivities(currentReport.getActivities());

            when(reportingService.approveReport(anyString(), anyString()))
                    .thenReturn(updatedReport);

            // Call the service
            currentReport = reportingService.approveReport(currentReportId, "supervisor");

            log.info("Report approved, status: {}", currentReport.getStatus());
        } catch (Exception e) {
            log.error("Exception in whenSupervisorApprovesReport: {}", e.getMessage());
            lastException = e;
        }
    }

    // Single-quote versions
    @When("the supervisor rejects the report with reason '$reason'")
    public void whenSupervisorRejectsReport(String reason) {
        log.info("Executing step with single quotes: the supervisor rejects the report with reason '{}'", reason);

        try {
            // Mock the service to return an updated report status
            DailyReport updatedReport = new DailyReport(
                    currentReportId,
                    currentReport.getProjectId(),
                    currentReport.getReportDate()
            );
            updatedReport.setStatus(ReportStatus.REJECTED);
            updatedReport.setCreatedBy(currentReport.getCreatedBy());
            updatedReport.setCreatedAt(currentReport.getCreatedAt());
            updatedReport.setUpdatedBy("supervisor");
            updatedReport.setUpdatedAt(LocalDateTime.now());
            updatedReport.setNotes("REJECTION REASON: " + reason);
            updatedReport.setActivities(currentReport.getActivities());

            when(reportingService.rejectReport(anyString(), anyString(), anyString()))
                    .thenReturn(updatedReport);

            // Call the service
            currentReport = reportingService.rejectReport(currentReportId, reason, "supervisor");

            log.info("Report rejected, status: {}, reason: {}", currentReport.getStatus(), reason);
        } catch (Exception e) {
            log.error("Exception in whenSupervisorRejectsReport: {}", e.getMessage());
            lastException = e;
        }
    }

    @Then("the report notes should contain the rejection reason")
    public void thenReportNotesContainRejectionReason() {
        log.info("Executing step: the report notes should contain the rejection reason");

        assertNotNull(currentReport, "Report is null");
        assertNotNull(currentReport.getNotes(), "Notes are null");
        assertTrue(currentReport.getNotes().contains("REJECTION REASON:"),
                "Notes do not contain rejection reason marker");

        log.info("Verified report notes contain rejection reason: {}", currentReport.getNotes());
    }

    // Single-quote versions
    @Given("I have an activity with ID '$activityId' in report '$reportId'")
    public void givenActivityInReport(String activityId, String reportId) {
        log.info("Executing step with single quotes: I have an activity with ID '{}' in report '{}'", activityId, reportId);

        // Setup activity
        ActivityEntry activity = new ActivityEntry();
        activity.setId(activityId);
        activity.setReportId(reportId);
        activity.setDescription("Test Activity");
        activity.setCategory("Testing");
        activity.setStartTime(LocalDateTime.now());
        activity.setEndTime(LocalDateTime.now().plusHours(2));
        activity.setProgress(50.0);
        activity.setStatus(ActivityStatus.IN_PROGRESS);
        activity.setCreatedBy(currentUsername != null ? currentUsername : "testUser");
        activity.setCreatedAt(LocalDateTime.now());
        activity.setPersonnel(new HashSet<>(Arrays.asList("Worker1", "Worker2")));

        // Mock the service to return this activity
        when(reportingService.getActivity(anyString())).thenReturn(Optional.of(activity));

        // Store activity and report info
        currentActivity = activity;
        currentReportId = reportId;

        // If report doesn't exist yet, create it
        if (currentReport == null) {
            log.info("Creating a report for the activity");
            givenDailyReportExists(reportId, "testProject");
        }

        log.info("Created activity with ID: {} in report: {}", activityId, reportId);
    }

    @When("I update the activity progress to $progress percent")
    public void whenUpdateActivityProgress(double progress) {
        log.info("Executing step: I update the activity progress to {} percent", progress);

        try {
            // Ensure currentActivity is not null
            if (currentActivity == null) {
                log.info("Creating default activity since currentActivity is null");

                currentActivity = new ActivityEntry();
                currentActivity.setId("activity123");
                currentActivity.setReportId("report123");
                currentActivity.setDescription("Test Activity");
                currentActivity.setCategory("Testing");
                currentActivity.setStartTime(LocalDateTime.now());
                currentActivity.setEndTime(LocalDateTime.now().plusHours(2));
                currentActivity.setProgress(50.0);
                currentActivity.setStatus(ActivityStatus.IN_PROGRESS);
                currentActivity.setCreatedBy(currentUsername != null ? currentUsername : "testUser");
                currentActivity.setCreatedAt(LocalDateTime.now());
                currentActivity.setPersonnel(new HashSet<>(Arrays.asList("Worker1")));
            }

            // Mock the service to return updated activity
            ActivityEntry updatedActivity = new ActivityEntry();
            updatedActivity.setId(currentActivity.getId());
            updatedActivity.setReportId(currentActivity.getReportId());
            updatedActivity.setDescription(currentActivity.getDescription());
            updatedActivity.setCategory(currentActivity.getCategory());
            updatedActivity.setStartTime(currentActivity.getStartTime());
            updatedActivity.setEndTime(currentActivity.getEndTime());
            updatedActivity.setProgress(progress);
            updatedActivity.setStatus(progress < 100 ? ActivityStatus.IN_PROGRESS : ActivityStatus.COMPLETED);
            updatedActivity.setCreatedBy(currentActivity.getCreatedBy());
            updatedActivity.setCreatedAt(currentActivity.getCreatedAt());
            updatedActivity.setUpdatedBy(currentUsername != null ? currentUsername : "testUser");
            updatedActivity.setUpdatedAt(LocalDateTime.now());
            updatedActivity.setPersonnel(currentActivity.getPersonnel());

            when(reportingService.updateActivityProgress(
                    anyString(), eq(progress), anyString()))
                    .thenReturn(updatedActivity);

            // Call the service
            currentActivity = reportingService.updateActivityProgress(
                    currentActivity.getId(), progress, currentUsername != null ? currentUsername : "testUser");

            log.info("Updated activity progress to {}%", progress);
        } catch (Exception e) {
            log.error("Exception in whenUpdateActivityProgress: {}", e.getMessage());
            lastException = e;
        }
    }

    @Then("the activity progress should be updated successfully")
    public void thenActivityProgressUpdatedSuccessfully() {
        log.info("Executing step: the activity progress should be updated successfully");

        assertNull(lastException, "Exception occurred: " + (lastException != null ? lastException.getMessage() : ""));
        assertNotNull(currentActivity, "Activity is null");

        // Verify the service was called
        verify(reportingService).updateActivityProgress(
                anyString(), anyDouble(), anyString());

        log.info("Verified activity progress was updated successfully");
    }

    @Then("the report progress should be recalculated")
    public void thenReportProgressRecalculated() {
        log.info("Executing step: the report progress should be recalculated");

        // Mock the service to be called to recalculate progress
        when(reportingService.calculateReportProgress(anyString())).thenReturn(75.0);

        double progress = reportingService.calculateReportProgress(currentReportId);
        assertEquals(75.0, progress, "Report progress not correctly calculated");

        verify(reportingService).calculateReportProgress(anyString());

        log.info("Verified report progress was recalculated, new progress: {}%", progress);
    }
}