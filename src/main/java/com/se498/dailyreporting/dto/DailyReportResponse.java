package com.se498.dailyreporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportResponse {
    private String id;
    private String projectId;
    private LocalDate reportDate;
    private String status;
    private String notes;
    private List<ActivityEntryResponse> activities;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private double progress;
    private boolean complete;
}