package com.se498.dailyreporting.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
@Component
public class GrpcServerStartupChecker implements CommandLineRunner {

    @Value("${grpc.server.port:9090}")
    private int serverPort;

    private final ApplicationContext context;

    public GrpcServerStartupChecker(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(String... args) {
        log.info("======================================");
        log.info("Checking gRPC server configuration...");
        log.info("gRPC server port: {}", serverPort);

        try {
            // Try to get the server bean from context
            String[] grpcBeans = context.getBeanNamesForAnnotation(net.devh.boot.grpc.server.service.GrpcService.class);
            log.info("Found {} gRPC service implementations:", grpcBeans.length);
            for (String bean : grpcBeans) {
                log.info("  - {}", bean);
            }

            // Check if port is actually listening
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", serverPort), 1000);
                log.info("Port {} is OPEN - gRPC server is listening!", serverPort);
            } catch (Exception e) {
                log.error("PORT {} IS NOT LISTENING! gRPC server failed to start.", serverPort);
            }
        } catch (Exception e) {
            log.error("Error checking gRPC server: {}", e.getMessage(), e);
        }
        log.info("======================================");
    }
}