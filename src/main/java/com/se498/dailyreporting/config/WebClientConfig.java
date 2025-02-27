package com.se498.dailyreporting.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * WebClient configuration for external API calls
 */
@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${service.connection.timeout:5000}")
    private int connectionTimeout;

    @Value("${service.read.timeout:10000}")
    private int readTimeout;

    @Value("${service.max-connections:100}")
    private int maxConnections;

    @Value("${service.max-idle-time:30}")
    private int maxIdleTime;

    @Value("${service.max-memory-size:16777216}") // Default 16MB
    private int maxMemorySize;

    /**
     * Configures and creates WebClient bean
     */
    @Bean
    public WebClient webClient() {
        // Configure connection provider with connection pooling
        ConnectionProvider provider = ConnectionProvider.builder("weather-api-connection-pool")
                .maxConnections(maxConnections)
                .maxIdleTime(Duration.ofSeconds(maxIdleTime))
                .build();

        // Configure HTTP client with timeouts
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS)));

        // Configure exchange strategies for memory limits
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxMemorySize))
                .build();

        // Build WebClient
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    /**
     * Creates a filter function to log requests
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            String maskedUrl = clientRequest.url().toString();

            // Hide API keys in logs
            if (maskedUrl.contains("appid=")) {
                maskedUrl = maskedUrl.replaceAll("appid=[^&]*", "appid=***");
            }

            log.debug("Request: {} {}", clientRequest.method(), maskedUrl);
            return Mono.just(clientRequest);
        });
    }

    /**
     * Creates a filter function to log responses
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}