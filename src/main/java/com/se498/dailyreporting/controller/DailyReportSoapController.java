package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import com.se498.dailyreporting.dto.soap.*;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SOAP Web Service for Daily Reports
 * Provides SOAP endpoints for all daily reporting operations
 */
@Slf4j
@Component
@WebService(
        serviceName = "DailyReportService",
        portName = "DailyReportPort",
        targetNamespace = "http://reporting.construction.com/soap",
        name = "DailyReport"
)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@RequiredArgsConstructor
public class DailyReportSoapController {

    @Autowired
    private final DailyReportingService reportingService;

    /**
     * Create a new daily report
     */
    @WebMethod(operationName = "createReport")
    @WebResult(name = "reportResponse")
    @RequestWrapper(localName = "createReportRequest", targetNamespace = "http://reporting.construction.com/soap")
    @ResponseWrapper(localName = "createReportResponse", targetNamespace = "http://reporting.construction.com/soap")
    public DailyReportSoapResponse createReport(
            @WebParam(name = "projectId") String projectId,
            @WebParam(name = "reportDate") String reportDate,
            @WebParam(name = "notes") String notes,
            @WebParam(name = "username") String username) {

        log.info("SOAP: Creating daily report for project {}, date {}", projectId, reportDate);

        try {
            LocalDate date = LocalDate.parse(reportDate);
            DailyReport report = reportingService.createReport(projectId, date, username);

            // Update notes if provided
            if (notes != null && !notes.isEmpty()) {
                report = reportingService.updateReport(report.getId(), notes, username);
            }

            return mapToSoapResponse(report);
        } catch (Exception e) {
            log.error("Error creating report via SOAP", e);
            return createErrorResponse("Failed to create report: " + e.getMessage());
        }
    }

    /**
     * Get a report by ID
     */
    @WebMethod(operationName = "getReport")
    @WebResult(name = "reportResponse")
    public DailyReportSoapResponse getReport(@WebParam(name = "reportId") String reportId) {
        log.info("SOAP: Getting report with ID: {}", reportId);

        try {
            return reportingService.getReport(reportId)
                    .map(this::mapToSoapResponse)
                    .orElse(createErrorResponse("Report not found: " + reportId));
        } catch (Exception e) {
            log.error("Error getting report via SOAP", e);
            return createErrorResponse("Failed to get report: " + e.getMessage());
        }
    }

    /**
     * Update a report
     */
    @WebMethod(operationName = "updateReport")
    @WebResult(name = "reportResponse")
    public DailyReportSoapResponse updateReport(
            @WebParam(name = "reportId") String reportId,
            @WebParam(name = "notes") String notes,
            @WebParam(name = "username") String username) {

        log.info("SOAP: Updating report {}", reportId);

        try {
            DailyReport report = reportingService.updateReport(reportId, notes, username);
            return mapToSoapResponse(report);
        } catch (Exception e) {
            log.error("Error updating report via SOAP", e);
            return createErrorResponse("Failed to update report: " + e.getMessage());
        }
    }

    /**
     * Submit a report for approval
     */
    @WebMethod(operationName = "submitReport")
    @WebResult(name = "reportResponse")
    public DailyReportSoapResponse submitReport(
            @WebParam(name = "reportId") String reportId,
            @WebParam(name = "username") String username) {

        log.info("SOAP: Submitting report {}", reportId);

        try {
            DailyReport report = reportingService.submitReport(reportId, username);
            return mapToSoapResponse(report);
        } catch (Exception e) {
            log.error("Error submitting report via SOAP", e);
            return createErrorResponse("Failed to submit report: " + e.getMessage());
        }
    }

    /**
     * Approve a report
     */
    @WebMethod(operationName = "approveReport")
    @WebResult(name = "reportResponse")
    public DailyReportSoapResponse approveReport(
            @WebParam(name = "reportId") String reportId,
            @WebParam(name = "username") String username) {

        log.info("SOAP: Approving report {}", reportId);

        try {
            DailyReport report = reportingService.approveReport(reportId, username);
            return mapToSoapResponse(report);
        } catch (Exception e) {
            log.error("Error approving report via SOAP", e);
            return createErrorResponse("Failed to approve report: " + e.getMessage());
        }
    }

    /**
     * Reject a report
     */
    @WebMethod(operationName = "rejectReport")
    @WebResult(name = "reportResponse")
    public DailyReportSoapResponse rejectReport(
            @WebParam(name = "reportId") String reportId,
            @WebParam(name = "reason") String reason,
            @WebParam(name = "username") String username) {

        log.info("SOAP: Rejecting report {} with reason: {}", reportId, reason);

        try {
            DailyReport report = reportingService.rejectReport(reportId, reason, username);
            return mapToSoapResponse(report);
        } catch (Exception e) {
            log.error("Error rejecting report via SOAP", e);
            return createErrorResponse("Failed to reject report: " + e.getMessage());
        }
    }

