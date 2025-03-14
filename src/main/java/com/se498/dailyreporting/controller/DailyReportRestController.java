package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.se498.dailyreporting.service.DailyReportingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Daily Reports", description = "APIs for managing construction daily reports")
public class DailyReportRestController {

    @Autowired
    private final DailyReportingService reportingService;

    @PostMapping
    public ResponseEntity<DailyReportResponse> createReport(
            @RequestBody @Valid DailyReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creating daily report for project {}, date {}",
                request.getProjectId(), request.getReportDate());

        DailyReport report = reportingService.createReport(
                request.getProjectId(),
                request.getReportDate(),
                userDetails.getUsername()
        );

        // Update notes if provided
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            report = reportingService.updateReport(
                    report.getId(),
                    request.getNotes(),
                    userDetails.getUsername()
            );
        }

        DailyReportResponse response = mapToReportResponse(report);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<DailyReportResponse> getReport(@PathVariable String reportId) {
        return reportingService.getReport(reportId)
                .map(report -> ResponseEntity.ok(mapToReportResponse(report)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<DailyReportResponse> updateReport(
            @PathVariable String reportId,
            @RequestBody DailyReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        DailyReport report = reportingService.updateReport(
                reportId,
                request.getNotes(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok(mapToReportResponse(report));
    }

    @PostMapping("/{reportId}/submit")
    public ResponseEntity<DailyReportResponse> submitReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal UserDetails userDetails) {

        DailyReport report = reportingService.submitReport(
                reportId,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(mapToReportResponse(report));
    }

    @PostMapping("/{reportId}/approve")
    public ResponseEntity<DailyReportResponse> approveReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal UserDetails userDetails) {

        DailyReport report = reportingService.approveReport(
                reportId,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(mapToReportResponse(report));
    }

    @PostMapping("/{reportId}/reject")
    public ResponseEntity<DailyReportResponse> rejectReport(
            @PathVariable String reportId,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {

        DailyReport report = reportingService.rejectReport(
                reportId,
                reason,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(mapToReportResponse(report));
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable String reportId) {
        reportingService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<DailyReportResponse>> getReportsByProject(
            @PathVariable String projectId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailyReport> reports;

        if (startDate != null && endDate != null) {
            reports = reportingService.getReportsByDateRange(projectId, startDate, endDate);
        } else {
            reports = reportingService.getReportsByProject(projectId);
        }

        List<DailyReportResponse> responseList = reports.stream()
                .map(this::mapToReportResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DailyReportResponse>> getReportsByStatus(
            @PathVariable String status) {

        ReportStatus reportStatus;
        try {
            reportStatus = ReportStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        List<DailyReport> reports = reportingService.getReportsByStatus(reportStatus);

        List<DailyReportResponse> responseList = reports.stream()
                .map(this::mapToReportResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/{reportId}/activities")
    public ResponseEntity<ActivityEntryResponse> addActivity(
            @PathVariable String reportId,
            @RequestBody @Valid ActivityEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Create activity from request
        ActivityEntry activity = new ActivityEntry();
        activity.setId(UUID.randomUUID().toString());
        activity.setReportId(reportId);
        activity.setDescription(request.getDescription());
        activity.setCategory(request.getCategory());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setProgress(request.getProgress());
        activity.setStatus(request.getStatus());
        activity.setNotes(request.getNotes());
        activity.setCreatedBy(userDetails.getUsername());
        activity.setCreatedAt(LocalDateTime.now());

        // Add personnel if provided
        if (request.getPersonnel() != null) {
            activity.setPersonnel(request.getPersonnel());
        }

        // Add activity to report
        ActivityEntry savedActivity = reportingService.addActivityToReport(reportId, activity);

        return new ResponseEntity<>(mapToActivityResponse(savedActivity), HttpStatus.CREATED);
    }

    @GetMapping("/activities/{activityId}")
    public ResponseEntity<ActivityEntryResponse> getActivity(@PathVariable String activityId) {
        return reportingService.getActivity(activityId)
                .map(activity -> ResponseEntity.ok(mapToActivityResponse(activity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{reportId}/activities")
    public ResponseEntity<List<ActivityEntryResponse>> getActivitiesByReport(
            @PathVariable String reportId) {

        List<ActivityEntry> activities = reportingService.getActivitiesByReport(reportId);

        List<ActivityEntryResponse> responseList = activities.stream()
                .map(this::mapToActivityResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/activities/{activityId}")
    public ResponseEntity<ActivityEntryResponse> updateActivity(
            @PathVariable String activityId,
            @RequestBody @Valid ActivityEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Create updated activity from request
        ActivityEntry activity = new ActivityEntry();
        activity.setDescription(request.getDescription());
        activity.setCategory(request.getCategory());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setProgress(request.getProgress());
        activity.setStatus(request.getStatus());
        activity.setNotes(request.getNotes());

        // Add personnel if provided
        if (request.getPersonnel() != null) {
            activity.setPersonnel(request.getPersonnel());
        }

        // Update activity
        ActivityEntry updatedActivity = reportingService.updateActivity(activityId, activity);

        return ResponseEntity.ok(mapToActivityResponse(updatedActivity));
    }

    @PutMapping("/activities/{activityId}/progress")
    public ResponseEntity<ActivityEntryResponse> updateActivityProgress(
            @PathVariable String activityId,
            @RequestParam double progress,
            @AuthenticationPrincipal UserDetails userDetails) {

        ActivityEntry updatedActivity = reportingService.updateActivityProgress(
                activityId, progress, userDetails.getUsername());

        return ResponseEntity.ok(mapToActivityResponse(updatedActivity));
    }

    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<Void> deleteActivity(@PathVariable String activityId) {
        reportingService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reportId}/progress")
    public ResponseEntity<Double> getReportProgress(@PathVariable String reportId) {
        double progress = reportingService.calculateReportProgress(reportId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{reportId}/complete")
    public ResponseEntity<Boolean> isReportComplete(@PathVariable String reportId) {
        boolean isComplete = reportingService.isReportComplete(reportId);
        return ResponseEntity.ok(isComplete);
    }

    @GetMapping("/{reportId}/duration")
    public ResponseEntity<Long> getTotalDuration(@PathVariable String reportId) {
        long durationMinutes = reportingService.getTotalActivityDurationMinutes(reportId);
        return ResponseEntity.ok(durationMinutes);
    }

    // Helper methods for mapping domain objects to DTOs

    private DailyReportResponse mapToReportResponse(DailyReport report) {
        DailyReportResponse response = new DailyReportResponse();
        response.setId(report.getId());
        response.setProjectId(report.getProjectId());
        response.setReportDate(report.getReportDate());
        response.setStatus(report.getStatus().name());
        response.setNotes(report.getNotes());
        response.setCreatedAt(report.getCreatedAt());
        response.setCreatedBy(report.getCreatedBy());
        response.setUpdatedAt(report.getUpdatedAt());
        response.setUpdatedBy(report.getUpdatedBy());

        if (report.getActivities() != null) {
            List<ActivityEntryResponse> activities = report.getActivities().stream()
                    .map(this::mapToActivityResponse)
                    .collect(Collectors.toList());
            response.setActivities(activities);
        }

        response.setProgress(report.calculateProgress());
        response.setComplete(report.isComplete());

        return response;
    }

    private ActivityEntryResponse mapToActivityResponse(ActivityEntry activity) {
        ActivityEntryResponse response = new ActivityEntryResponse();
        response.setId(activity.getId());
        response.setReportId(activity.getReportId());
        response.setDescription(activity.getDescription());
        response.setCategory(activity.getCategory());
        response.setStartTime(activity.getStartTime());
        response.setEndTime(activity.getEndTime());
        response.setProgress(activity.getProgress());
        response.setStatus(activity.getStatus().name());
        response.setNotes(activity.getNotes());
        response.setPersonnel(activity.getPersonnel());
        response.setCreatedAt(activity.getCreatedAt());
        response.setCreatedBy(activity.getCreatedBy());
        response.setUpdatedAt(activity.getUpdatedAt());

        // Calculate duration
        long durationMinutes = activity.calculateDuration().toMinutes();
        response.setDurationMinutes(durationMinutes);

        return response;
    }
}