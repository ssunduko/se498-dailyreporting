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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * gRPC controller for daily reporting service
 * Provides RPC endpoints for all daily reporting operations
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class DailyReportGrpcController extends DailyReportingServiceGrpc.DailyReportingServiceImplBase {

    @Autowired
    private final DailyReportingService reportingService;

    @Override
    public void createReport(CreateReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Creating daily report for project {}, date {}",
                request.getProjectId(), toLocalDate(request.getReportDate()));

        DailyReport report = reportingService.createReport(
                request.getProjectId(),
                toLocalDate(request.getReportDate()),
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

        DailyReportResponse response = DailyReportResponse.newBuilder()
                .setReport(toDailyReportProto(report))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getReport(GetReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Fetching report with id: {}", request.getReportId());

        reportingService.getReport(request.getReportId())
                .ifPresentOrElse(
                        report -> {
                            DailyReportResponse response = DailyReportResponse.newBuilder()
                                    .setReport(toDailyReportProto(report))
                                    .build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        () -> {
                            // If report not found, send empty response
                            responseObserver.onNext(DailyReportResponse.newBuilder().build());
                            responseObserver.onCompleted();
                        }
                );
    }

    @Override
    public void updateReport(UpdateReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Updating report: {}", request.getReportId());

        DailyReport report = reportingService.updateReport(
                request.getReportId(),
                request.hasNotes() ? request.getNotes().getValue() : null,
                request.getUsername()
        );

        DailyReportResponse response = DailyReportResponse.newBuilder()
                .setReport(toDailyReportProto(report))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void submitReport(SubmitReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Submitting report: {}", request.getReportId());

        DailyReport report = reportingService.submitReport(
                request.getReportId(),
                request.getUsername()
        );

        DailyReportResponse response = DailyReportResponse.newBuilder()
                .setReport(toDailyReportProto(report))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void approveReport(ApproveReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Approving report: {}", request.getReportId());

        DailyReport report = reportingService.approveReport(
                request.getReportId(),
                request.getUsername()
        );

        DailyReportResponse response = DailyReportResponse.newBuilder()
                .setReport(toDailyReportProto(report))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void rejectReport(RejectReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        log.info("gRPC: Rejecting report: {} with reason: {}",
                request.getReportId(), request.getReason());

        DailyReport report = reportingService.rejectReport(
                request.getReportId(),
                request.getReason(),
                request.getUsername()
        );

        DailyReportResponse response = DailyReportResponse.newBuilder()
                .setReport(toDailyReportProto(report))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteReport(DeleteReportRequest request, StreamObserver<DeleteReportResponse> responseObserver) {
        log.info("gRPC: Deleting report: {}", request.getReportId());

        reportingService.deleteReport(request.getReportId());

        DeleteReportResponse response = DeleteReportResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getReportsByProject(GetReportsByProjectRequest request,
                                    StreamObserver<GetReportsByProjectResponse> responseObserver) {
        log.info("gRPC: Fetching reports for project: {}", request.getProjectId());

        List<DailyReport> reports;

        if (request.hasStartDate() && request.hasEndDate()) {
            LocalDate startDate = toLocalDate(request.getStartDate());
            LocalDate endDate = toLocalDate(request.getEndDate());

            reports = reportingService.getReportsByDateRange(
                    request.getProjectId(), startDate, endDate);
        } else {
            reports = reportingService.getReportsByProject(request.getProjectId());
        }

        List<com.se498.dailyreporting.grpc.DailyReport> protoReports = reports.stream()
                .map(this::toDailyReportProto)
                .collect(Collectors.toList());

        GetReportsByProjectResponse response = GetReportsByProjectResponse.newBuilder()
                .addAllReports(protoReports)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getReportsByStatus(GetReportsByStatusRequest request,
                                   StreamObserver<GetReportsByStatusResponse> responseObserver) {
        ReportStatus status = toReportStatus(request.getStatus());
        log.info("gRPC: Fetching reports with status: {}", status);

        List<DailyReport> reports = reportingService.getReportsByStatus(status);

        List<com.se498.dailyreporting.grpc.DailyReport> protoReports = reports.stream()
                .map(this::toDailyReportProto)
                .collect(Collectors.toList());

        GetReportsByStatusResponse response = GetReportsByStatusResponse.newBuilder()
                .addAllReports(protoReports)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addActivity(AddActivityRequest request, StreamObserver<ActivityResponse> responseObserver) {
        log.info("gRPC: Adding activity to report: {}", request.getReportId());

        // Create activity from request
        ActivityEntry activity = new ActivityEntry();
        activity.setId(UUID.randomUUID().toString());
        activity.setReportId(request.getReportId());
        activity.setDescription(request.getDescription());
        activity.setCategory(request.getCategory());
        activity.setStartTime(toLocalDateTime(request.getStartTime()));
        activity.setEndTime(toLocalDateTime(request.getEndTime()));
        activity.setProgress(request.getProgress());
        activity.setStatus(toActivityStatus(request.getStatus()));
        activity.setNotes(request.hasNotes() ? request.getNotes().getValue() : null);
        activity.setCreatedBy(request.getUsername());
        activity.setCreatedAt(LocalDateTime.now());

        // Add personnel if provided
        if (request.getPersonnelCount() > 0) {
            Set<String> personnel = new HashSet<>(request.getPersonnelList());
            activity.setPersonnel(personnel);
        }

        // Add activity to report
        ActivityEntry savedActivity = reportingService.addActivityToReport(request.getReportId(), activity);

        ActivityResponse response = ActivityResponse.newBuilder()
                .setActivity(toActivityProto(savedActivity))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getActivity(GetActivityRequest request, StreamObserver<ActivityResponse> responseObserver) {
        log.info("gRPC: Fetching activity with id: {}", request.getActivityId());

        reportingService.getActivity(request.getActivityId())
                .ifPresentOrElse(
                        activity -> {
                            ActivityResponse response = ActivityResponse.newBuilder()
                                    .setActivity(toActivityProto(activity))
                                    .build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        () -> {
                            // If activity not found, send empty response
                            responseObserver.onNext(ActivityResponse.newBuilder().build());
                            responseObserver.onCompleted();
                        }
                );
    }

    @Override
    public void getActivitiesByReport(GetActivitiesByReportRequest request,
                                      StreamObserver<GetActivitiesByReportResponse> responseObserver) {
        log.info("gRPC: Fetching activities for report: {}", request.getReportId());

        List<ActivityEntry> activities = reportingService.getActivitiesByReport(request.getReportId());

        List<Activity> protoActivities = activities.stream()
                .map(this::toActivityProto)
                .collect(Collectors.toList());

        GetActivitiesByReportResponse response = GetActivitiesByReportResponse.newBuilder()
                .addAllActivities(protoActivities)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateActivity(UpdateActivityRequest request, StreamObserver<ActivityResponse> responseObserver) {
        log.info("gRPC: Updating activity: {}", request.getActivityId());

        // Create updated activity from request
        ActivityEntry activity = new ActivityEntry();
        activity.setDescription(request.getDescription());
        activity.setCategory(request.getCategory());
        activity.setStartTime(toLocalDateTime(request.getStartTime()));
        activity.setEndTime(toLocalDateTime(request.getEndTime()));
        activity.setProgress(request.getProgress());
        activity.setStatus(toActivityStatus(request.getStatus()));
        activity.setNotes(request.hasNotes() ? request.getNotes().getValue() : null);

        // Add personnel if provided
        if (request.getPersonnelCount() > 0) {
            Set<String> personnel = new HashSet<>(request.getPersonnelList());
            activity.setPersonnel(personnel);
        }

        // Update activity
        ActivityEntry updatedActivity = reportingService.updateActivity(request.getActivityId(), activity);

        ActivityResponse response = ActivityResponse.newBuilder()
                .setActivity(toActivityProto(updatedActivity))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateActivityProgress(UpdateActivityProgressRequest request,
                                       StreamObserver<ActivityResponse> responseObserver) {
        log.info("gRPC: Updating progress for activity: {} to {}%",
                request.getActivityId(), request.getProgress());

        ActivityEntry updatedActivity = reportingService.updateActivityProgress(
                request.getActivityId(),
                request.getProgress(),
                request.getUsername()
        );

        ActivityResponse response = ActivityResponse.newBuilder()
                .setActivity(toActivityProto(updatedActivity))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteActivity(DeleteActivityRequest request,
                               StreamObserver<DeleteActivityResponse> responseObserver) {
        log.info("gRPC: Deleting activity: {}", request.getActivityId());

        reportingService.deleteActivity(request.getActivityId());

        DeleteActivityResponse response = DeleteActivityResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getReportProgress(GetReportProgressRequest request,
                                  StreamObserver<GetReportProgressResponse> responseObserver) {
        log.info("gRPC: Calculating progress for report: {}", request.getReportId());

        double progress = reportingService.calculateReportProgress(request.getReportId());

        GetReportProgressResponse response = GetReportProgressResponse.newBuilder()
                .setProgress(progress)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void isReportComplete(IsReportCompleteRequest request,
                                 StreamObserver<IsReportCompleteResponse> responseObserver) {
        log.info("gRPC: Checking if report is complete: {}", request.getReportId());

        boolean isComplete = reportingService.isReportComplete(request.getReportId());

        IsReportCompleteResponse response = IsReportCompleteResponse.newBuilder()
                .setIsComplete(isComplete)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTotalDuration(GetTotalDurationRequest request,
                                 StreamObserver<GetTotalDurationResponse> responseObserver) {
        log.info("gRPC: Calculating duration for report: {}", request.getReportId());

        long durationMinutes = reportingService.getTotalActivityDurationMinutes(request.getReportId());

        GetTotalDurationResponse response = GetTotalDurationResponse.newBuilder()
                .setDurationMinutes(durationMinutes)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Helper methods for mapping between domain objects and protobuf messages

    private com.se498.dailyreporting.grpc.DailyReport toDailyReportProto(DailyReport report) {
        com.se498.dailyreporting.grpc.DailyReport.Builder builder = com.se498.dailyreporting.grpc.DailyReport.newBuilder()
                .setId(report.getId())
                .setProjectId(report.getProjectId())
                .setReportDate(toDateProto(report.getReportDate()))
                .setStatus(toReportStatusProto(report.getStatus()))
                .setCreatedAt(toTimestamp(report.getCreatedAt()))
                .setCreatedBy(report.getCreatedBy())
                .setProgress(report.calculateProgress())
                .setComplete(report.isComplete());

        if (report.getNotes() != null) {
            builder.setNotes(StringValue.of(report.getNotes()));
        }

        if (report.getUpdatedAt() != null) {
            builder.setUpdatedAt(toTimestamp(report.getUpdatedAt()));
        }

        if (report.getUpdatedBy() != null) {
            builder.setUpdatedBy(StringValue.of(report.getUpdatedBy()));
        }

        if (report.getActivities() != null) {
            report.getActivities().forEach(activity ->
                    builder.addActivities(toActivityProto(activity)));
        }

        return builder.build();
    }

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
                .setCreatedAt(toTimestamp(activity.getCreatedAt()))
                .setCreatedBy(activity.getCreatedBy())
                .setDurationMinutes(activity.calculateDuration().toMinutes());

        if (activity.getNotes() != null) {
            builder.setNotes(StringValue.of(activity.getNotes()));
        }

        if (activity.getUpdatedAt() != null) {
            builder.setUpdatedAt(toTimestamp(activity.getUpdatedAt()));
        }

        if (activity.getUpdatedBy() != null) {
            builder.setUpdatedBy(StringValue.of(activity.getUpdatedBy()));
        }

        if (activity.getPersonnel() != null) {
            builder.addAllPersonnel(activity.getPersonnel());
        }

        return builder.build();
    }

    private com.se498.dailyreporting.grpc.Date toDateProto(LocalDate date) {
        return com.se498.dailyreporting.grpc.Date.newBuilder()
                .setYear(date.getYear())
                .setMonth(date.getMonthValue())
                .setDay(date.getDayOfMonth())
                .build();
    }

    private LocalDate toLocalDate(com.se498.dailyreporting.grpc.Date date) {
        return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }

    private com.se498.dailyreporting.grpc.ReportStatus toReportStatusProto(ReportStatus status) {
        switch(status) {
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

    private ReportStatus toReportStatus(com.se498.dailyreporting.grpc.ReportStatus status) {
        switch(status) {
            case REPORT_STATUS_DRAFT:
                return ReportStatus.DRAFT;
            case REPORT_STATUS_SUBMITTED:
                return ReportStatus.SUBMITTED;
            case REPORT_STATUS_APPROVED:
                return ReportStatus.APPROVED;
            case REPORT_STATUS_REJECTED:
                return ReportStatus.REJECTED;
            default:
                return ReportStatus.DRAFT;
        }
    }

    private com.se498.dailyreporting.grpc.ActivityStatus toActivityStatusProto(ActivityStatus status) {
        switch(status) {
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

    private ActivityStatus toActivityStatus(com.se498.dailyreporting.grpc.ActivityStatus status) {
        switch(status) {
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
            default:
                return ActivityStatus.PLANNED;
        }
    }

    private Timestamp toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return Timestamp.getDefaultInstance();
        }
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
                ZoneId.systemDefault());
    }
}