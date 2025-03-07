package com.se498.dailyreporting.controller;


import com.se498.dailyreporting.grpc.GetReportsByStatusRequest;
import com.se498.dailyreporting.grpc.GetReportsByStatusResponse;
import com.se498.dailyreporting.grpc.DailyReportingServiceGrpc;
import com.se498.dailyreporting.grpc.ReportStatus;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

/**
 * A simple client to test if the gRPC server is running.
 * This standalone class can be run directly to check connectivity.
 */
public class GrpcHealthCheckClient {
    private final ManagedChannel channel;
    private final DailyReportingServiceGrpc.DailyReportingServiceBlockingStub blockingStub;

    public static void main(String[] args) throws Exception {
        // Get host and port from args or use defaults
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9090;

        System.out.println("Testing gRPC connection to " + host + ":" + port);

        // Create and run the client
        GrpcHealthCheckClient client = new GrpcHealthCheckClient(host, port);
        try {
            client.checkConnection();
        } finally {
            client.shutdown();
        }
    }

    /** Create a client using the specified host and port */
    public GrpcHealthCheckClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()  // Disable TLS for simplicity
                .build();
        this.blockingStub = DailyReportingServiceGrpc.newBlockingStub(channel);
        System.out.println("Client initialized");
    }

    /** Shut down the channel */
    public void shutdown() throws InterruptedException {
        System.out.println("Shutting down channel");
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /** Check if the server is running by making a simple query */
    public void checkConnection() {
        System.out.println("Checking server connection...");

        try {
            // Try to get a list of reports by status (DRAFT) as a simple test
            GetReportsByStatusRequest request = GetReportsByStatusRequest.newBuilder()
                    .setStatus(ReportStatus.REPORT_STATUS_DRAFT)
                    .build();

            System.out.println("Sending test request...");
            GetReportsByStatusResponse response = blockingStub.getReportsByStatus(request);

            // Successfully received a response
            System.out.println("✅ Connection successful!");
            System.out.println("Server is running properly");
            System.out.println("Received response with " + response.getReportsCount() + " reports");

        } catch (StatusRuntimeException e) {
            System.err.println("❌ RPC failed: " + e.getStatus());
            System.err.println("Error details: " + e.getMessage());
            if (e.getMessage().contains("UNAVAILABLE")) {
                System.err.println("The server appears to be unavailable. Make sure it's running on the specified host and port.");
            } else if (e.getMessage().contains("UNIMPLEMENTED")) {
                System.err.println("The method is not implemented on the server. This suggests the server is running but might have API compatibility issues.");
            }
            throw e;
        }
    }
}