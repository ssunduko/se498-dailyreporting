package com.se498.dailyreporting.domain.vo;

import lombok.Getter;

/**
 * Predefined reasons for report rejection
 */
@Getter
public enum RejectionReason {
    INCOMPLETE_INFORMATION("Incomplete information"),
    INACCURATE_DATA("Inaccurate data"),
    MISSING_ACTIVITIES("Missing activities"),
    ADDITIONAL_DETAILS_REQUIRED("Requires additional details"),
    FORMATTING_ISSUES("Formatting issues"),
    DUPLICATE_REPORT("Duplicate report"),
    SAFETY_ISSUES("Safety issues not addressed"),
    MISSING_SIGNATURES("Missing required signatures"),
    BUDGET_INCONSISTENCIES("Budget inconsistencies"),
    OTHER("Other");

    private final String description;

    RejectionReason(String description) {
        this.description = description;
    }

}
