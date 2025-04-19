package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import com.se498.dailyreporting.soap.adapter.LocalDateAdapter;
import com.se498.dailyreporting.soap.adapter.LocalDateTimeAdapter;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SOAP Web Service for Daily Report operations
 */
@Slf4j
@Component
@WebService(serviceName = "DailyReportService",
        targetNamespace = "http://se498.com/dailyreporting/soap")
@RequiredArgsConstructor
public class DailyReportSoapController {

    @Autowired
    private final DailyReportingService reportingService;

    // === REPORT OPERATIONS ===

    @WebMethod(operationName = "createReport")
    @WebResult(name = "reportResponse")
    @Transactional
    public DailyReportSoapResponse createReport(
            @WebParam(name = "createReportRequest") CreateReportRequest request) {

        log.info("SOAP: Creating daily report for project {}, date {}",
                request.getProjectId(), request.getReportDate());

        DailyReport report = reportingService.createReport(
                request.getProjectId(),
                request.getReportDate(),
                request.getUsername()
        );

        // Update notes if provided
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            report = reportingService.updateReport(
                    report.getId(),
                    request.getNotes(),
                    request.getUsername()
            );
        }

        return mapToReportResponse(report);
    }

    @WebMethod(operationName = "getReport")
    @WebResult(name = "reportResponse")
    @Transactional(readOnly = true)
    public DailyReportSoapResponse getReport(
            @WebParam(name = "reportId") String reportId) {

        log.info("SOAP: Fetching report with id: {}", reportId);

        return reportingService.getReport(reportId)
                .map(this::mapToReportResponse)
                .orElse(null);
    }

    @WebMethod(operationName = "updateReport")
    @WebResult(name = "reportResponse")
    public DailyReportSoapResponse updateReport(
            @WebParam(name = "updateReportRequest") UpdateReportRequest request) {

        log.info("SOAP: Updating report: {}", request.getReportId());

        DailyReport report = reportingService.updateReport(
                request.getReportId(),
                request.getNotes(),
                request.getUsername()
        );

        return mapToReportResponse(report);
    }

    @WebMethod(operationName = "submitReport")
    @WebResult(name = "reportResponse")
    public DailyReportSoapResponse submitReport(
            @WebParam(name = "submitReportRequest") SubmitReportRequest request) {

        log.info("SOAP: Submitting report: {}", request.getReportId());

        DailyReport report = reportingService.submitReport(
                request.getReportId(),
                request.getUsername()
        );

        return mapToReportResponse(report);
    }

    @WebMethod(operationName = "approveReport")
    @WebResult(name = "reportResponse")
    public DailyReportSoapResponse approveReport(
            @WebParam(name = "approveReportRequest") ApproveReportRequest request) {

        log.info("SOAP: Approving report: {}", request.getReportId());

        DailyReport report = reportingService.approveReport(
                request.getReportId(),
                request.getUsername()
        );

        return mapToReportResponse(report);
    }

    @WebMethod(operationName = "rejectReport")
    @WebResult(name = "reportResponse")
    public DailyReportSoapResponse rejectReport(
            @WebParam(name = "rejectReportRequest") RejectReportRequest request) {

        log.info("SOAP: Rejecting report: {} with reason: {}",
                request.getReportId(), request.getReason());

        DailyReport report = reportingService.rejectReport(
                request.getReportId(),
                request.getReason(),
                request.getUsername()
        );

        return mapToReportResponse(report);
    }

    @WebMethod(operationName = "deleteReport")
    @WebResult(name = "success")
    public boolean deleteReport(
            @WebParam(name = "reportId") String reportId) {

        log.info("SOAP: Deleting report: {}", reportId);

        reportingService.deleteReport(reportId);
        return true;
    }

    @WebMethod(operationName = "getReportsByProject")
    @WebResult(name = "reportResponses")
    public List<DailyReportSoapResponse> getReportsByProject(
            @WebParam(name = "getReportsByProjectRequest") GetReportsByProjectRequest request) {

        log.info("SOAP: Fetching reports for project: {}, startDate: {}, endDate: {}",
                request.getProjectId(), request.getStartDate(), request.getEndDate());

        List<DailyReport> reports;

        if (request.getStartDate() != null && request.getEndDate() != null) {
            reports = reportingService.getReportsByDateRange(
                    request.getProjectId(),
                    request.getStartDate(),
                    request.getEndDate()
            );
        } else {
            reports = reportingService.getReportsByProject(request.getProjectId());
        }

        return reports.stream()
                .map(this::mapToReportResponse)
                .collect(Collectors.toList());
    }

    @WebMethod(operationName = "getReportsByStatus")
    @WebResult(name = "reportResponses")
    public List<DailyReportSoapResponse> getReportsByStatus(
            @WebParam(name = "status") String status) {

        log.info("SOAP: Fetching reports with status: {}", status);

        ReportStatus reportStatus;
        try {
            reportStatus = ReportStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid report status: {}", status);
            throw new IllegalArgumentException("Invalid report status: " + status);
        }

        List<DailyReport> reports = reportingService.getReportsByStatus(reportStatus);

        return reports.stream()
                .map(this::mapToReportResponse)
                .collect(Collectors.toList());
    }

    // === ACTIVITY OPERATIONS ===

    @WebMethod(operationName = "addActivity")
    @WebResult(name = "activityResponse")
    public ActivityEntrySoapResponse addActivity(
            @WebParam(name = "addActivityRequest") AddActivityRequest request) {

        log.info("SOAP: Adding activity to report: {}", request.getReportId());

        // Create activity from request
        ActivityEntry activity = new ActivityEntry();
        activity.setId(UUID.randomUUID().toString());
        activity.setReportId(request.getReportId());
        activity.setDescription(request.getDescription());
        activity.setCategory(request.getCategory());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setProgress(request.getProgress());

        try {
            ActivityStatus status = ActivityStatus.valueOf(request.getStatus().toUpperCase());
            activity.setStatus(status);
        } catch (IllegalArgumentException e) {
            log.error("Invalid activity status: {}", request.getStatus());
            throw new IllegalArgumentException("Invalid activity status: " + request.getStatus());
        }

        activity.setNotes(request.getNotes());
        activity.setCreatedBy(request.getUsername());
        activity.setCreatedAt(LocalDateTime.now());

        // Add personnel if provided
        if (request.getPersonnel() != null && !request.getPersonnel().isEmpty()) {
            Set<String> personnel = new HashSet<>(request.getPersonnel());
            activity.setPersonnel(personnel);
        }

        // Add activity to report
        ActivityEntry savedActivity = reportingService.addActivityToReport(request.getReportId(), activity);

        return mapToActivityResponse(savedActivity);
    }

    @WebMethod(operationName = "getActivity")
    @WebResult(name = "activityResponse")
    public ActivityEntrySoapResponse getActivity(
            @WebParam(name = "activityId") String activityId) {

        log.info("SOAP: Fetching activity with id: {}", activityId);

        return reportingService.getActivity(activityId)
                .map(this::mapToActivityResponse)
                .orElse(null);
    }

    @WebMethod(operationName = "getActivitiesByReport")
    @WebResult(name = "activityResponses")
    public List<ActivityEntrySoapResponse> getActivitiesByReport(
            @WebParam(name = "reportId") String reportId) {

        log.info("SOAP: Fetching activities for report: {}", reportId);

        List<ActivityEntry> activities = reportingService.getActivitiesByReport(reportId);

        return activities.stream()
                .map(this::mapToActivityResponse)
                .collect(Collectors.toList());
    }

    @WebMethod(operationName = "updateActivity")
    @WebResult(name = "activityResponse")
    public ActivityEntrySoapResponse updateActivity(
            @WebParam(name = "updateActivityRequest") UpdateActivityRequest request) {

        log.info("SOAP: Updating activity: {}", request.getActivityId());

        // Create updated activity from request
        ActivityEntry activity = new ActivityEntry();
        activity.setDescription(request.getDescription());
        activity.setCategory(request.getCategory());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setProgress(request.getProgress());

        try {
            ActivityStatus status = ActivityStatus.valueOf(request.getStatus().toUpperCase());
            activity.setStatus(status);
        } catch (IllegalArgumentException e) {
            log.error("Invalid activity status: {}", request.getStatus());
            throw new IllegalArgumentException("Invalid activity status: " + request.getStatus());
        }

        activity.setNotes(request.getNotes());

        // Add personnel if provided
        if (request.getPersonnel() != null && !request.getPersonnel().isEmpty()) {
            Set<String> personnel = new HashSet<>(request.getPersonnel());
            activity.setPersonnel(personnel);
        }

        // Update activity
        ActivityEntry updatedActivity = reportingService.updateActivity(request.getActivityId(), activity);

        return mapToActivityResponse(updatedActivity);
    }

    @WebMethod(operationName = "updateActivityProgress")
    @WebResult(name = "activityResponse")
    public ActivityEntrySoapResponse updateActivityProgress(
            @WebParam(name = "updateActivityProgressRequest") UpdateActivityProgressRequest request) {

        log.info("SOAP: Updating progress for activity: {} to {}%",
                request.getActivityId(), request.getProgress());

        ActivityEntry updatedActivity = reportingService.updateActivityProgress(
                request.getActivityId(),
                request.getProgress(),
                request.getUsername()
        );

        return mapToActivityResponse(updatedActivity);
    }

    @WebMethod(operationName = "deleteActivity")
    @WebResult(name = "success")
    public boolean deleteActivity(
            @WebParam(name = "activityId") String activityId) {

        log.info("SOAP: Deleting activity: {}", activityId);

        reportingService.deleteActivity(activityId);
        return true;
    }

    @WebMethod(operationName = "getReportProgress")
    @WebResult(name = "progress")
    public double getReportProgress(
            @WebParam(name = "reportId") String reportId) {

        log.info("SOAP: Calculating progress for report: {}", reportId);

        return reportingService.calculateReportProgress(reportId);
    }

    @WebMethod(operationName = "isReportComplete")
    @WebResult(name = "isComplete")
    public boolean isReportComplete(
            @WebParam(name = "reportId") String reportId) {

        log.info("SOAP: Checking if report is complete: {}", reportId);

        return reportingService.isReportComplete(reportId);
    }

    @WebMethod(operationName = "getTotalDuration")
    @WebResult(name = "durationMinutes")
    public long getTotalDuration(
            @WebParam(name = "reportId") String reportId) {

        log.info("SOAP: Calculating total duration for report: {}", reportId);

        return reportingService.getTotalActivityDurationMinutes(reportId);
    }

    // === HELPER METHODS ===

    private DailyReportSoapResponse mapToReportResponse(DailyReport report) {
        if (report == null) {
            return null;
        }

        DailyReportSoapResponse response = new DailyReportSoapResponse();
        response.setId(report.getId());
        response.setProjectId(report.getProjectId());
        response.setReportDate(report.getReportDate());
        response.setStatus(report.getStatus() != null ? report.getStatus().name() : null);
        response.setNotes(report.getNotes());
        response.setCreatedAt(report.getCreatedAt());
        response.setCreatedBy(report.getCreatedBy());
        response.setUpdatedAt(report.getUpdatedAt());
        response.setUpdatedBy(report.getUpdatedBy());

        if (report.getActivities() != null && !report.getActivities().isEmpty()) {
            List<ActivityEntrySoapResponse> activities = report.getActivities().stream()
                    .map(this::mapToActivityResponse)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            response.setActivities(activities);
        } else {
            response.setActivities(new ArrayList<>());
        }

        response.setProgress(report.calculateProgress());
        response.setComplete(report.isComplete());

        return response;
    }

    private ActivityEntrySoapResponse mapToActivityResponse(ActivityEntry activity) {
        if (activity == null) {
            return null;
        }

        ActivityEntrySoapResponse response = new ActivityEntrySoapResponse();
        response.setId(activity.getId());
        response.setReportId(activity.getReportId());
        response.setDescription(activity.getDescription());
        response.setCategory(activity.getCategory());
        response.setStartTime(activity.getStartTime());
        response.setEndTime(activity.getEndTime());
        response.setProgress(activity.getProgress());
        response.setStatus(activity.getStatus() != null ? activity.getStatus().name() : null);
        response.setNotes(activity.getNotes());

        if (activity.getPersonnel() != null && !activity.getPersonnel().isEmpty()) {
            response.setPersonnel(new ArrayList<>(activity.getPersonnel()));
        } else {
            response.setPersonnel(new ArrayList<>());
        }

        response.setCreatedAt(activity.getCreatedAt());
        response.setCreatedBy(activity.getCreatedBy());
        response.setUpdatedAt(activity.getUpdatedAt());

        // Calculate duration
        long durationMinutes = activity.calculateDuration() != null ?
                activity.calculateDuration().toMinutes() : 0;
        response.setDurationMinutes(durationMinutes);

        return response;
    }

    // === SOAP REQUEST/RESPONSE CLASSES ===

    @Getter
    @XmlRootElement(name = "dailyReportResponse")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DailyReportSoapResponse {
        // Getters and setters
        @Setter
        private String id;
        @Setter
        private String projectId;

        @Setter
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate reportDate;

        @Setter
        private String status;
        @Setter
        private String notes;

        @XmlElementWrapper(name = "activities")
        @XmlElement(name = "activity")
        private List<ActivityEntrySoapResponse> activities = new ArrayList<>();

        @Setter
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime createdAt;

        @Setter
        private String createdBy;

        @Setter
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime updatedAt;

        @Setter
        private String updatedBy;
        @Setter
        private double progress;
        @Setter
        private boolean complete;

        // Default constructor needed for JAXB
        public DailyReportSoapResponse() {
        }

        public void setActivities(List<ActivityEntrySoapResponse> activities) {
            this.activities = activities != null ? activities : new ArrayList<>();
        }

    }

    @Getter
    @XmlRootElement(name = "activityEntryResponse")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ActivityEntrySoapResponse {
        // Getters and setters
        @Setter
        private String id;
        @Setter
        private String reportId;
        @Setter
        private String description;
        @Setter
        private String category;

        @Setter
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime startTime;

        @Setter
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime endTime;

        @Setter
        private double progress;
        @Setter
        private String status;
        @Setter
        private String notes;

        @XmlElementWrapper(name = "personnel")
        @XmlElement(name = "person")
        private List<String> personnel = new ArrayList<>();

        @Setter
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime createdAt;

        @Setter
        private String createdBy;

        @Setter
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime updatedAt;

        @Setter
        private long durationMinutes;

        // Default constructor needed for JAXB
        public ActivityEntrySoapResponse() {
        }

        public void setPersonnel(List<String> personnel) {
            this.personnel = personnel != null ? personnel : new ArrayList<>();
        }

    }

    @Setter
    @Getter
    @XmlRootElement(name = "createReportRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CreateReportRequest {
        // Getters and setters
        private String projectId;

        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate reportDate;

        private String notes;
        private String username;

        // Default constructor needed for JAXB
        public CreateReportRequest() {
        }

    }

    @Setter
    @Getter
    @XmlRootElement(name = "updateReportRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UpdateReportRequest {
        // Getters and setters
        private String reportId;
        private String notes;
        private String username;

        // Default constructor needed for JAXB
        public UpdateReportRequest() {
        }

    }

    @Setter
    @Getter
    @XmlRootElement(name = "submitReportRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SubmitReportRequest {
        // Getters and setters
        private String reportId;
        private String username;

        // Default constructor needed for JAXB
        public SubmitReportRequest() {
        }

    }

    @Setter
    @Getter
    @XmlRootElement(name = "approveReportRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ApproveReportRequest {
        // Getters and setters
        private String reportId;
        private String username;

        // Default constructor needed for JAXB
        public ApproveReportRequest() {
        }

    }

    @Setter
    @Getter
    @XmlRootElement(name = "rejectReportRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RejectReportRequest {
        // Getters and setters
        private String reportId;
        private String reason;
        private String username;

        // Default constructor needed for JAXB
        public RejectReportRequest() {
        }

    }

    @Setter
    @Getter
    @XmlRootElement(name = "getReportsByProjectRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GetReportsByProjectRequest {
        // Getters and setters
        private String projectId;

        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate startDate;

        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate endDate;

        // Default constructor needed for JAXB
        public GetReportsByProjectRequest() {
        }

    }

    @Getter
    @XmlRootElement(name = "addActivityRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AddActivityRequest {
        // Getters and setters
        @Setter
        private String reportId;
        @Setter
        private String description;
        @Setter
        private String category;

        @Setter
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime startTime;

        @Setter
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime endTime;

        @Setter
        private double progress;
        @Setter
        private String status;
        @Setter
        private String notes;

        @XmlElementWrapper(name = "personnel")
        @XmlElement(name = "person")
        private List<String> personnel = new ArrayList<>();

        @Setter
        private String username;

        // Default constructor needed for JAXB
        public AddActivityRequest() {
        }

        public void setPersonnel(List<String> personnel) {
            this.personnel = personnel != null ? personnel : new ArrayList<>();
        }

    }

    @Getter
    @XmlRootElement(name = "updateActivityRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UpdateActivityRequest {
        // Getters and setters
        @Setter
        private String activityId;
        @Setter
        private String description;
        @Setter
        private String category;

        @Setter
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime startTime;

        @Setter
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime endTime;

        @Setter
        private double progress;
        @Setter
        private String status;
        @Setter
        private String notes;

        @XmlElementWrapper(name = "personnel")
        @XmlElement(name = "person")
        private List<String> personnel = new ArrayList<>();

        // Default constructor needed for JAXB
        public UpdateActivityRequest() {
        }

        public void setPersonnel(List<String> personnel) {
            this.personnel = personnel != null ? personnel : new ArrayList<>();
        }
    }

    @Getter
    @XmlRootElement(name = "updateActivityProgressRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UpdateActivityProgressRequest {
        // Getters and setters
        private String activityId;
        private double progress;
        private String username;

        // Default constructor needed for JAXB
        public UpdateActivityProgressRequest() {
        }

        public void setActivityId(String activityId) { this.activityId = activityId; }

        public void setProgress(double progress) { this.progress = progress; }

        public void setUsername(String username) { this.username = username; }
    }
}