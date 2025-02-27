package com.se498.dailyreporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEntryResponse {
    private String id;
    private String reportId;
    private String description;
    private String category;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double progress;
    private String status;
    private String notes;
    private Set<String> personnel;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private long durationMinutes;
}