    /**
     * Delete a report
     */
    @WebMethod(operationName = "deleteReport")
    @WebResult(name = "serviceResponse")
    public ServiceResponse deleteReport(@WebParam(name = "reportId") String reportId) {
        log.info("SOAP: Deleting report {}", reportId);

        try {
            reportingService.deleteReport(reportId);
            return new ServiceResponse(true, "Report deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting report via SOAP", e);
            return new ServiceResponse(false, "Failed to delete report: " + e.getMessage());
        }
    }

    /**
     * Get reports by project
     */
    @WebMethod(operationName = "getReportsByProject")
    @WebResult(name = "reportListResponse")
    public ReportListResponse getReportsByProject(@WebParam(name = "projectId") String projectId) {
        log.info("SOAP: Getting reports for project {}", projectId);

        try {
            List<DailyReport> reports = reportingService.getReportsByProject(projectId);
            return createReportListResponse(reports);
        } catch (Exception e) {
            log.error("Error getting reports by project via SOAP", e);
            return createEmptyReportListResponse("Failed to get reports: " + e.getMessage());
        }
    }

    /**
     * Get reports by date range
     */
    @WebMethod(operationName = "getReportsByDateRange")
    @WebResult(name = "reportListResponse")
    public ReportListResponse getReportsByDateRange(
            @WebParam(name = "projectId") String projectId,
            @WebParam(name = "startDate") String startDate,
            @WebParam(name = "endDate") String endDate) {

        log.info("SOAP: Getting reports for project {} between {} and {}", projectId, startDate, endDate);

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<DailyReport> reports = reportingService.getReportsByDateRange(projectId, start, end);
            return createReportListResponse(reports);
        } catch (Exception e) {
            log.error("Error getting reports by date range via SOAP", e);
            return createEmptyReportListResponse("Failed to get reports: " + e.getMessage());
        }
    }

    /**
     * Get reports by status
     */
    @WebMethod(operationName = "getReportsByStatus")
    @WebResult(name = "reportListResponse")
    public ReportListResponse getReportsByStatus(@WebParam(name = "status") String status) {
        log.info("SOAP: Getting reports with status {}", status);

        try {
            ReportStatus reportStatus = ReportStatus.valueOf(status.toUpperCase());
            List<DailyReport> reports = reportingService.getReportsByStatus(reportStatus);
            return createReportListResponse(reports);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status provided via SOAP: {}", status);
            return createEmptyReportListResponse("Invalid status: " + status);
        } catch (Exception e) {
            log.error("Error getting reports by status via SOAP", e);
            return createEmptyReportListResponse("Failed to get reports: " + e.getMessage());
        }
    }

    /**
     * Add activity to report
     */
    @WebMethod(operationName = "addActivity")
    @WebResult(name = "activityResponse")
    public ActivitySoapResponse addActivity(@WebParam(name = "activity") ActivitySoapRequest request) {
        log.info("SOAP: Adding activity to report {}", request.getReportId());

        try {
            // Create activity from request
            ActivityEntry activity = new ActivityEntry();
            activity.setId(UUID.randomUUID().toString());
            activity.setReportId(request.getReportId());
            activity.setDescription(request.getDescription());
            activity.setCategory(request.getCategory());
            activity.setStartTime(LocalDateTime.parse(request.getStartTime()));
            activity.setEndTime(LocalDateTime.parse(request.getEndTime()));
            activity.setProgress(request.getProgress());
            activity.setStatus(ActivityStatus.valueOf(request.getStatus()));
            activity.setNotes(request.getNotes());
            activity.setCreatedBy(request.getUsername());
            activity.setCreatedAt(LocalDateTime.now());

            // Add personnel if provided
            if (request.getPersonnel() != null && !request.getPersonnel().isEmpty()) {
                Set<String> personnel = new HashSet<>(Arrays.asList(request.getPersonnel().split(",")));
                activity.setPersonnel(personnel);
            }

            // Add activity to report
            ActivityEntry savedActivity = reportingService.addActivityToReport(request.getReportId(), activity);
            return mapToSoapResponse(savedActivity);
        } catch (Exception e) {
            log.error("Error adding activity via SOAP", e);
            return createActivityErrorResponse("Failed to add activity: " + e.getMessage());
        }
    }

    /**
     * Get activity by ID
     */
    @WebMethod(operationName = "getActivity")
    @WebResult(name = "activityResponse")
    public ActivitySoapResponse getActivity(@WebParam(name = "activityId") String activityId) {
        log.info("SOAP: Getting activity {}", activityId);

        try {
            return reportingService.getActivity(activityId)
                    .map(this::mapToSoapResponse)
                    .orElse(createActivityErrorResponse("Activity not found: " + activityId));
        } catch (Exception e) {
            log.error("Error getting activity via SOAP", e);
            return createActivityErrorResponse("Failed to get activity: " + e.getMessage());
        }
    }

    /**
     * Get activities by report
     */
    @WebMethod(operationName = "getActivitiesByReport")
    @WebResult(name = "activityListResponse")
    public ActivityListResponse getActivitiesByReport(@WebParam(name = "reportId") String reportId) {
        log.info("SOAP: Getting activities for report {}", reportId);

        try {
            List<ActivityEntry> activities = reportingService.getActivitiesByReport(reportId);
            return createActivityListResponse(activities);
        } catch (Exception e) {
            log.error("Error getting activities by report via SOAP", e);
            return createEmptyActivityListResponse("Failed to get activities: " + e.getMessage());
        }
    }

    /**
     * Update activity
     */
    @WebMethod(operationName = "updateActivity")
    @WebResult(name = "activityResponse")
    public ActivitySoapResponse updateActivity(@WebParam(name = "activity") ActivitySoapRequest request) {
        log.info("SOAP: Updating activity {}", request.getActivityId());

        try {
            // Create updated activity from request
            ActivityEntry activity = new ActivityEntry();
            activity.setDescription(request.getDescription());
            activity.setCategory(request.getCategory());
            activity.setStartTime(LocalDateTime.parse(request.getStartTime()));
            activity.setEndTime(LocalDateTime.parse(request.getEndTime()));
            activity.setProgress(request.getProgress());
            activity.setStatus(ActivityStatus.valueOf(request.getStatus()));
            activity.setNotes(request.getNotes());

            // Add personnel if provided
            if (request.getPersonnel() != null && !request.getPersonnel().isEmpty()) {
                Set<String> personnel = new HashSet<>(Arrays.asList(request.getPersonnel().split(",")));
                activity.setPersonnel(personnel);
            }

            // Update activity
            ActivityEntry updatedActivity = reportingService.updateActivity(request.getActivityId(), activity);
            return mapToSoapResponse(updatedActivity);
        } catch (Exception e) {
            log.error("Error updating activity via SOAP", e);
            return createActivityErrorResponse("Failed to update activity: " + e.getMessage());
        }
    }

    /**
     * Update activity progress
     */
    @WebMethod(operationName = "updateActivityProgress")
    @WebResult(name = "activityResponse")
    public ActivitySoapResponse updateActivityProgress(
            @WebParam(name = "activityId") String activityId,
            @WebParam(name = "progress") double progress,
            @WebParam(name = "username") String username) {

        log.info("SOAP: Updating progress for activity {} to {}%", activityId, progress);

        try {
            ActivityEntry updatedActivity = reportingService.updateActivityProgress(activityId, progress, username);
            return mapToSoapResponse(updatedActivity);
        } catch (Exception e) {
            log.error("Error updating activity progress via SOAP", e);
            return createActivityErrorResponse("Failed to update activity progress: " + e.getMessage());
        }
    }

    /**
     * Delete activity
     */
    @WebMethod(operationName = "deleteActivity")
    @WebResult(name = "serviceResponse")
    public ServiceResponse deleteActivity(@WebParam(name = "activityId") String activityId) {
        log.info("SOAP: Deleting activity {}", activityId);

        try {
            reportingService.deleteActivity(activityId);
            return new ServiceResponse(true, "Activity deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting activity via SOAP", e);
            return new ServiceResponse(false, "Failed to delete activity: " + e.getMessage());
        }
    }

    /**
     * Get report progress
     */
    @WebMethod(operationName = "getReportProgress")
    @WebResult(name = "progressResponse")
    public ProgressResponse getReportProgress(@WebParam(name = "reportId") String reportId) {
        log.info("SOAP: Getting progress for report {}", reportId);

        try {
            double progress = reportingService.calculateReportProgress(reportId);
            return new ProgressResponse(true, progress, "Progress calculated successfully");
        } catch (Exception e) {
            log.error("Error getting report progress via SOAP", e);
            return new ProgressResponse(false, 0.0, "Failed to get progress: " + e.getMessage());
        }
    }

    /**
     * Check if report is complete
     */
    @WebMethod(operationName = "isReportComplete")
    @WebResult(name = "completionResponse")
    public CompletionResponse isReportComplete(@WebParam(name = "reportId") String reportId) {
        log.info("SOAP: Checking if report {} is complete", reportId);

        try {
            boolean isComplete = reportingService.isReportComplete(reportId);
            return new CompletionResponse(true, isComplete, "Completion status retrieved successfully");
        } catch (Exception e) {
            log.error("Error checking report completion via SOAP", e);
            return new CompletionResponse(false, false, "Failed to check completion: " + e.getMessage());
        }
    }

    /**
     * Get total activity duration
     */
    @WebMethod(operationName = "getTotalActivityDuration")
    @WebResult(name = "durationResponse")
    public DurationResponse getTotalActivityDuration(@WebParam(name = "reportId") String reportId) {
        log.info("SOAP: Getting total duration for report {}", reportId);

        try {
            long durationMinutes = reportingService.getTotalActivityDurationMinutes(reportId);
            return new DurationResponse(true, durationMinutes, "Duration calculated successfully");
        } catch (Exception e) {
            log.error("Error getting activity duration via SOAP", e);
            return new DurationResponse(false, 0, "Failed to get duration: " + e.getMessage());
        }
    }

    // Helper methods for mapping domain objects to SOAP DTOs

    private DailyReportSoapResponse mapToSoapResponse(DailyReport report) {
        DailyReportSoapResponse response = new DailyReportSoapResponse();
        response.setSuccess(true);
        response.setId(report.getId());
        response.setProjectId(report.getProjectId());
        response.setReportDate(report.getReportDate().toString());
        response.setStatus(report.getStatus().name());
        response.setNotes(report.getNotes());
        response.setCreatedAt(report.getCreatedAt().toString());
        response.setCreatedBy(report.getCreatedBy());

        if (report.getUpdatedAt() != null) {
            response.setUpdatedAt(report.getUpdatedAt().toString());
        }

        response.setUpdatedBy(report.getUpdatedBy());
        response.setProgress(report.calculateProgress());
        response.setComplete(report.isComplete());

        return response;
    }

    private ActivitySoapResponse mapToSoapResponse(ActivityEntry activity) {
        ActivitySoapResponse response = new ActivitySoapResponse();
        response.setSuccess(true);
        response.setId(activity.getId());
        response.setReportId(activity.getReportId());
        response.setDescription(activity.getDescription());
        response.setCategory(activity.getCategory());
        response.setStartTime(activity.getStartTime().toString());
        response.setEndTime(activity.getEndTime().toString());
        response.setProgress(activity.getProgress());
        response.setStatus(activity.getStatus().name());
        response.setNotes(activity.getNotes());

        if (activity.getPersonnel() != null && !activity.getPersonnel().isEmpty()) {
            response.setPersonnel(String.join(",", activity.getPersonnel()));
        }

        response.setCreatedAt(activity.getCreatedAt().toString());
        response.setCreatedBy(activity.getCreatedBy());

        if (activity.getUpdatedAt() != null) {
            response.setUpdatedAt(activity.getUpdatedAt().toString());
        }

        response.setDurationMinutes(activity.calculateDuration().toMinutes());

        return response;
    }

    private DailyReportSoapResponse createErrorResponse(String errorMessage) {
        DailyReportSoapResponse response = new DailyReportSoapResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    private ActivitySoapResponse createActivityErrorResponse(String errorMessage) {
        ActivitySoapResponse response = new ActivitySoapResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    private ReportListResponse createReportListResponse(List<DailyReport> reports) {
        ReportListResponse response = new ReportListResponse();
        response.setSuccess(true);
        response.setReportCount(reports.size());
        response.setReports(reports.stream()
                .map(this::mapToSoapResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private ReportListResponse createEmptyReportListResponse(String errorMessage) {
        ReportListResponse response = new ReportListResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        response.setReportCount(0);
        response.setReports(Collections.emptyList());
        return response;
    }

    private ActivityListResponse createActivityListResponse(List<ActivityEntry> activities) {
        ActivityListResponse response = new ActivityListResponse();
        response.setSuccess(true);
        response.setActivityCount(activities.size());
        response.setActivities(activities.stream()
                .map(this::mapToSoapResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private ActivityListResponse createEmptyActivityListResponse(String errorMessage) {
        ActivityListResponse response = new ActivityListResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        response.setActivityCount(0);
        response.setActivities(Collections.emptyList());
        return response;
    }
}