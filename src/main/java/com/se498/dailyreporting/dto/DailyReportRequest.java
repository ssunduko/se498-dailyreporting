package com.se498.dailyreporting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportRequest {
    @NotBlank(message = "Project ID is required")
    private String projectId;

    @NotNull(message = "Report date is required")
    @PastOrPresent(message = "Report date cannot be in the future")
    private LocalDate reportDate;

    private String notes;

    // Field for adding initial activities during report creation
    // No validation constraints on this field to ensure it's optional
    private List<ActivityEntryRequest> initialActivities = new ArrayList<>();

    /**
     * Utility method to check if the request has valid activities
     * @return true if there are valid activities, false otherwise
     */
    public boolean hasValidActivities() {
        if (initialActivities == null || initialActivities.isEmpty()) {
            return false;
        }

        return initialActivities.stream()
                .anyMatch(activity ->
                        activity != null &&
                                activity.getDescription() != null &&
                                !activity.getDescription().isEmpty() &&
                                activity.getCategory() != null &&
                                !activity.getCategory().isEmpty()
                );
    }

    /**
     * Filter out invalid activities from the list
     * @return filtered list with only valid activities
     */
    public List<ActivityEntryRequest> getValidActivities() {
        if (initialActivities == null) {
            return new ArrayList<>();
        }

        return initialActivities.stream()
                .filter(activity ->
                        activity != null &&
                                activity.getDescription() != null &&
                                !activity.getDescription().isEmpty() &&
                                activity.getCategory() != null &&
                                !activity.getCategory().isEmpty()
                )
                .collect(java.util.stream.Collectors.toList());
    }
}