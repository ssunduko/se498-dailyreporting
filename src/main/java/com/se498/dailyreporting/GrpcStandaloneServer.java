package com.se498.dailyreporting;

import com.se498.dailyreporting.config.GrpcServerConfig;
import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.dto.grpc.GrpcMapper;
import com.se498.dailyreporting.grpc.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * gRPC server that integrates with Spring Boot lifecycle
 */
@Slf4j
@Component
public class GrpcStandaloneServer implements InitializingBean, DisposableBean {

    private final GrpcServerConfig config;
    private Server server;

    public GrpcStandaloneServer(GrpcServerConfig config) {
        this.config = config;
    }

    // In-memory storage
    private final Map<String, DailyReport> reports = new ConcurrentHashMap<>();
    private final Map<String, ActivityEntry> activities = new ConcurrentHashMap<>();

    // Mapper for converting between domain and gRPC objects
    private final GrpcMapper mapper = new GrpcMapper();

    /**
     * Initialize the gRPC server when the Spring application starts
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initializing gRPC server on port {}", config.getPort());
        try {
            // Disable Epoll native transport on non-Linux platforms
            if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) {
                log.info("Non-Linux OS detected. Disabling Netty's native transport.");
                System.setProperty("io.grpc.netty.shaded.io.netty.transport.noNative", "true");
            }

            start();
        } catch (Exception e) {
            log.error("Failed to start gRPC server: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Clean up the gRPC server when the Spring application stops
     */
    @Override
    public void destroy() throws Exception {
        log.info("Shutting down gRPC server");
        stop();
    }

