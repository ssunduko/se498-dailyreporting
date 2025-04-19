package com.se498.dailyreporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for rejection reason response in v2 API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectionReasonResponse {
    private String code;
    private String description;
}