package com.se498.dailyreporting.exception;

/**
 * Exception thrown when there are issues with the weather API
 */
public class WeatherApiException extends RuntimeException {

    private final String provider;
    private final int statusCode;

    public WeatherApiException(String message) {
        super(message);
        this.provider = "unknown";
        this.statusCode = 0;
    }

    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
        this.provider = "unknown";
        this.statusCode = 0;
    }

    public WeatherApiException(String message, String provider) {
        super(message);
        this.provider = provider;
        this.statusCode = 0;
    }

    public WeatherApiException(String message, String provider, int statusCode) {
        super(message);
        this.provider = provider;
        this.statusCode = statusCode;
    }

    public WeatherApiException(String message, String provider, int statusCode, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.statusCode = statusCode;
    }

    /**
     * Get the API provider that threw this exception
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Get the HTTP status code if applicable
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Check if this is a client error (4xx)
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * Check if this is a server error (5xx)
     */
    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }
}
