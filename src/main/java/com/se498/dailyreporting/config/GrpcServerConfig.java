package com.se498.dailyreporting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;
import org.springframework.context.annotation.Profile;

/**
 * Configuration properties for the gRPC server
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "grpc.server")
public class GrpcServerConfig {
    /**
     * Port on which the gRPC server will listen
     */
    private int port = 9090;

    /**
     * Enable/disable the gRPC server
     */
    private boolean enabled = true;

    /**
     * Maximum inbound message size in bytes (default 20MB)
     */
    private int maxInboundMessageSize = 20971520;

    /**
     * Whether to use native transport (Epoll on Linux, KQueue on macOS)
     * Set to false to force JDK transport
     */
    private boolean useNativeTransport = false;

    /**
     * Shutdown grace period in seconds
     */
    private int shutdownGraceSeconds = 30;
}