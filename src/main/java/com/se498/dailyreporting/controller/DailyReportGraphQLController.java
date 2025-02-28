package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.dto.ActivityEntryRequest;
import com.se498.dailyreporting.dto.ActivityEntryResponse;
import com.se498.dailyreporting.dto.DailyReportRequest;
import com.se498.dailyreporting.service.DailyReportingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DailyReportGraphQLController {

    private final DailyReportingServiceImpl reportingService;

    // QUERY METHODS

    @QueryMapping
    public DailyReport report(@Argument String id) {
        log.info("GraphQL: Fetching report with id: {}", id);
        return reportingService.getReport(id).orElse(null);
    }

    @QueryMapping
    public List<DailyReport> reportsByProject(
            @Argument String projectId,
            @Argument LocalDate startDate,
            @Argument LocalDate endDate) {

        log.info("GraphQL: Fetching reports for project: {}, startDate: {}, endDate: {}",
                projectId, startDate, endDate);

        if (startDate != null && endDate != null) {
            return reportingService.getReportsByDateRange(projectId, startDate, endDate);
        } else {
            return reportingService.getReportsByProject(projectId);
        }
    }

    @QueryMapping
    public List<DailyReport> reportsByStatus(@Argument ReportStatus status) {
        log.info("GraphQL: Fetching reports with status: {}", status);
        return reportingService.getReportsByStatus(status);
    }

    @QueryMapping
    public ActivityEntry activity(@Argument String id) {
        log.info("GraphQL: Fetching activity with id: {}", id);
        return reportingService.getActivity(id).orElse(null);
    }

    @QueryMapping
    public List<ActivityEntry> activitiesByReport(@Argument String reportId) {
        log.info("GraphQL: Fetching activities for report: {}", reportId);
        return reportingService.getActivitiesByReport(reportId);
    }

    @QueryMapping
    public Double reportProgress(@Argument String reportId) {
        log.info("GraphQL: Calculating progress for report: {}", reportId);
        return reportingService.calculateReportProgress(reportId);
    }

    @QueryMapping
    public Boolean isReportComplete(@Argument String reportId) {
        log.info("GraphQL: Checking if report is complete: {}", reportId);
        return reportingService.isReportComplete(reportId);
    }

    @QueryMapping
    public Long reportDuration(@Argument String reportId) {
        log.info("GraphQL: Calculating duration for report: {}", reportId);
        return reportingService.getTotalActivityDurationMinutes(reportId);
    }

    // Field resolvers - these resolve fields within the parent types

    @SchemaMapping(typeName = "ActivityEntry", field = "durationMinutes")
    public long getActivityDurationMinutes(ActivityEntry activity) {
        return activity.calculateDuration().toMinutes();
    }

    // MUTATION METHODS

    @MutationMapping
    public DailyReport createReport(
            @Argument DailyReportRequest input,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GraphQL: Creating daily report for project {}, date {}",
                input.getProjectId(), input.getReportDate());

        DailyReport report = reportingService.createReport(
                input.getProjectId(),
                input.getReportDate(),
                userDetails.getUsername()
        );

        // Update notes if provided
        if (input.getNotes() != null && !input.getNotes().isEmpty()) {
            report = reportingService.updateReport(
                    report.getId(),
                    input.getNotes(),
                    userDetails.getUsername()
            );
        }

        return report;
    }

    @MutationMapping
    public DailyReport updateReport(
            @Argument String id,
            @Argument DailyReportRequest input,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GraphQL: Updating daily report: {}", id);

        return reportingService.updateReport(
                id,
                input.getNotes(),
                userDetails.getUsername()
        );
    }

    @MutationMapping
    public DailyReport submitReport(
            @Argument String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GraphQL: Submitting report: {}", id);

        return reportingService.submitReport(
                id,
                userDetails.getUsername()
        );
    }

    @MutationMapping
    public DailyReport approveReport(
            @Argument String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GraphQL: Approving report: {}", id);

        return reportingService.approveReport(
                id,
                userDetails.getUsername()
        );
    }

    @MutationMapping
    public DailyReport rejectReport(
            @Argument String id,
            @Argument String reason,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GraphQL: Rejecting report: {} with reason: {}", id, reason);

        return reportingService.rejectReport(
                id,
                reason,
                userDetails.getUsername()
        );
    }

    @MutationMapping
    public Boolean deleteReport(@Argument String id) {
        log.info("GraphQL: Deleting report: {}", id);
        reportingService.deleteReport(id);
        return true;
    }

    @MutationMapping
    public ActivityEntry addActivity(
            @Argument String reportId,
            @Argument ActivityEntryRequest input,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GraphQL: Adding activity to report: {}", reportId);

        // Create activity from input
        ActivityEntry activity = new ActivityEntry();
        activity.setId(UUID.randomUUID().toString());
        activity.setReportId(reportId);
        activity.setDescription(input.getDescription());
        activity.setCategory(input.getCategory());
        activity.setStartTime(input.getStartTime());
        activity.setEndTime(input.getEndTime());
        activity.setProgress(input.getProgress());
        activity.setStatus(input.getStatus());
        activity.setNotes(input.getNotes());
        activity.setCreatedBy(userDetails.getUsername());
        activity.setCreatedAt(LocalDateTime.now());

        // Add personnel if provided
        if (input.getPersonnel() != null) {
            Set<String> personnel = new HashSet<>(input.getPersonnel());
            activity.setPersonnel(personnel);
        }

        // Add activity to report
        return reportingService.addActivityToReport(reportId, activity);
    }

    @MutationMapping
    public ActivityEntry updateActivity(
            @Argument String id,
            @Argument ActivityEntryRequest input,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GraphQL: Updating activity: {}", id);

        // Create updated activity from input
        ActivityEntry activity = new ActivityEntry();
        activity.setDescription(input.getDescription());
        activity.setCategory(input.getCategory());
        activity.setStartTime(input.getStartTime());
        activity.setEndTime(input.getEndTime());
        activity.setProgress(input.getProgress());
        activity.setStatus(input.getStatus());
        activity.setNotes(input.getNotes());

        // Add personnel if provided
        if (input.getPersonnel() != null) {
            Set<String> personnel = new HashSet<>(input.getPersonnel());
            activity.setPersonnel(personnel);
        }

        // Update activity
        return reportingService.updateActivity(id, activity);
    }

    @MutationMapping
    public ActivityEntry updateActivityProgress(
            @Argument String id,
            @Argument ActivityEntryResponse input,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GraphQL: Updating progress for activity: {} to {}%", id, input.getProgress());

        return reportingService.updateActivityProgress(
                id,
                input.getProgress(),
                userDetails.getUsername()
        );
    }

    @MutationMapping
    public Boolean deleteActivity(@Argument String id) {
        log.info("GraphQL: Deleting activity: {}", id);
        reportingService.deleteActivity(id);
        return true;
    }
}