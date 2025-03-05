package com.se498.dailyreporting.client;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * gRPC client interceptor for adding Basic Authentication to requests
 */
@Slf4j
public class AuthenticationInterceptor implements ClientInterceptor {

    private final String username;
    private final String password;

    public AuthenticationInterceptor(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Add basic authentication header
                addBasicAuthHeader(headers);

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        if (!status.isOk()) {
                            log.warn("gRPC call failed: {}, method: {}", status, method.getFullMethodName());
                        }
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }

    /**
     * Adds a Basic Authentication header to the request metadata
     */
    private void addBasicAuthHeader(Metadata headers) {
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        // Create and add the Basic auth header
        Metadata.Key<String> authKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(authKey, "Basic " + encodedAuth);
    }
}