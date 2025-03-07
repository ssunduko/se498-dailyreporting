package com.se498.dailyreporting.config;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.dto.grpc.GrpcMapper;
import com.se498.dailyreporting.grpc.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Standalone gRPC server that doesn't rely on Spring Boot.
 * This implements a simple in-memory version of the service.
 */
public class GrpcStandaloneServer {
    private final int port;
    private final Server server;

    // In-memory storage
    private final Map<String, DailyReport> reports = new ConcurrentHashMap<>();
    private final Map<String, ActivityEntry> activities = new ConcurrentHashMap<>();

    // Mapper for converting between domain and gRPC objects
    private final GrpcMapper mapper = new GrpcMapper();

    /**
     * Create a standalone gRPC server
     */
    public GrpcStandaloneServer(int port) {
        this.port = port;
        HealthStatusManager healthStatusManager = new HealthStatusManager();

        server = ServerBuilder.forPort(port)
                .addService(new DailyReportServiceImpl())
                .addService(healthStatusManager.getHealthService())
                .addService(ProtoReflectionService.newInstance())
                .build();
    }

    /**
     * Start the server
     */
    public void start() throws IOException {
        server.start();
        System.out.println("Server started, listening on port " + port);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server due to JVM shutdown");
            try {
                GrpcStandaloneServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.out.println("Server shut down");
        }));
    }

    /**
     * Stop the server
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main method to start the server
     */
    public static void main(String[] args) throws Exception {
        // Use port 9090 by default
        int port = 9090;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        // Force IPv4
        System.setProperty("java.net.preferIPv4Stack", "true");

        // Create and start the server
        final GrpcStandaloneServer server = new GrpcStandaloneServer(port);
        server.start();

        // Keep the server running
        server.blockUntilShutdown();
    }

    /**
     * Implementation of the DailyReportService
     */
    private class DailyReportServiceImpl extends DailyReportServiceGrpc.DailyReportServiceImplBase {

        @Override
        public void createReport(CreateReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
            try {
                System.out.println("Creating report for project: " + request.getProjectId());

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

                System.out.println("Created report with ID: " + reportId);

            } catch (Exception e) {
                System.err.println("Error creating report: " + e.getMessage());
                e.printStackTrace();
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error creating report: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void getReport(GetReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
            try {
                String reportId = request.getReportId();
                System.out.println("Getting report with ID: " + reportId);

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
                System.err.println("Error getting report: " + e.getMessage());
                e.printStackTrace();
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error getting report: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void addActivity(AddActivityRequest request, StreamObserver<ActivityResponse> responseObserver) {
            try {
                String reportId = request.getReportId();
                System.out.println("Adding activity to report: " + reportId);

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

                System.out.println("Added activity with ID: " + activityId);

            } catch (Exception e) {
                System.err.println("Error adding activity: " + e.getMessage());
                e.printStackTrace();
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error adding activity: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void submitReport(SubmitReportRequest request, StreamObserver<DailyReportResponse> responseObserver) {
            try {
                String reportId = request.getReportId();
                System.out.println("Submitting report with ID: " + reportId);

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

                System.out.println("Submitted report with ID: " + reportId);

            } catch (Exception e) {
                System.err.println("Error submitting report: " + e.getMessage());
                e.printStackTrace();
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
                System.out.println("Getting activities for report: " + reportId);

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
                System.err.println("Error getting activities: " + e.getMessage());
                e.printStackTrace();
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
                System.out.println("Getting progress for report: " + reportId);

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
                System.err.println("Error getting report progress: " + e.getMessage());
                e.printStackTrace();
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
                System.out.println("Checking if report is complete: " + reportId);

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
                System.err.println("Error checking report completion: " + e.getMessage());
                e.printStackTrace();
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error checking report completion: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        // Implement other methods similarly...
        // For brevity, not all methods are implemented here
    }
}