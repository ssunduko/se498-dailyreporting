package com.se498.dailyreporting.config;

import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.service.GrpcService;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Configuration for gRPC server
 */
@Slf4j
@Configuration
public class GrpcConfig {

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    /**
     * Global server interceptor for logging
     */
    @GrpcGlobalServerInterceptor
    @Order(10)
    public ServerInterceptor loggingInterceptor() {
        log.info("Registering gRPC logging interceptor");
        return new LoggingInterceptor();
    }

    /**
     * Configure the gRPC server
     */
    @Bean
    public GrpcServerConfigurer serverConfigurer() {
        return serverBuilder -> {
            log.info("Configuring gRPC server on port {}", grpcPort);
            try {
                // Add ProtoReflectionService for better debugging capabilities
                serverBuilder.addService(ProtoReflectionService.newInstance());
                log.info("Added gRPC reflection service");
            } catch (Exception e) {
                log.error("Failed to add reflection service", e);
            }
        };
    }



    /**
     * Add a health service
     */
    @Bean
    public HealthStatusManager healthStatusManager() {
        log.info("Registering gRPC health service");
        HealthStatusManager healthStatusManager = new HealthStatusManager();
        // Set the service to SERVING status
        healthStatusManager.setStatus("", HealthCheckResponse.ServingStatus.SERVING);
        return healthStatusManager;
    }

    /**
     * Simple logging interceptor for gRPC calls
     */
    private static class LoggingInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> io.grpc.ServerCall.Listener<ReqT> interceptCall(
                io.grpc.ServerCall<ReqT, RespT> call,
                io.grpc.Metadata headers,
                io.grpc.ServerCallHandler<ReqT, RespT> next) {

            log.info("gRPC call: {}", call.getMethodDescriptor().getFullMethodName());

            return next.startCall(new io.grpc.ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                @Override
                public void sendMessage(RespT message) {
                    log.debug("gRPC response sent for {}", call.getMethodDescriptor().getFullMethodName());
                    super.sendMessage(message);
                }

                @Override
                public void close(io.grpc.Status status, io.grpc.Metadata trailers) {
                    log.info("gRPC call completed: {} with status: {}",
                            call.getMethodDescriptor().getFullMethodName(), status.getCode());
                    super.close(status, trailers);
                }
            }, headers);
        }
    }
}