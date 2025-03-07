package com.se498.dailyreporting.controller;

import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import com.se498.dailyreporting.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Standalone runnable client for testing gRPC API
 * Can be executed independently of the Spring Boot application
 */
public class SimpleGrpcClient {

    private final ManagedChannel channel;
    private final DailyReportingServiceGrpc.DailyReportingServiceBlockingStub blockingStub;
    private final String username;

    public SimpleGrpcClient(String host, int port, String username) {
        this.username = username;
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = DailyReportingServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Test creating a report
     */
    public DailyReport testCreateReport() {
        System.out.println("\n=== Testing createReport ===");

        try {
            // Create a unique project ID for testing
            String projectId = "test-project-" + UUID.randomUUID().toString().substring(0, 8);
            LocalDate reportDate = LocalDate.now();

            // Build the date
            Date date = Date.newBuilder()
                    .setYear(reportDate.getYear())
                    .setMonth(reportDate.getMonthValue())
                    .setDay(reportDate.getDayOfMonth())
                    .build();

            // Build the request
            CreateReportRequest request = CreateReportRequest.newBuilder()
                    .setProjectId(projectId)
                    .setReportDate(date)
                    .setUsername(username)
                    .setNotes(StringValue.of("Test report created via gRPC client"))
                    .build();

            // Make the API call
            System.out.println("Creating report for project " + projectId + " on " + reportDate);
            DailyReportResponse response = blockingStub.createReport(request);
            DailyReport report = response.getReport();

            // Display the result
            System.out.println("Created report with ID: " + report.getId());
            System.out.println("Project ID: " + report.getProjectId());
            System.out.println("Status: " + report.getStatus().name());
            System.out.println("Report date: " + report.getReportDate().getYear() + "-" +
                    report.getReportDate().getMonth() + "-" + report.getReportDate().getDay());

            return report;
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Test adding an activity to a report
     */
    public Activity testAddActivity(DailyReport report) {
        System.out.println("\n=== Testing addActivity ===");

        if (report == null) {
            System.out.println("Skipping - no report available");
            return null;
        }

        try {
            // Set up activity times
            LocalDateTime startTime = LocalDateTime.now().minusHours(2);
            LocalDateTime endTime = LocalDateTime.now().minusHours(1);

            // Build the request
            AddActivityRequest request = AddActivityRequest.newBuilder()
                    .setReportId(report.getId())
                    .setDescription("Test Activity")
                    .setCategory("Testing")
                    .setStartTime(toTimestamp(startTime))
                    .setEndTime(toTimestamp(endTime))
                    .setProgress(50.0)
                    .setStatus(ActivityStatus.ACTIVITY_STATUS_IN_PROGRESS)
                    .setUsername(username)
                    .addPersonnel("Person 1")
                    .addPersonnel("Person 2")
                    .setNotes(StringValue.of("This is a test activity"))
                    .build();

            // Make the API call
            System.out.println("Adding activity to report " + report.getId());
            ActivityResponse response = blockingStub.addActivity(request);
            Activity activity = response.getActivity();

            // Display the result
            System.out.println("Added activity with ID: " + activity.getId());
            System.out.println("Description: " + activity.getDescription());
            System.out.println("Progress: " + activity.getProgress() + "%");
            System.out.println("Status: " + activity.getStatus().name());
            System.out.println("Duration: " + activity.getDurationMinutes() + " minutes");

            return activity;
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Test getting reports by status
     */
    public void testGetReportsByStatus() {
        System.out.println("\n=== Testing getReportsByStatus ===");

        try {
            // Build the request
            GetReportsByStatusRequest request = GetReportsByStatusRequest.newBuilder()
                    .setStatus(ReportStatus.REPORT_STATUS_DRAFT)
                    .build();

            // Make the API call
            System.out.println("Getting reports with status DRAFT");
            GetReportsByStatusResponse response = blockingStub.getReportsByStatus(request);
            List<DailyReport> reports = response.getReportsList();

            // Display the result
            System.out.println("Found " + reports.size() + " reports with DRAFT status");

            // Show details of first few reports
            int count = 0;
            for (DailyReport report : reports) {
                if (count++ >= 3) {
                    System.out.println("... and " + (reports.size() - 3) + " more");
                    break;
                }

                System.out.println("Report ID: " + report.getId());
                System.out.println("  Project: " + report.getProjectId());
                System.out.println("  Activities: " + report.getActivitiesCount());
                System.out.println("  Progress: " + report.getProgress() + "%");
                System.out.println();
            }
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            if (e.getStatus().getCode().name().equals("NOT_FOUND")) {
                System.out.println("No reports found with DRAFT status (expected for new installation)");
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * Test updating progress on an activity
     */
    public void testUpdateActivityProgress(Activity activity) {
        System.out.println("\n=== Testing updateActivityProgress ===");

        if (activity == null) {
            System.out.println("Skipping - no activity available");
            return;
        }

        try {
            // New progress value
            double newProgress = 75.0;

            // Build the request
            UpdateActivityProgressRequest request = UpdateActivityProgressRequest.newBuilder()
                    .setActivityId(activity.getId())
                    .setProgress(newProgress)
                    .setUsername(username)
                    .build();

            // Make the API call
            System.out.println("Updating progress for activity " + activity.getId() + " to " + newProgress + "%");
            ActivityResponse response = blockingStub.updateActivityProgress(request);
            Activity updatedActivity = response.getActivity();

            // Display the result
            System.out.println("Updated activity progress:");
            System.out.println("ID: " + updatedActivity.getId());
            System.out.println("New progress: " + updatedActivity.getProgress() + "%");
            System.out.println("Status: " + updatedActivity.getStatus().name());
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            e.printStackTrace();
        }
    }

    /**
     * Test calculating report progress
     */
    public void testGetReportProgress(DailyReport report) {
        System.out.println("\n=== Testing getReportProgress ===");

        if (report == null) {
            System.out.println("Skipping - no report available");
            return;
        }

        try {
            // Build the request
            GetReportProgressRequest request = GetReportProgressRequest.newBuilder()
                    .setReportId(report.getId())
                    .build();

            // Make the API call
            System.out.println("Calculating progress for report " + report.getId());
            GetReportProgressResponse response = blockingStub.getReportProgress(request);

            // Display the result
            System.out.println("Report progress: " + response.getProgress() + "%");
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            e.printStackTrace();
        }
    }

    /**
     * Main entry point
     */
    public static void main(String[] args) throws Exception {
        // Default connection parameters
        String host = "localhost";
        int port = 9090;
        String username = "test-user";

        // Parse command line args if provided
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            username = args[2];
        }

        System.out.println("==============================================");
        System.out.println("gRPC Client Test Runner");
        System.out.println("==============================================");
        System.out.println("Connecting to: " + host + ":" + port);
        System.out.println("Username: " + username);
        System.out.println("==============================================");

        // Create the client
        SimpleGrpcClient client = new SimpleGrpcClient(host, port, username);

        try {
            // Run tests
            DailyReport report = client.testCreateReport();
            Activity activity = client.testAddActivity(report);
            client.testUpdateActivityProgress(activity);
            client.testGetReportProgress(report);
            client.testGetReportsByStatus();

            System.out.println("\n=== All tests completed ===");
        } finally {
            // Shutdown properly
            client.shutdown();
        }
    }

    /**
     * Helper method to convert LocalDateTime to Timestamp
     */
    private static Timestamp toTimestamp(LocalDateTime dateTime) {
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}