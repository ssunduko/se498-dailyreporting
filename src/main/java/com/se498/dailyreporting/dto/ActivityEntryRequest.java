package com.se498.dailyreporting.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.se498.dailyreporting.domain.vo.ActivityStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Transfer Object for activity entry requests.
 * Handles both required and optional fields for activity creation and updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEntryRequest {
    // Required fields
    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    // Optional fields - removed @NotNull annotations
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Min(value = 0, message = "Progress cannot be negative")
    @Max(value = 100, message = "Progress cannot exceed 100")
    private double progress;

    private ActivityStatus status;

    private String notes;

    /**
     * List of personnel assigned to this activity.
     * When used from a form, this list will be populated using a hidden field
     * for each personnel item.
     */
    private List<String> personnel = new ArrayList<>();

    /**
     * Utility method to get personnel as a Set
     * @return Set containing all personnel
     */
    public Set<String> getPersonnelAsSet() {
        return new HashSet<>(personnel);
    }

    /**
     * Used for temporarily storing comma-separated personnel from form input
     */
    private String personnelText;

    /**
     * Process personnel from a comma-separated string
     */
    public void processPersonnelFromText() {
        if (personnelText != null && !personnelText.isEmpty()) {
            // Clear existing personnel first to avoid duplicates
            personnel.clear();

            String[] items = personnelText.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    personnel.add(trimmed);
                }
            }
        }
    }
}