package com.se498.dailyreporting.controller;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;

/**
 * Simple test to verify gRPC server connectivity
 * Run this after starting your Spring Boot application
 */
public class GrpcConnectionTest {

    public static void main(String[] args) {
        // Test multiple ports
        testGrpcConnection("localhost", 9090);
        testGrpcConnection("localhost", 9091);
        testGrpcConnection("127.0.0.1", 9090);
    }

    private static void testGrpcConnection(String host, int port) {
        System.out.println("\nTesting gRPC connection to " + host + ":" + port);
        ManagedChannel channel = null;

        try {
            // Create a channel - just test if connection can be established
            channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();

            // Try to get connection state - will fail fast if server isn't listening
            boolean isConnected = !channel.isShutdown() && !channel.isTerminated();
            System.out.println("Channel created successfully. Connected: " + isConnected);
            System.out.println("Connection test PASSED for " + host + ":" + port);

        } catch (StatusRuntimeException e) {
            System.out.println("gRPC call failed: " + e.getStatus());
            System.out.println("Connection test FAILED for " + host + ":" + port);
        } catch (Exception e) {
            System.out.println("Connection test FAILED for " + host + ":" + port);
            System.out.println("Error: " + e.getMessage());
        } finally {
            // Clean shutdown
            if (channel != null) {
                try {
                    channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}