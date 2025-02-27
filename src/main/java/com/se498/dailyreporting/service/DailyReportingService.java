package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ReportStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportingService {
    // Daily Report Operations
    DailyReport createReport(String projectId, LocalDate reportDate, String createdBy);
    DailyReport updateReport(String reportId, String notes, String updatedBy);
    DailyReport submitReport(String reportId, String submittedBy);
    DailyReport approveReport(String reportId, String approvedBy);
    DailyReport rejectReport(String reportId, String reason, String rejectedBy);
    Optional<DailyReport> getReport(String reportId);

    List<DailyReport> getReportsByProject(String projectId);
    List<DailyReport> getReportsByDateRange(String projectId, LocalDate startDate, LocalDate endDate);
    List<DailyReport> getReportsByStatus(ReportStatus status);
    void deleteReport(String reportId);

    // Activity Operations
    ActivityEntry addActivityToReport(String reportId, ActivityEntry activity);
    ActivityEntry updateActivity(String activityId, ActivityEntry updatedActivity);
    ActivityEntry updateActivityProgress(String activityId, double progress, String updatedBy);
    Optional<ActivityEntry> getActivity(String activityId);

    List<ActivityEntry> getActivitiesByReport(String reportId);
    void deleteActivity(String activityId);

    // Report Analytics
    double calculateReportProgress(String reportId);
    boolean isReportComplete(String reportId);
    long getTotalActivityDurationMinutes(String reportId);
}