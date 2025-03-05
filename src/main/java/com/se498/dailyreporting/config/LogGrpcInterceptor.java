package com.se498.dailyreporting.config;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Interceptor for logging gRPC calls
 */
@Slf4j
public class LogGrpcInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {

        String methodName = serverCall.getMethodDescriptor().getFullMethodName();
        String remoteAddress = extractRemoteAddress(serverCall);

        // Get the authenticated user (if any)
        final String username = "anonymous";
        /*Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
        }*/

        log.info("gRPC call received - Method: {}, Client: {}, User: {}", methodName, remoteAddress, username);

        // Create a wrapper for the server call to log the completion status
        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(serverCall) {
            @Override
            public void close(Status status, Metadata trailers) {
                log.info("gRPC call completed - Method: {}, Status: {}, User: {}",
                        methodName, status.getCode(), username);

                if (!status.isOk()) {
                    log.warn("gRPC call failed - Method: {}, Status: {}, Description: {}, User: {}",
                            methodName, status.getCode(), status.getDescription(), username);
                }

                super.close(status, trailers);
            }
        };

        // Create the actual listener and continue with the call processing
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
                serverCallHandler.startCall(wrappedCall, metadata)) {

            @Override
            public void onMessage(ReqT message) {
                log.debug("gRPC message received - Method: {}, Message type: {}",
                        methodName, message.getClass().getName());
                super.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                log.debug("gRPC client finished sending messages - Method: {}", methodName);
                super.onHalfClose();
            }

            @Override
            public void onCancel() {
                log.info("gRPC call cancelled - Method: {}, User: {}", methodName, username);
                super.onCancel();
            }

            @Override
            public void onComplete() {
                log.debug("gRPC call listener completed - Method: {}", methodName);
                super.onComplete();
            }
        };
    }

    /**
     * Extract the remote address from the server call
     * This is an implementation detail and may vary depending on the gRPC implementation
     */
    private String extractRemoteAddress(ServerCall<?, ?> serverCall) {
        try {
            // Try to extract remote peer from call attributes
            Attributes attributes = serverCall.getAttributes();
            if (attributes != null) {
                Object remoteAddr = attributes.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
                if (remoteAddr != null) {
                    return remoteAddr.toString();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract remote address", e);
        }
        return "unknown";
    }
}
