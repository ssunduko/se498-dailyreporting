package com.se498.dailyreporting.config;

import com.se498.dailyreporting.controller.DailyReportGrpcController;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for gRPC server that explicitly uses NIO instead of Epoll
 * to avoid ExceptionInInitializerError on non-Linux platforms
 */
@Slf4j
@Configuration
public class GrpcServerConfig {

    @Value("${grpc.server.port:9090}")
    private int port;

    @Autowired
    private DailyReportGrpcController grpcController;

    private Server server;

    @Bean
    public Server grpcServer() throws IOException {
        log.info("Starting gRPC server on port {} with explicit NIO configuration", port);

        // Create boss event loop for accepting connections
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);

        // Create worker event loop for processing requests
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        // Build the server with NIO transport explicitly configured
        server = NettyServerBuilder.forPort(port)
                .addService(grpcController)
                .channelType(NioServerSocketChannel.class)  // Explicitly use NIO
                .bossEventLoopGroup(bossGroup)
                .workerEventLoopGroup(workerGroup)
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .permitKeepAliveWithoutCalls(true)
                .build();

        // Start the server
        server.start();

        log.info("gRPC server started successfully on port {}", port);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server via JVM shutdown hook");
            try {
                GrpcServerConfig.this.stopServer();
            } catch (InterruptedException e) {
                log.error("Error shutting down gRPC server", e);
            }
        }));

        return server;
    }

    @PreDestroy
    public void stopServer() throws InterruptedException {
        if (server != null) {
            log.info("Initiating graceful shutdown of gRPC server...");
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            log.info("gRPC server shutdown complete");
        }
    }
}