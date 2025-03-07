package com.se498.dailyreporting.controller;

import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.grpc.*;
import com.se498.dailyreporting.service.DailyReportingService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * gRPC controller for daily reporting service
 * Implements the DailyReportingService gRPC service
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class DailyReportGrpcController extends DailyReportingServiceGrpc.DailyReportingServiceImplBase {

    @Autowired
    private final DailyReportingService reportingService;

    // REPORT OPERATIONS

    @Override
    public void createReport(CreateReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Creating daily report for project {}, date {}",
                request.getProjectId(), dateFromProto(request.getReportDate()));

        try {
            // Create the report
            DailyReport report = reportingService.createReport(
                    request.getProjectId(),
                    dateFromProto(request.getReportDate()),
                    request.getUsername()
            );

            // Update notes if provided
            if (request.hasNotes()) {
                report = reportingService.updateReport(
                        report.getId(),
                        request.getNotes().getValue(),
                        request.getUsername()
                );
            }

            // Build and send the response
            DailyReportResponse response = DailyReportResponse.newBuilder()
                    .setReport(toDailyReportProto(report))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error creating report: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getReport(GetReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Fetching report with id: {}", request.getReportId());

        try {
            // Get the report
            Optional<DailyReport> reportOpt = reportingService.getReport(request.getReportId());

            if (reportOpt.isPresent()) {
                // Build and send the response
                DailyReportResponse response = DailyReportResponse.newBuilder()
                        .setReport(toDailyReportProto(reportOpt.get()))
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                log.warn("Report not found: {}", request.getReportId());
                responseObserver.onError(new IllegalArgumentException("Report not found: " + request.getReportId()));
            }
        } catch (Exception e) {
            log.error("Error getting report: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateReport(UpdateReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Updating daily report: {}", request.getReportId());

        try {
            // Get notes if provided
            String notes = request.hasNotes() ? request.getNotes().getValue() : "";

            // Update the report
            DailyReport report = reportingService.updateReport(
                    request.getReportId(),
                    notes,
                    request.getUsername()
            );

            // Build and send the response
            DailyReportResponse response = DailyReportResponse.newBuilder()
                    .setReport(toDailyReportProto(report))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error updating report: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void submitReport(SubmitReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Submitting report: {}", request.getReportId());

        try {
            // Submit the report
            DailyReport report = reportingService.submitReport(
                    request.getReportId(),
                    request.getUsername()
            );

            // Build and send the response
            DailyReportResponse response = DailyReportResponse.newBuilder()
                    .setReport(toDailyReportProto(report))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error submitting report: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void approveReport(ApproveReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Approving report: {}", request.getReportId());

        try {
            // Approve the report
            DailyReport report = reportingService.approveReport(
                    request.getReportId(),
                    request.getUsername()
            );

            // Build and send the response
            DailyReportResponse response = DailyReportResponse.newBuilder()
                    .setReport(toDailyReportProto(report))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error approving report: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void rejectReport(RejectReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Rejecting report: {} with reason: {}", request.getReportId(), request.getReason());

        try {
            // Reject the report
            DailyReport report = reportingService.rejectReport(
                    request.getReportId(),
                    request.getReason(),
                    request.getUsername()
            );

            // Build and send the response
            DailyReportResponse response = DailyReportResponse.newBuilder()
                    .setReport(toDailyReportProto(report))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error rejecting report: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteReport(DeleteReportRequest request, StreamObserver<DeleteReportResponse> responseObserver) {
        log.info("gRPC: Deleting report: {}", request.getReportId());

        try {
            // Delete the report
            reportingService.deleteReport(request.getReportId());

            // Send success response
            DeleteReportResponse response = DeleteReportResponse.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error deleting report: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getReportsByProject(GetReportsByProjectRequest request,
                                    StreamObserver<GetReportsByProjectResponse> responseObserver) {
        log.info("gRPC: Fetching reports for project: {}", request.getProjectId());

        try {
            List<DailyReport> reports;

            // Get reports by project or by date range
            if (request.hasStartDate() && request.hasEndDate()) {
                reports = reportingService.getReportsByDateRange(
                        request.getProjectId(),
                        dateFromProto(request.getStartDate()),
                        dateFromProto(request.getEndDate())
                );
            } else {
                reports = reportingService.getReportsByProject(request.getProjectId());
            }

            // Build response with all reports
            GetReportsByProjectResponse.Builder responseBuilder = GetReportsByProjectResponse.newBuilder();
            reports.forEach(report -> responseBuilder.addReports(toDailyReportProto(report)));

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting reports by project: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getReportsByStatus(GetReportsByStatusRequest request,
                                   StreamObserver<GetReportsByStatusResponse> responseObserver) {
        ReportStatus status = reportStatusFromProto(request.getStatus());
        log.info("gRPC: Fetching reports with status: {}", status);

        try {
            // Get reports by status
            List<DailyReport> reports = reportingService.getReportsByStatus(status);

            // Build response with all reports
            GetReportsByStatusResponse.Builder responseBuilder = GetReportsByStatusResponse.newBuilder();
            reports.forEach(report -> responseBuilder.addReports(toDailyReportProto(report)));

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting reports by status: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    // ACTIVITY OPERATIONS

    @Override
    public void addActivity(AddActivityRequest request, StreamObserver<ActivityResponse> responseObserver) {
        log.info("gRPC: Adding activity to report: {}", request.getReportId());

        try {
            // Create activity from request
            ActivityEntry activity = new ActivityEntry();
            activity.setId(UUID.randomUUID().toString());
            activity.setReportId(request.getReportId());
            activity.setDescription(request.getDescription());
            activity.setCategory(request.getCategory());
            activity.setStartTime(timestampToLocalDateTime(request.getStartTime()));
            activity.setEndTime(timestampToLocalDateTime(request.getEndTime()));
            activity.setProgress(request.getProgress());
            activity.setStatus(activityStatusFromProto(request.getStatus()));

            if (request.hasNotes()) {
                activity.setNotes(request.getNotes().getValue());
            }

            activity.setCreatedBy(request.getUsername());
            activity.setCreatedAt(LocalDateTime.now());

            // Add personnel if provided
            if (request.getPersonnelCount() > 0) {
                Set<String> personnel = new HashSet<>(request.getPersonnelList());
                activity.setPersonnel(personnel);
            }

            // Add activity to report
            ActivityEntry savedActivity = reportingService.addActivityToReport(request.getReportId(), activity);

            // Build and send the response
            ActivityResponse response = ActivityResponse.newBuilder()
                    .setActivity(toActivityProto(savedActivity))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error adding activity: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getActivity(GetActivityRequest request, StreamObserver<ActivityResponse> responseObserver) {
        log.info("gRPC: Fetching activity with id: {}", request.getActivityId());

        try {
            // Get the activity
            Optional<ActivityEntry> activityOpt = reportingService.getActivity(request.getActivityId());

            if (activityOpt.isPresent()) {
                // Build and send the response
                ActivityResponse response = ActivityResponse.newBuilder()
                        .setActivity(toActivityProto(activityOpt.get()))
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                log.warn("Activity not found: {}", request.getActivityId());
                responseObserver.onError(new IllegalArgumentException("Activity not found: " + request.getActivityId()));
            }
        } catch (Exception e) {
            log.error("Error getting activity: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getActivitiesByReport(GetActivitiesByReportRequest request,
                                      StreamObserver<GetActivitiesByReportResponse> responseObserver) {
        log.info("gRPC: Fetching activities for report: {}", request.getReportId());

        try {
            // Get activities for report
            List<ActivityEntry> activities = reportingService.getActivitiesByReport(request.getReportId());

            // Build response with all activities
            GetActivitiesByReportResponse.Builder responseBuilder = GetActivitiesByReportResponse.newBuilder();
            activities.forEach(activity -> responseBuilder.addActivities(toActivityProto(activity)));

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting activities by report: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateActivity(UpdateActivityRequest request, StreamObserver<ActivityResponse> responseObserver) {
        log.info("gRPC: Updating activity: {}", request.getActivityId());

        try {
            // Create updated activity from request
            ActivityEntry updatedActivity = new ActivityEntry();
            updatedActivity.setDescription(request.getDescription());
            updatedActivity.setCategory(request.getCategory());
            updatedActivity.setStartTime(timestampToLocalDateTime(request.getStartTime()));
            updatedActivity.setEndTime(timestampToLocalDateTime(request.getEndTime()));
            updatedActivity.setProgress(request.getProgress());
            updatedActivity.setStatus(activityStatusFromProto(request.getStatus()));

            if (request.hasNotes()) {
                updatedActivity.setNotes(request.getNotes().getValue());
            }

            // Add personnel if provided
            if (request.getPersonnelCount() > 0) {
                Set<String> personnel = new HashSet<>(request.getPersonnelList());
                updatedActivity.setPersonnel(personnel);
            }

            // Update the activity
            ActivityEntry activity = reportingService.updateActivity(request.getActivityId(), updatedActivity);

            // Build and send the response
            ActivityResponse response = ActivityResponse.newBuilder()
                    .setActivity(toActivityProto(activity))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error updating activity: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateActivityProgress(UpdateActivityProgressRequest request,
                                       StreamObserver<ActivityResponse> responseObserver) {
        log.info("gRPC: Updating progress for activity: {} to {}%",
                request.getActivityId(), request.getProgress());

        try {
            // Update activity progress
            ActivityEntry activity = reportingService.updateActivityProgress(
                    request.getActivityId(),
                    request.getProgress(),
                    request.getUsername()
            );

            // Build and send the response
            ActivityResponse response = ActivityResponse.newBuilder()
                    .setActivity(toActivityProto(activity))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error updating activity progress: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteActivity(DeleteActivityRequest request, StreamObserver<DeleteActivityResponse> responseObserver) {
        log.info("gRPC: Deleting activity: {}", request.getActivityId());

        try {
            // Delete the activity
            reportingService.deleteActivity(request.getActivityId());

            // Send success response
            DeleteActivityResponse response = DeleteActivityResponse.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error deleting activity: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    // ANALYTICS OPERATIONS

    @Override
    public void getReportProgress(GetReportProgressRequest request,
                                  StreamObserver<GetReportProgressResponse> responseObserver) {
        log.info("gRPC: Calculating progress for report: {}", request.getReportId());

        try {
            // Calculate report progress
            double progress = reportingService.calculateReportProgress(request.getReportId());

            // Build and send the response
            GetReportProgressResponse response = GetReportProgressResponse.newBuilder()
                    .setProgress(progress)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error calculating report progress: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void isReportComplete(IsReportCompleteRequest request,
                                 StreamObserver<IsReportCompleteResponse> responseObserver) {
        log.info("gRPC: Checking if report is complete: {}", request.getReportId());

        try {
            // Check if report is complete
            boolean isComplete = reportingService.isReportComplete(request.getReportId());

            // Build and send the response
            IsReportCompleteResponse response = IsReportCompleteResponse.newBuilder()
                    .setIsComplete(isComplete)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error checking if report is complete: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getTotalDuration(GetTotalDurationRequest request,
                                 StreamObserver<GetTotalDurationResponse> responseObserver) {
        log.info("gRPC: Calculating total duration for report: {}", request.getReportId());

        try {
            // Calculate total duration
            long durationMinutes = reportingService.getTotalActivityDurationMinutes(request.getReportId());

            // Build and send the response
            GetTotalDurationResponse response = GetTotalDurationResponse.newBuilder()
                    .setDurationMinutes(durationMinutes)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error calculating total duration: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    // HELPER METHODS FOR PROTO <-> DOMAIN OBJECT CONVERSIONS

    /**
     * Convert a domain DailyReport to a gRPC DailyReport message
     */
    private com.se498.dailyreporting.grpc.DailyReport toDailyReportProto(DailyReport report) {
        com.se498.dailyreporting.grpc.DailyReport.Builder builder = com.se498.dailyreporting.grpc.DailyReport.newBuilder()
                .setId(report.getId())
                .setProjectId(report.getProjectId())
                .setReportDate(toDateProto(report.getReportDate()))
                .setStatus(toReportStatusProto(report.getStatus()))
                .setProgress(report.calculateProgress())
                .setComplete(report.isComplete());

        // Add optional fields if available
        if (report.getNotes() != null) {
            builder.setNotes(StringValue.of(report.getNotes()));
        }

        // Add timestamp fields
        if (report.getCreatedAt() != null) {
            builder.setCreatedAt(toTimestamp(report.getCreatedAt()));
        }
        if (report.getCreatedBy() != null) {
            builder.setCreatedBy(report.getCreatedBy());
        }
        if (report.getUpdatedAt() != null) {
            builder.setUpdatedAt(toTimestamp(report.getUpdatedAt()));
        }
        if (report.getUpdatedBy() != null) {
            builder.setUpdatedBy(StringValue.of(report.getUpdatedBy()));
        }

        // Add activities if available
        if (report.getActivities() != null) {
            report.getActivities().forEach(activity ->
                    builder.addActivities(toActivityProto(activity))
            );
        }

        return builder.build();
    }

    /**
     * Convert a domain ActivityEntry to a gRPC Activity message
     */
    private Activity toActivityProto(ActivityEntry activity) {
        Activity.Builder builder = Activity.newBuilder()
                .setId(activity.getId())
                .setReportId(activity.getReportId())
                .setDescription(activity.getDescription())
                .setCategory(activity.getCategory())
                .setStartTime(toTimestamp(activity.getStartTime()))
                .setEndTime(toTimestamp(activity.getEndTime()))
                .setProgress(activity.getProgress())
                .setStatus(toActivityStatusProto(activity.getStatus()))
                .setDurationMinutes(activity.calculateDuration().toMinutes());

        // Add optional fields if available
        if (activity.getNotes() != null) {
            builder.setNotes(StringValue.of(activity.getNotes()));
        }

        // Add personnel if available
        if (activity.getPersonnel() != null) {
            builder.addAllPersonnel(activity.getPersonnel());
        }

        // Add timestamp fields
        if (activity.getCreatedAt() != null) {
            builder.setCreatedAt(toTimestamp(activity.getCreatedAt()));
        }
        if (activity.getCreatedBy() != null) {
            builder.setCreatedBy(activity.getCreatedBy());
        }
        if (activity.getUpdatedAt() != null) {
            builder.setUpdatedAt(toTimestamp(activity.getUpdatedAt()));
        }
        if (activity.getUpdatedBy() != null) {
            builder.setUpdatedBy(StringValue.of(activity.getUpdatedBy()));
        }

        return builder.build();
    }

    /**
     * Convert a proto Date to a LocalDate
     */
    private LocalDate dateFromProto(com.se498.dailyreporting.grpc.Date date) {
        return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }

    /**
     * Convert a LocalDate to a proto Date
     */
    private com.se498.dailyreporting.grpc.Date toDateProto(LocalDate date) {
        return com.se498.dailyreporting.grpc.Date.newBuilder()
                .setYear(date.getYear())
                .setMonth(date.getMonthValue())
                .setDay(date.getDayOfMonth())
                .build();
    }

    /**
     * Convert a proto Timestamp to a LocalDateTime
     */
    private LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
                ZoneId.systemDefault()
        );
    }

    /**
     * Convert a LocalDateTime to a proto Timestamp
     */
    private Timestamp toTimestamp(LocalDateTime dateTime) {
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    /**
     * Convert a domain ReportStatus to a proto ReportStatus
     */
    private com.se498.dailyreporting.grpc.ReportStatus toReportStatusProto(ReportStatus status) {
        switch (status) {
            case DRAFT:
                return com.se498.dailyreporting.grpc.ReportStatus.REPORT_STATUS_DRAFT;
            case SUBMITTED:
                return com.se498.dailyreporting.grpc.ReportStatus.REPORT_STATUS_SUBMITTED;
            case APPROVED:
                return com.se498.dailyreporting.grpc.ReportStatus.REPORT_STATUS_APPROVED;
            case REJECTED:
                return com.se498.dailyreporting.grpc.ReportStatus.REPORT_STATUS_REJECTED;
            default:
                return com.se498.dailyreporting.grpc.ReportStatus.REPORT_STATUS_UNSPECIFIED;
        }
    }

    /**
     * Convert a proto ReportStatus to a domain ReportStatus
     */
    private ReportStatus reportStatusFromProto(com.se498.dailyreporting.grpc.ReportStatus status) {
        switch (status) {
            case REPORT_STATUS_DRAFT:
                return ReportStatus.DRAFT;
            case REPORT_STATUS_SUBMITTED:
                return ReportStatus.SUBMITTED;
            case REPORT_STATUS_APPROVED:
                return ReportStatus.APPROVED;
            case REPORT_STATUS_REJECTED:
                return ReportStatus.REJECTED;
            case UNRECOGNIZED:
            case REPORT_STATUS_UNSPECIFIED:
            default:
                throw new IllegalArgumentException("Invalid report status: " + status);
        }
    }

    /**
     * Convert a domain ActivityStatus to a proto ActivityStatus
     */
    private com.se498.dailyreporting.grpc.ActivityStatus toActivityStatusProto(ActivityStatus status) {
        switch (status) {
            case PLANNED:
                return com.se498.dailyreporting.grpc.ActivityStatus.ACTIVITY_STATUS_PLANNED;
            case IN_PROGRESS:
                return com.se498.dailyreporting.grpc.ActivityStatus.ACTIVITY_STATUS_IN_PROGRESS;
            case COMPLETED:
                return com.se498.dailyreporting.grpc.ActivityStatus.ACTIVITY_STATUS_COMPLETED;
            case DELAYED:
                return com.se498.dailyreporting.grpc.ActivityStatus.ACTIVITY_STATUS_DELAYED;
            case CANCELLED:
                return com.se498.dailyreporting.grpc.ActivityStatus.ACTIVITY_STATUS_CANCELLED;
            default:
                return com.se498.dailyreporting.grpc.ActivityStatus.ACTIVITY_STATUS_UNSPECIFIED;
        }
    }

    /**
     * Convert a proto ActivityStatus to a domain ActivityStatus
     */
    private ActivityStatus activityStatusFromProto(com.se498.dailyreporting.grpc.ActivityStatus status) {
        switch (status) {
            case ACTIVITY_STATUS_PLANNED:
                return ActivityStatus.PLANNED;
            case ACTIVITY_STATUS_IN_PROGRESS:
                return ActivityStatus.IN_PROGRESS;
            case ACTIVITY_STATUS_COMPLETED:
                return ActivityStatus.COMPLETED;
            case ACTIVITY_STATUS_DELAYED:
                return ActivityStatus.DELAYED;
            case ACTIVITY_STATUS_CANCELLED:
                return ActivityStatus.CANCELLED;
            case UNRECOGNIZED:
            case ACTIVITY_STATUS_UNSPECIFIED:
            default:
                throw new IllegalArgumentException("Invalid activity status: " + status);
        }
    }
}