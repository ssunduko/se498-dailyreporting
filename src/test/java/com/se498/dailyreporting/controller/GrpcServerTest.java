package com.se498.dailyreporting.controller;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Simple test client to verify if the gRPC server is running and healthy
 */
@Slf4j
public class GrpcServerTest {

    public static void main(String[] args) throws Exception {
        // Force IPv4
        System.setProperty("java.net.preferIPv4Stack", "true");

        // Default settings
        String host = "127.0.0.1"; // Using explicit IPv4 address instead of localhost
        int port = 9090;

        // Parse arguments
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        log.info("Testing gRPC server at {}:{}", host, port);

        // Create a channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        try {
            // Create health check stub
            HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);

            // Perform health check
            log.info("Sending health check request...");
            HealthCheckResponse response = healthStub.check(HealthCheckRequest.newBuilder().build());

            log.info("Health check response: {}", response.getStatus());

            if (response.getStatus() == HealthCheckResponse.ServingStatus.SERVING) {
                log.info("gRPC server is HEALTHY!");
            } else {
                log.warn("gRPC server is NOT HEALTHY: {}", response.getStatus());
            }

        } catch (Exception e) {
            log.error("Failed to connect to gRPC server: {}", e.getMessage(), e);
        } finally {
            // Shutdown channel
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}