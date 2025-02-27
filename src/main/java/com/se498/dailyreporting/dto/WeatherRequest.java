package com.se498.dailyreporting.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for weather data requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Weather request parameters")
public class WeatherRequest {

    @NotBlank(message = "City name is required")
    @Schema(description = "City name", example = "New York", required = true)
    private String city;

    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be a valid 2-letter ISO code")
    @Schema(description = "Country code (ISO 3166 alpha-2)", example = "US", defaultValue = "US")
    private String country = "US";

    @Schema(description = "State or province (optional)", example = "NY")
    private String stateOrProvince;

    @Schema(description = "Whether to include weather alerts in response", defaultValue = "false")
    private boolean includeAlerts;

    @Schema(description = "Use cache even if data is stale", defaultValue = "false")
    private boolean useCache;

    @Schema(description = "Temperature unit preference", allowableValues = {"F", "C"}, defaultValue = "F")
    private String unit = "F";

    @Schema(description = "Zip code (if available)", example = "10001")
    private String zipCode;
}
