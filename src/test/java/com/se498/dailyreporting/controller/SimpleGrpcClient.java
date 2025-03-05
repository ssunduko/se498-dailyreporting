package com.se498.dailyreporting.controller;

import com.google.protobuf.StringValue;
import com.se498.dailyreporting.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SimpleGrpcClient {

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 9090;

        log.info("Starting simple gRPC client to {}:{}", host, port);

        // Create channel without any authentication
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        try {
            // Create stub
            DailyReportingServiceGrpc.DailyReportingServiceBlockingStub stub =
                    DailyReportingServiceGrpc.newBlockingStub(channel);

            // Try a simple call first
            log.info("Trying a simple call...");
            try {
                GetReportsByProjectRequest testRequest = GetReportsByProjectRequest.newBuilder()
                        .setProjectId("test-project")
                        .build();
                stub.getReportsByProject(testRequest);
                log.info("Simple call succeeded!");
            } catch (Exception e) {
                log.error("Simple call failed: {}", e.getMessage());
            }

            // Now try to create a report
            log.info("Creating a report...");
            LocalDate today = LocalDate.now();
            Date dateProto = Date.newBuilder()
                    .setYear(today.getYear())
                    .setMonth(today.getMonthValue())
                    .setDay(today.getDayOfMonth())
                    .build();

            CreateReportRequest request = CreateReportRequest.newBuilder()
                    .setProjectId("simple-test")
                    .setReportDate(dateProto)
                    .setUsername("test-user")
                    .setNotes(StringValue.of("Test report"))
                    .build();

            DailyReportResponse response = stub.createReport(request);
            log.info("Report created with ID: {}", response.getReport().getId());

        } finally {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}