package com.se498.dailyreporting.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Simple validator that logs when the gRPC server starts
 */
@Slf4j
@Component
public class GrpcServerStartupValidator implements CommandLineRunner {

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    @Value("${grpc.server.enabled:true}")
    private boolean grpcEnabled;

    @Override
    public void run(String... args) {
        log.info("---------------------------------------");
        if (grpcEnabled) {
            log.info("gRPC Server enabled and starting on port: {}", grpcPort);
        } else {
            log.warn("gRPC Server is DISABLED in configuration!");
        }
        log.info("---------------------------------------");
    }
}