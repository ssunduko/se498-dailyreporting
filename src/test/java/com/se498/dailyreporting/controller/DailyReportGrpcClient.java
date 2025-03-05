package com.se498.dailyreporting.controller;


import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import com.se498.dailyreporting.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLException;
import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Client for accessing the Daily Reporting gRPC service
 * This class is mainly for demonstration purposes and local testing
 */
@Slf4j
@Component
@Profile("!test") // Don't instantiate during tests
public class DailyReportGrpcClient {

    private ManagedChannel channel;
    private DailyReportingServiceGrpc.DailyReportingServiceBlockingStub blockingStub;

    @Value("${grpc.client.daily-reporting-service.address:static://localhost:9090}")
    private String serverAddress;

    @Value("${grpc.client.daily-reporting-service.enable-tls:false}")
    private boolean enableTls;

    @Value("${grpc.client.daily-reporting-service.trustCertificate:}")
    private String trustCertificate;

    @Value("${spring.security.user.name:sergey}")
    private String username;

    @Value("${spring.security.user.password:chapman}")
    private String password;

    /**
     * Initialize the gRPC channel after bean creation
     */
    @PostConstruct
    public void init() throws SSLException {
        String host = "localhost";
        int port = 9090;

        // Parse server address from the format "static://host:port"
        if (serverAddress.startsWith("static://")) {
            String address = serverAddress.substring(9);
            String[] parts = address.split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        }

        log.info("Initializing gRPC client to connect to {}:{}", host, port);

        // Configure channel based on TLS setting
        if (enableTls && trustCertificate != null && !trustCertificate.isEmpty()) {
            channel = NettyChannelBuilder.forAddress(host, port)
                    .sslContext(GrpcSslContexts.forClient()
                            .trustManager(new File(trustCertificate))
                            .build())
                    .build();
            log.info("gRPC client configured with TLS");
        } else {
            channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();
            log.info("gRPC client configured without TLS (plaintext)");
        }

        // Create the stub with authentication headers
        blockingStub = DailyReportingServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Shutdown the gRPC channel gracefully
     */
    @PreDestroy
    public void shutdown() {
        try {
            log.info("Shutting down gRPC client");
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Error shutting down gRPC client", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Create a new daily report
     */
    public DailyReport createReport(String projectId, LocalDate reportDate, String notes) {
        log.info("Creating report for project {} on {}", projectId, reportDate);

        try {
            CreateReportRequest.Builder requestBuilder = CreateReportRequest.newBuilder()
                    .setProjectId(projectId)
                    .setReportDate(toDateProto(reportDate))
                    .setUsername(username);

            if (notes != null && !notes.isEmpty()) {
                requestBuilder.setNotes(StringValue.of(notes));
            }

            DailyReportResponse response = blockingStub.createReport(requestBuilder.build());
            return response.getReport();
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Get a report by ID
     */
    public DailyReport getReport(String reportId) {
        log.info("Getting report {}", reportId);

        try {
            GetReportRequest request = GetReportRequest.newBuilder()
                    .setReportId(reportId)
                    .build();

            DailyReportResponse response = blockingStub.getReport(request);
            return response.getReport();
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Get reports by project ID
     */
    public List<DailyReport> getReportsByProject(String projectId) {
        log.info("Getting reports for project {}", projectId);

        try {
            GetReportsByProjectRequest request = GetReportsByProjectRequest.newBuilder()
                    .setProjectId(projectId)
                    .build();

            GetReportsByProjectResponse response = blockingStub.getReportsByProject(request);
            return response.getReportsList();
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Get reports by status
     */
    public List<DailyReport> getReportsByStatus(ReportStatus status) {
        log.info("Getting reports with status {}", status);

        try {
            GetReportsByStatusRequest request = GetReportsByStatusRequest.newBuilder()
                    .setStatus(status)
                    .build();

            GetReportsByStatusResponse response = blockingStub.getReportsByStatus(request);
            return response.getReportsList();
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Add activity to a report
     */
    public Activity addActivity(String reportId, String description, String category,
                                LocalDateTime startTime, LocalDateTime endTime,
                                double progress, ActivityStatus status) {
        log.info("Adding activity to report {}", reportId);

        try {
            AddActivityRequest.Builder requestBuilder = AddActivityRequest.newBuilder()
                    .setReportId(reportId)
                    .setDescription(description)
                    .setCategory(category)
                    .setStartTime(toTimestamp(startTime))
                    .setEndTime(toTimestamp(endTime))
                    .setProgress(progress)
                    .setStatus(status)
                    .setUsername(username);

            ActivityResponse response = blockingStub.addActivity(requestBuilder.build());
            return response.getActivity();
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Get report progress
     */
    public double getReportProgress(String reportId) {
        log.info("Getting progress for report {}", reportId);

        try {
            GetReportProgressRequest request = GetReportProgressRequest.newBuilder()
                    .setReportId(reportId)
                    .build();

            GetReportProgressResponse response = blockingStub.getReportProgress(request);
            return response.getProgress();
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Check if report is complete
     */
    public boolean isReportComplete(String reportId) {
        log.info("Checking if report {} is complete", reportId);

        try {
            IsReportCompleteRequest request = IsReportCompleteRequest.newBuilder()
                    .setReportId(reportId)
                    .build();

            IsReportCompleteResponse response = blockingStub.isReportComplete(request);
            return response.getIsComplete();
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Get total activity duration for a report
     */
    public long getTotalDuration(String reportId) {
        log.info("Getting total duration for report {}", reportId);

        try {
            GetTotalDurationRequest request = GetTotalDurationRequest.newBuilder()
                    .setReportId(reportId)
                    .build();

            GetTotalDurationResponse response = blockingStub.getTotalDuration(request);
            return response.getDurationMinutes();
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getStatus());
            throw e;
        }
    }

    /**
     * Helper method to convert LocalDate to Date proto
     */
    private com.se498.dailyreporting.grpc.Date toDateProto(LocalDate date) {
        return com.se498.dailyreporting.grpc.Date.newBuilder()
                .setYear(date.getYear())
                .setMonth(date.getMonthValue())
                .setDay(date.getDayOfMonth())
                .build();
    }

    /**
     * Helper method to convert LocalDateTime to Timestamp proto
     */
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
}