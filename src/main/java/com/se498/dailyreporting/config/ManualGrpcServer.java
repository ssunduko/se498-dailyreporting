package com.se498.dailyreporting.config;

import com.se498.dailyreporting.controller.DailyReportGrpcController;
import com.se498.dailyreporting.dto.grpc.GrpcMapper;
import com.se498.dailyreporting.service.DailyReportingService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Manual gRPC server setup in case Spring auto-configuration isn't working
 * Only active when profile "manual-grpc" is enabled
 */
@Slf4j
@Component
@Profile("manual-grpc")
@RequiredArgsConstructor
public class ManualGrpcServer implements CommandLineRunner {

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    @Autowired
    private final DailyReportingService reportingService;

    @Autowired
    private final GrpcMapper mapper;

    private Server server;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting manual gRPC server on port {}", grpcPort);

        // Create health status manager
        HealthStatusManager healthStatusManager = new HealthStatusManager();

        // Create gRPC service implementation
        DailyReportGrpcController service = new DailyReportGrpcController(reportingService, mapper);

        // Create and start the server
        server = ServerBuilder.forPort(grpcPort)
                .addService(service)
                .addService(healthStatusManager.getHealthService())
                .addService(ProtoReflectionService.newInstance())
                .build();

        server.start();

        log.info("Manual gRPC server started successfully on port {}", grpcPort);

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down manual gRPC server");
            if (server != null) {
                server.shutdown();
            }
        }));
    }
}