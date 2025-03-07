package com.se498.dailyreporting.controller;

import com.google.protobuf.StringValue;
import com.se498.dailyreporting.grpc.*;
import com.se498.dailyreporting.grpc.DailyReportResponse;
import com.se498.dailyreporting.grpc.DailyReportServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple gRPC client for testing the Daily Report gRPC service
 */
@Slf4j
public class DailyReportGrpcClient {

    private final ManagedChannel channel;
    private final DailyReportServiceGrpc.DailyReportServiceBlockingStub blockingStub;

    /**
     * Construct client connecting to DailyReportService at specified host and port
     */
    public DailyReportGrpcClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()  // Note: Not suitable for production
                .build());
    }

    /**
     * Construct client using an existing channel
     */
    public DailyReportGrpcClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = DailyReportServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Shutdown the client
     */
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Create a new daily report
     */
    public DailyReportResponse createReport(String projectId, LocalDate reportDate, String notes, String username) {
        log.info("Creating report for project {} on date {}", projectId, reportDate);

        // Convert LocalDate to proto Date
        Date protoDate = Date.newBuilder()
                .setYear(reportDate.getYear())
                .setMonth(reportDate.getMonthValue())
                .setDay(reportDate.getDayOfMonth())
                .build();

        CreateReportRequest.Builder requestBuilder = CreateReportRequest.newBuilder()
                .setProjectId(projectId)
                .setReportDate(protoDate)
                .setUsername(username);

        if (notes != null && !notes.isEmpty()) {
            requestBuilder.setNotes(StringValue.of(notes));
        }

        try {
            return blockingStub.createReport(requestBuilder.build());
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Get a report by ID
     */
    public DailyReportResponse getReport(String reportId) {
        log.info("Getting report with ID: {}", reportId);

        GetReportRequest request = GetReportRequest.newBuilder()
                .setReportId(reportId)
                .build();

        try {
            return blockingStub.getReport(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Update a report
     */
    public DailyReportResponse updateReport(String reportId, String notes, String username) {
        log.info("Updating report with ID: {}", reportId);

        UpdateReportRequest.Builder requestBuilder = UpdateReportRequest.newBuilder()
                .setReportId(reportId)
                .setUsername(username);

        if (notes != null && !notes.isEmpty()) {
            requestBuilder.setNotes(StringValue.of(notes));
        }

        try {
            return blockingStub.updateReport(requestBuilder.build());
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Submit a report
     */
    public DailyReportResponse submitReport(String reportId, String username) {
        log.info("Submitting report with ID: {}", reportId);

        SubmitReportRequest request = SubmitReportRequest.newBuilder()
                .setReportId(reportId)
                .setUsername(username)
                .build();

        try {
            return blockingStub.submitReport(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Approve a report
     */
    public DailyReportResponse approveReport(String reportId, String username) {
        log.info("Approving report with ID: {}", reportId);

        ApproveReportRequest request = ApproveReportRequest.newBuilder()
                .setReportId(reportId)
                .setUsername(username)
                .build();

        try {
            return blockingStub.approveReport(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Reject a report
     */
    public DailyReportResponse rejectReport(String reportId, String reason, String username) {
        log.info("Rejecting report with ID: {} reason: {}", reportId, reason);

        RejectReportRequest request = RejectReportRequest.newBuilder()
                .setReportId(reportId)
                .setReason(reason)
                .setUsername(username)
                .build();

        try {
            return blockingStub.rejectReport(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Delete a report
     */
    public boolean deleteReport(String reportId) {
        log.info("Deleting report with ID: {}", reportId);

        DeleteReportRequest request = DeleteReportRequest.newBuilder()
                .setReportId(reportId)
                .build();

        try {
            BooleanResponse response = blockingStub.deleteReport(request);
            return response.getValue();
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Get reports by project
     */
    public List<DailyReportResponse> getReportsByProject(String projectId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting reports for project: {} between {} and {}", projectId, startDate, endDate);

        GetReportsByProjectRequest.Builder requestBuilder = GetReportsByProjectRequest.newBuilder()
                .setProjectId(projectId);

        if (startDate != null) {
            requestBuilder.setStartDate(StringValue.of(startDate.toString()));
        }

        if (endDate != null) {
            requestBuilder.setEndDate(StringValue.of(endDate.toString()));
        }

        try {
            ReportListResponse response = blockingStub.getReportsByProject(requestBuilder.build());
            return response.getReportsList();
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Add an activity to a report
     */
    public ActivityResponse addActivity(AddActivityRequest request) {
        log.info("Adding activity to report: {}", request.getReportId());

        try {
            return blockingStub.addActivity(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Get activities for a report
     */
    public List<ActivityResponse> getActivitiesByReport(String reportId) {
        log.info("Getting activities for report: {}", reportId);

        GetActivitiesByReportRequest request = GetActivitiesByReportRequest.newBuilder()
                .setReportId(reportId)
                .build();

        try {
            ActivityListResponse response = blockingStub.getActivitiesByReport(request);
            return response.getActivitiesList();
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Get report progress
     */
    public double getReportProgress(String reportId) {
        log.info("Getting progress for report: {}", reportId);

        GetReportProgressRequest request = GetReportProgressRequest.newBuilder()
                .setReportId(reportId)
                .build();

        try {
            DoubleResponse response = blockingStub.getReportProgress(request);
            return response.getValue();
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Check if a report is complete
     */
    public boolean isReportComplete(String reportId) {
        log.info("Checking if report is complete: {}", reportId);

        IsReportCompleteRequest request = IsReportCompleteRequest.newBuilder()
                .setReportId(reportId)
                .build();

        try {
            BooleanResponse response = blockingStub.isReportComplete(request);
            return response.getValue();
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Get the total duration of all activities in a report
     */
    public long getTotalDuration(String reportId) {
        log.info("Getting total duration for report: {}", reportId);

        GetTotalDurationRequest request = GetTotalDurationRequest.newBuilder()
                .setReportId(reportId)
                .build();

        try {
            LongResponse response = blockingStub.getTotalDuration(request);
            return response.getValue();
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Simple command-line client for testing
     */
    public static void main(String[] args) throws Exception {
        // Default settings if arguments not provided
        String host = "localhost";
        int port = 9090;

        // Parse arguments
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        DailyReportGrpcClient client = new DailyReportGrpcClient(host, port);
        try {
            // Create a report
            String projectId = "test-project";
            LocalDate reportDate = LocalDate.now();
            String username = "test-user";

            DailyReportResponse report = client.createReport(projectId, reportDate, "Test notes", username);
            System.out.println("Created report with ID: " + report.getId());

            // Get the report
            report = client.getReport(report.getId());
            System.out.println("Retrieved report with ID: " + report.getId());

            // Submit the report
            report = client.submitReport(report.getId(), username);
            System.out.println("Submitted report, status: " + report.getStatus());

            // Get report progress
            double progress = client.getReportProgress(report.getId());
            System.out.println("Report progress: " + progress + "%");

            // Check if report is complete
            boolean isComplete = client.isReportComplete(report.getId());
            System.out.println("Report is complete: " + isComplete);

        } finally {
            client.shutdown();
        }
    }
}