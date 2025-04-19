package com.se498.dailyreporting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for report rejection request in v2 API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRejectionRequest {
    @NotBlank(message = "Rejection reason code is required")
    private String reason;
    private String customDetails;
}