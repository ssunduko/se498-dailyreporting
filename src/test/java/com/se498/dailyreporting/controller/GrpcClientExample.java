package com.se498.dailyreporting.controller;


import com.google.protobuf.StringValue;
import com.se498.dailyreporting.client.AuthenticationInterceptor;
import com.se498.dailyreporting.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Example of using the gRPC client directly
 * This is a standalone application that demonstrates how to use the gRPC API
 * for the Daily Reporting Service
 */
public class GrpcClientExample {

    private final ManagedChannel channel;
    private final DailyReportingServiceGrpc.DailyReportingServiceBlockingStub blockingStub;

    /**
     * Initialize the gRPC client
     */
    public GrpcClientExample(String host, int port, String username, String password) {
        // Create the channel
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // For development only, use TLS in production
                .intercept(new AuthenticationInterceptor(username, password))
                .build();

        // Create the stub
        blockingStub = DailyReportingServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Shutdown the channel
     */
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Create a new daily report
     */
    public DailyReport createReport(String projectId, LocalDate reportDate, String notes) {
        System.out.println("Creating report for project " + projectId + " on " + reportDate);

        // Build the date
        Date date = Date.newBuilder()
                .setYear(reportDate.getYear())
                .setMonth(reportDate.getMonthValue())
                .setDay(reportDate.getDayOfMonth())
                .build();

        // Build the request
        CreateReportRequest.Builder requestBuilder = CreateReportRequest.newBuilder()
                .setProjectId(projectId)
                .setReportDate(date)
                .setUsername("example-user");

        // Add notes if provided
        if (notes != null && !notes.isEmpty()) {
            requestBuilder.setNotes(StringValue.of(notes));
        }

        // Send the request and get the response
        DailyReportResponse response = blockingStub.createReport(requestBuilder.build());
        return response.getReport();
    }

    /**
     * Get reports by project
     */
    public List<DailyReport> getReportsByProject(String projectId) {
        System.out.println("Getting reports for project " + projectId);

        // Build the request
        GetReportsByProjectRequest request = GetReportsByProjectRequest.newBuilder()
                .setProjectId(projectId)
                .build();

        // Send the request and get the response
        GetReportsByProjectResponse response = blockingStub.getReportsByProject(request);
        return response.getReportsList();
    }

    /**
     * Add an activity to a report
     */
    public Activity addActivity(String reportId, String description, String category,
                                LocalDateTime startTime, LocalDateTime endTime) {
        System.out.println("Adding activity to report " + reportId);

        // Build the request
        AddActivityRequest request = AddActivityRequest.newBuilder()
                .setReportId(reportId)
                .setDescription(description)
                .setCategory(category)
                .setStartTime(toTimestamp(startTime))
                .setEndTime(toTimestamp(endTime))
                .setProgress(0.0) // Initial progress
                .setStatus(ActivityStatus.ACTIVITY_STATUS_PLANNED)
                .setUsername("example-user")
                .build();

        // Send the request and get the response
        ActivityResponse response = blockingStub.addActivity(request);
        return response.getActivity();
    }

    /**
     * Update activity progress
     */
    public Activity updateActivityProgress(String activityId, double progress) {
        System.out.println("Updating progress for activity " + activityId + " to " + progress + "%");

        // Build the request
        UpdateActivityProgressRequest request = UpdateActivityProgressRequest.newBuilder()
                .setActivityId(activityId)
                .setProgress(progress)
                .setUsername("example-user")
                .build();

        // Send the request and get the response
        ActivityResponse response = blockingStub.updateActivityProgress(request);
        return response.getActivity();
    }

    /**
     * Main method demonstrating the client usage
     */
    public static void main(String[] args) throws Exception {
        // Default values
        String host = "localhost";
        int port = 9090;
        String username = "admin";
        String password = "password";

        // Create the client
        GrpcClientExample client = new GrpcClientExample(host, port, username, password);

        try {
            // Example workflow

            // 1. Create a report
            DailyReport report = client.createReport(
                    "project-123",
                    LocalDate.now(),
                    "Example report created via gRPC"
            );
            System.out.println("Created report with ID: " + report.getId());

            // 2. Add an activity to the report
            Activity activity = client.addActivity(
                    report.getId(),
                    "Example activity",
                    "Development",
                    LocalDateTime.now().minusHours(2),
                    LocalDateTime.now().minusHours(1)
            );
            System.out.println("Added activity with ID: " + activity.getId());

            // 3. Update activity progress
            Activity updatedActivity = client.updateActivityProgress(activity.getId(), 50.0);
            System.out.println("Updated activity progress to: " + updatedActivity.getProgress() + "%");

            // 4. Get all reports for the project
            List<DailyReport> reports = client.getReportsByProject("project-123");
            System.out.println("Found " + reports.size() + " reports for project-123");

        } finally {
            // Shutdown the client
            client.shutdown();
        }
    }

    /**
     * Helper method to convert LocalDateTime to Timestamp
     */
    private static com.google.protobuf.Timestamp toTimestamp(LocalDateTime dateTime) {
        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(dateTime.atZone(ZoneId.systemDefault()).toEpochSecond())
                .setNanos(dateTime.getNano())
                .build();
    }
}