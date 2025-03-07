package com.se498.dailyreporting.client;


import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server interceptor that logs gRPC method calls, request metadata, and timing
 * information. Used to debug gRPC communication issues.
 */
@Slf4j
@Component
public class LoggingInterceptor implements ServerInterceptor {

    private static final AtomicLong CALL_COUNTER = new AtomicLong(0);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Generate unique call ID for tracing
        long callId = CALL_COUNTER.incrementAndGet();
        String methodName = call.getMethodDescriptor().getFullMethodName();
        long startTime = System.nanoTime();

        // Log the incoming call with its metadata
        log.info("gRPC CALL #{} STARTED: Method: {}", callId, methodName);
        logMetadata(headers, callId);

        // Wrap the server call to intercept responses
        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void sendMessage(RespT message) {
                log.debug("gRPC CALL #{} RESPONSE: {}", callId, message);
                super.sendMessage(message);
            }

            @Override
            public void close(Status status, Metadata trailers) {
                long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

                if (status.isOk()) {
                    log.info("gRPC CALL #{} COMPLETED: Method: {}, Duration: {}ms",
                            callId, methodName, duration);
                } else {
                    log.warn("gRPC CALL #{} FAILED: Method: {}, Status: {}, Duration: {}ms",
                            callId, methodName, status.getCode(), duration);

                    if (status.getDescription() != null) {
                        log.warn("gRPC CALL #{} ERROR DESCRIPTION: {}", callId, status.getDescription());
                    }

                    if (status.getCause() != null) {
                        log.warn("gRPC CALL #{} ERROR CAUSE:", callId, status.getCause());
                    }
                }

                super.close(status, trailers);
            }
        };

        // Create a listener that logs requests
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
                next.startCall(wrappedCall, headers)) {

            @Override
            public void onMessage(ReqT message) {
                log.debug("gRPC CALL #{} REQUEST: {}", callId, message);
                super.onMessage(message);
            }

            @Override
            public void onCancel() {
                log.warn("gRPC CALL #{} CANCELLED: Method: {}", callId, methodName);
                super.onCancel();
            }

            @Override
            public void onComplete() {
                log.debug("gRPC CALL #{} REQUEST COMPLETE: Client finished sending messages", callId);
                super.onComplete();
            }

            @Override
            public void onHalfClose() {
                log.debug("gRPC CALL #{} HALF-CLOSE: Client completed requests", callId);
                super.onHalfClose();
            }

            @Override
            public void onReady() {
                log.debug("gRPC CALL #{} READY", callId);
                super.onReady();
            }
        };
    }

    /**
     * Logs important metadata from the headers
     */
    private void logMetadata(Metadata headers, long callId) {
        if (log.isDebugEnabled()) {
            StringBuilder metadataLog = new StringBuilder();
            metadataLog.append("gRPC CALL #").append(callId).append(" METADATA:");

            headers.keys().forEach(key -> {
                if (!key.endsWith("-bin")) { // Skip binary headers
                    String value = headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
                    // Mask authorization headers to avoid logging credentials
                    if (key.toLowerCase().contains("auth")) {
                        value = "******";
                    }
                    metadataLog.append("\n  ").append(key).append(": ").append(value);
                }
            });

            log.debug(metadataLog.toString());
        }
    }
}
