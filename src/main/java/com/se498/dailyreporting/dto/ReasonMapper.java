package com.se498.dailyreporting.dto;


import com.se498.dailyreporting.domain.vo.RejectionReason;
import com.se498.dailyreporting.dto.RejectionReasonResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between RejectionReason value objects and DTOs
 */
@Component
public class ReasonMapper {

    /**
     * Convert a RejectionReason enum to a RejectionReasonResponse DTO
     * @param reason The rejection reason enum
     * @return The response DTO
     */
    public RejectionReasonResponse toResponseDto(RejectionReason reason) {
        return new RejectionReasonResponse(reason.name(), reason.getDescription());
    }

    /**
     * Get a list of all available rejection reasons as DTOs
     * @return List of rejection reason response DTOs
     */
    public List<RejectionReasonResponse> getAllReasonsAsDto() {
        return Arrays.stream(RejectionReason.values())
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Find a RejectionReason by its code (enum name)
     * @param code The code to look up
     * @return Optional containing the matching RejectionReason, or empty if not found
     */
    public Optional<RejectionReason> fromCode(String code) {
        try {
            return Optional.of(RejectionReason.valueOf(code));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Find a RejectionReason by its description
     * @param description The description to look up
     * @return Optional containing the matching RejectionReason, or empty if not found
     */
    public Optional<RejectionReason> fromDescription(String description) {
        return Arrays.stream(RejectionReason.values())
                .filter(reason -> reason.getDescription().equalsIgnoreCase(description))
                .findFirst();
    }

    /**
     * Format the full rejection reason text, handling the special case for "OTHER"
     * @param reason The rejection reason
     * @param customDetails Optional custom details for "OTHER" reason
     * @return The formatted rejection reason text
     */
    public String formatRejectionReason(RejectionReason reason, String customDetails) {
        if (reason == RejectionReason.OTHER && customDetails != null && !customDetails.isEmpty()) {
            return reason.getDescription() + ": " + customDetails;
        }
        return reason.getDescription();
    }
}