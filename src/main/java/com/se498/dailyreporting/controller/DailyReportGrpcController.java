package com.se498.dailyreporting.controller;

import com.google.protobuf.StringValue;
import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.dto.grpc.GrpcMapper;
import com.se498.dailyreporting.grpc.*;
import com.se498.dailyreporting.service.DailyReportingService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * gRPC controller for daily report operations
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class DailyReportGrpcController extends DailyReportServiceGrpc.DailyReportServiceImplBase {

    @Autowired
    private final DailyReportingService reportingService;

    @Autowired
    private final GrpcMapper mapper;

    /**
     * Create a new daily report
     */
    @Override
    public void createReport(CreateReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        try {
            log.info("gRPC: Creating daily report for project {}, date {}",
                    request.getProjectId(), request.getReportDate());

            // Convert from gRPC date to LocalDate
            LocalDate reportDate = mapper.fromGrpcDate(request.getReportDate());

            // Create report
            DailyReport report = reportingService.createReport(
                    request.getProjectId(),
                    reportDate,
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

            // Return response
            DailyReportResponse response = mapper.toGrpcResponse(report);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error creating report: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error creating report: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Get a daily report by ID
     */
    @Override
    public void getReport(GetReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        try {
            log.info("gRPC: Fetching report with id: {}", request.getReportId());

            Optional<DailyReport> reportOpt = reportingService.getReport(request.getReportId());

            if (reportOpt.isPresent()) {
                DailyReportResponse response = mapper.toGrpcResponse(reportOpt.get());
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Report not found with ID: " + request.getReportId())
                                .asRuntimeException()
                );
            }

        } catch (Exception e) {
            log.error("Error getting report: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error getting report: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Update a daily report
     */
    @Override
    public void updateReport(UpdateReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        try {
            log.info("gRPC: Updating daily report: {}", request.getReportId());

            String notes = request.hasNotes() ? request.getNotes().getValue() : null;

            DailyReport report = reportingService.updateReport(
                    request.getReportId(),
                    notes,
                    request.getUsername()
            );

            DailyReportResponse response = mapper.toGrpcResponse(report);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (IllegalStateException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error updating report: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error updating report: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Submit a report
     */
    @Override
    public void submitReport(SubmitReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        try {
            log.info("gRPC: Submitting report: {}", request.getReportId());

            DailyReport report = reportingService.submitReport(
                    request.getReportId(),
                    request.getUsername()
            );

            DailyReportResponse response = mapper.toGrpcResponse(report);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (IllegalStateException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error submitting report: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error submitting report: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Approve a report
     */
    @Override
    public void approveReport(ApproveReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        try {
            log.info("gRPC: Approving report: {}", request.getReportId());

            DailyReport report = reportingService.approveReport(
                    request.getReportId(),
                    request.getUsername()
            );

            DailyReportResponse response = mapper.toGrpcResponse(report);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (IllegalStateException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error approving report: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error approving report: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Reject a report
     */
    @Override
    public void rejectReport(RejectReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
        try {
            log.info("gRPC: Rejecting report: {} with reason: {}",
                    request.getReportId(), request.getReason());

            DailyReport report = reportingService.rejectReport(
                    request.getReportId(),
                    request.getReason(),
                    request.getUsername()
            );

            DailyReportResponse response = mapper.toGrpcResponse(report);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (IllegalStateException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error rejecting report: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error rejecting report: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Delete a report
     */
    @Override
    public void deleteReport(DeleteReportRequest request, StreamObserver<BooleanResponse> responseObserver) {
        try {
            log.info("gRPC: Deleting report: {}", request.getReportId());

            reportingService.deleteReport(request.getReportId());

            BooleanResponse response = BooleanResponse.newBuilder().setValue(true).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (IllegalStateException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error deleting report: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error deleting report: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Get reports by project
     */
    @Override
    public void getReportsByProject(GetReportsByProjectRequest request, StreamObserver<ReportListResponse> responseObserver) {
        try {
            log.info("gRPC: Fetching reports for project: {}", request.getProjectId());

            List<DailyReport> reports;

            // If date range is provided, use it
            if (request.hasStartDate() && request.hasEndDate()) {
                LocalDate startDate = LocalDate.parse(request.getStartDate().getValue());
                LocalDate endDate = LocalDate.parse(request.getEndDate().getValue());

                log.info("Using date range: {} to {}", startDate, endDate);
                reports = reportingService.getReportsByDateRange(
                        request.getProjectId(), startDate, endDate);
            } else {
                reports = reportingService.getReportsByProject(request.getProjectId());
            }

            ReportListResponse response = mapper.toGrpcReportListResponse(reports);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error getting reports by project: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error getting reports by project: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Get reports by status
     */
    @Override
    public void getReportsByStatus(GetReportsByStatusRequest request, StreamObserver<ReportListResponse> responseObserver) {
        try {
            ReportStatus status = mapper.fromGrpcReportStatus(request.getStatus());
            log.info("gRPC: Fetching reports with status: {}", status);

            List<DailyReport> reports = reportingService.getReportsByStatus(status);

            ReportListResponse response = mapper.toGrpcReportListResponse(reports);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error getting reports by status: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error getting reports by status: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Add an activity to a report
     */
    @Override
    public void addActivity(AddActivityRequest request, StreamObserver<ActivityResponse> responseObserver) {
        try {
            log.info("gRPC: Adding activity to report: {}", request.getReportId());

            // Create activity from request
            ActivityEntry activity = mapper.fromGrpcAddActivityRequest(request);
            activity.setId(UUID.randomUUID().toString());

            // Add activity to report
            ActivityEntry savedActivity = reportingService.addActivityToReport(
                    request.getReportId(), activity);

            ActivityResponse response = mapper.toGrpcActivity(savedActivity);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (IllegalStateException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error adding activity: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error adding activity: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Get an activity by ID
     */
    @Override
    public void getActivity(GetActivityRequest request, StreamObserver<ActivityResponse> responseObserver) {
        try {
            log.info("gRPC: Fetching activity: {}", request.getActivityId());

            Optional<ActivityEntry> activityOpt = reportingService.getActivity(request.getActivityId());

            if (activityOpt.isPresent()) {
                ActivityResponse response = mapper.toGrpcActivity(activityOpt.get());
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Activity not found with ID: " + request.getActivityId())
                                .asRuntimeException()
                );
            }

        } catch (Exception e) {
            log.error("Error getting activity: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error getting activity: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Get activities by report
     */
    @Override
    public void getActivitiesByReport(GetActivitiesByReportRequest request, StreamObserver<ActivityListResponse> responseObserver) {
        try {
            log.info("gRPC: Fetching activities for report: {}", request.getReportId());

            List<ActivityEntry> activities = reportingService.getActivitiesByReport(request.getReportId());

            ActivityListResponse response = mapper.toGrpcActivityListResponse(activities);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error getting activities by report: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error getting activities by report: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Update an activity
     */
    @Override
    public void updateActivity(UpdateActivityRequest request, StreamObserver<ActivityResponse> responseObserver) {
        try {
            log.info("gRPC: Updating activity: {}", request.getActivityId());

            // Create updated activity from request
            ActivityEntry activity = mapper.fromGrpcUpdateActivityRequest(request);

            // Update activity
            ActivityEntry updatedActivity = reportingService.updateActivity(
                    request.getActivityId(), activity);

            ActivityResponse response = mapper.toGrpcActivity(updatedActivity);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (IllegalStateException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error updating activity: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error updating activity: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Update activity progress
     */
    @Override
    public void updateActivityProgress(UpdateActivityProgressRequest request, StreamObserver<ActivityResponse> responseObserver) {
        try {
            log.info("gRPC: Updating progress for activity: {} to {}%",
                    request.getActivityId(), request.getProgress());

            ActivityEntry updatedActivity = reportingService.updateActivityProgress(
                    request.getActivityId(),
                    request.getProgress(),
                    request.getUsername()
            );

            ActivityResponse response = mapper.toGrpcActivity(updatedActivity);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error updating activity progress: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error updating activity progress: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Delete an activity
     */
    @Override
    public void deleteActivity(DeleteActivityRequest request, StreamObserver<BooleanResponse> responseObserver) {
        try {
            log.info("gRPC: Deleting activity: {}", request.getActivityId());

            reportingService.deleteActivity(request.getActivityId());

            BooleanResponse response = BooleanResponse.newBuilder().setValue(true).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (IllegalStateException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error deleting activity: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error deleting activity: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Get report progress
     */
    @Override
    public void getReportProgress(GetReportProgressRequest request, StreamObserver<DoubleResponse> responseObserver) {
        try {
            log.info("gRPC: Calculating progress for report: {}", request.getReportId());

            double progress = reportingService.calculateReportProgress(request.getReportId());

            DoubleResponse response = DoubleResponse.newBuilder().setValue(progress).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error calculating report progress: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error calculating report progress: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Check if report is complete
     */
    @Override
    public void isReportComplete(IsReportCompleteRequest request, StreamObserver<BooleanResponse> responseObserver) {
        try {
            log.info("gRPC: Checking if report is complete: {}", request.getReportId());

            boolean isComplete = reportingService.isReportComplete(request.getReportId());

            BooleanResponse response = BooleanResponse.newBuilder().setValue(isComplete).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error checking if report is complete: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error checking if report is complete: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Get total duration of activities in a report
     */
    @Override
    public void getTotalDuration(GetTotalDurationRequest request, StreamObserver<LongResponse> responseObserver) {
        try {
            log.info("gRPC: Calculating total duration for report: {}", request.getReportId());

            long durationMinutes = reportingService.getTotalActivityDurationMinutes(request.getReportId());

            LongResponse response = LongResponse.newBuilder().setValue(durationMinutes).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error calculating total duration: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error calculating total duration: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }
}