    /**
     * Start the server
     */
    public void start() throws IOException {
        if (!config.isEnabled()) {
            log.info("gRPC server is disabled. Not starting.");
            return;
        }

        HealthStatusManager healthStatusManager = new HealthStatusManager();

        // Configure native transport based on configuration
        if (!config.isUseNativeTransport()) {
            log.info("Disabling Netty's native transport per configuration");
            System.setProperty("io.grpc.netty.shaded.io.netty.transport.noNative", "true");
        }

        server = ServerBuilder.forPort(config.getPort())
                .addService(new DailyReportServiceImpl())
                .addService(healthStatusManager.getHealthService())
                .addService(ProtoReflectionService.newInstance())
                .maxInboundMessageSize(config.getMaxInboundMessageSize())
                .build();

        try {
            server.start();
            log.info("gRPC Server started successfully, listening on port {}", config.getPort());

            // Add shutdown hook as a backup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down gRPC server due to JVM shutdown");
                try {
                    GrpcStandaloneServer.this.stop();
                } catch (InterruptedException e) {
                    log.error("Error shutting down gRPC server", e);
                }
                log.info("gRPC Server shut down");
            }));
        } catch (IOException e) {
            log.error("Failed to start gRPC server: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Stop the server
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            log.info("Gracefully shutting down gRPC server...");
            server.shutdown().awaitTermination(config.getShutdownGraceSeconds(), TimeUnit.SECONDS);
            log.info("gRPC server shutdown completed");
        }
    }

    /**
     * Implementation of the DailyReportService
     */
    private class DailyReportServiceImpl extends DailyReportServiceGrpc.DailyReportServiceImplBase {

        @Override
        public void createReport(CreateReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
            try {
                log.debug("Creating report for project: {}", request.getProjectId());

                // Create a new report
                String reportId = UUID.randomUUID().toString();
                LocalDate reportDate = mapper.fromGrpcDate(request.getReportDate());

                DailyReport report = new DailyReport();
                report.setId(reportId);
                report.setProjectId(request.getProjectId());
                report.setReportDate(reportDate);
                report.setStatus(ReportStatus.DRAFT);
                if (request.hasNotes()) {
                    report.setNotes(request.getNotes().getValue());
                }
                report.setCreatedBy(request.getUsername());
                report.setCreatedAt(LocalDateTime.now());
                report.setActivities(new ArrayList<>());

                // Save the report
                reports.put(reportId, report);

                // Generate response
                DailyReportResponse response = mapper.toGrpcResponse(report);
                responseObserver.onNext(response);
                responseObserver.onCompleted();

                log.debug("Created report with ID: {}", reportId);

            } catch (Exception e) {
                log.error("Error creating report: {}", e.getMessage(), e);
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error creating report: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void getReport(GetReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
            try {
                String reportId = request.getReportId();
                log.debug("Getting report with ID: {}", reportId);

                // Find the report
                DailyReport report = reports.get(reportId);
                if (report == null) {
                    responseObserver.onError(io.grpc.Status.NOT_FOUND
                            .withDescription("Report not found with ID: " + reportId)
                            .asRuntimeException());
                    return;
                }

                // Generate response
                DailyReportResponse response = mapper.toGrpcResponse(report);
                responseObserver.onNext(response);
                responseObserver.onCompleted();

            } catch (Exception e) {
                log.error("Error getting report: {}", e.getMessage(), e);
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error getting report: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void addActivity(AddActivityRequest request, StreamObserver<ActivityResponse> responseObserver) {
            try {
                String reportId = request.getReportId();
                log.debug("Adding activity to report: {}", reportId);

                // Find the report
                DailyReport report = reports.get(reportId);
                if (report == null) {
                    responseObserver.onError(io.grpc.Status.NOT_FOUND
                            .withDescription("Report not found with ID: " + reportId)
                            .asRuntimeException());
                    return;
                }

                // Create a new activity
                String activityId = UUID.randomUUID().toString();
                ActivityEntry activity = new ActivityEntry();
                activity.setId(activityId);
                activity.setReportId(reportId);
                activity.setDescription(request.getDescription());
                activity.setCategory(request.getCategory());
                activity.setStartTime(mapper.fromGrpcTimestamp(request.getStartTime()));
                activity.setEndTime(mapper.fromGrpcTimestamp(request.getEndTime()));
                activity.setProgress(request.getProgress());
                activity.setStatus(mapper.fromGrpcActivityStatus(request.getStatus()));
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

                // Save the activity
                activities.put(activityId, activity);

                // Add to the report
                report.getActivities().add(activity);

                // Generate response
                ActivityResponse response = mapper.toGrpcActivity(activity);
                responseObserver.onNext(response);
                responseObserver.onCompleted();

                log.debug("Added activity with ID: {}", activityId);

            } catch (Exception e) {
                log.error("Error adding activity: {}", e.getMessage(), e);
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error adding activity: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void submitReport(SubmitReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
            try {
                String reportId = request.getReportId();
                log.debug("Submitting report with ID: {}", reportId);

                // Find the report
                DailyReport report = reports.get(reportId);
                if (report == null) {
                    responseObserver.onError(io.grpc.Status.NOT_FOUND
                            .withDescription("Report not found with ID: " + reportId)
                            .asRuntimeException());
                    return;
                }

                // Update status
                report.setStatus(ReportStatus.SUBMITTED);
                report.setUpdatedBy(request.getUsername());
                report.setUpdatedAt(LocalDateTime.now());

                // Generate response
                DailyReportResponse response = mapper.toGrpcResponse(report);
                responseObserver.onNext(response);
                responseObserver.onCompleted();

                log.debug("Submitted report with ID: {}", reportId);

            } catch (Exception e) {
                log.error("Error submitting report: {}", e.getMessage(), e);
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error submitting report: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void getActivitiesByReport(GetActivitiesByReportRequest request,
                                          StreamObserver<ActivityListResponse> responseObserver) {
            try {
                String reportId = request.getReportId();
                log.debug("Getting activities for report: {}", reportId);

                // Find the report
                DailyReport report = reports.get(reportId);
                if (report == null) {
                    responseObserver.onError(io.grpc.Status.NOT_FOUND
                            .withDescription("Report not found with ID: " + reportId)
                            .asRuntimeException());
                    return;
                }

                // Get activities
                List<ActivityEntry> activityList = report.getActivities();

                // Build response
                ActivityListResponse.Builder responseBuilder = ActivityListResponse.newBuilder();
                for (ActivityEntry activity : activityList) {
                    responseBuilder.addActivities(mapper.toGrpcActivity(activity));
                }

                // Send response
                responseObserver.onNext(responseBuilder.build());
                responseObserver.onCompleted();

            } catch (Exception e) {
                log.error("Error getting activities: {}", e.getMessage(), e);
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error getting activities: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void getReportProgress(GetReportProgressRequest request,
                                      StreamObserver<DoubleResponse> responseObserver) {
            try {
                String reportId = request.getReportId();
                log.debug("Getting progress for report: {}", reportId);

                // Find the report
                DailyReport report = reports.get(reportId);
                if (report == null) {
                    responseObserver.onError(io.grpc.Status.NOT_FOUND
                            .withDescription("Report not found with ID: " + reportId)
                            .asRuntimeException());
                    return;
                }

                // Calculate progress
                double progress = report.calculateProgress();

                // Send response
                DoubleResponse response = DoubleResponse.newBuilder()
                        .setValue(progress)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();

            } catch (Exception e) {
                log.error("Error getting report progress: {}", e.getMessage(), e);
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error getting report progress: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void isReportComplete(IsReportCompleteRequest request,
                                     StreamObserver<BooleanResponse> responseObserver) {
            try {
                String reportId = request.getReportId();
                log.debug("Checking if report is complete: {}", reportId);

                // Find the report
                DailyReport report = reports.get(reportId);
                if (report == null) {
                    responseObserver.onError(io.grpc.Status.NOT_FOUND
                            .withDescription("Report not found with ID: " + reportId)
                            .asRuntimeException());
                    return;
                }

                // Check if complete
                boolean isComplete = report.isComplete();

                // Send response
                BooleanResponse response = BooleanResponse.newBuilder()
                        .setValue(isComplete)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();

            } catch (Exception e) {
                log.error("Error checking report completion: {}", e.getMessage(), e);
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error checking report completion: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        // Implement other methods similarly...
        // For brevity, not all methods are implemented here
    }
}