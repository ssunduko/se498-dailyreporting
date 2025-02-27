package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.repository.ActivityEntryRepository;
import com.se498.dailyreporting.repository.DailyReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyReportingServiceImpl implements DailyReportingService {

    @Autowired
    private final DailyReportRepository reportRepository;
    @Autowired
    private final ActivityEntryRepository activityRepository;

    @Override
    @Transactional
    public DailyReport createReport(String projectId, LocalDate reportDate, String createdBy) {
        log.info("Creating daily report for project {}, date {}", projectId, reportDate);

        // Check if report already exists for this project and date
        Optional<DailyReport> existingReport = reportRepository.findByProjectIdAndReportDate(projectId, reportDate);
        if (existingReport.isPresent()) {
            log.warn("Report already exists for project {} on date {}", projectId, reportDate);
            throw new IllegalStateException("A report already exists for this project and date");
        }

        // Create new report
        DailyReport report = new DailyReport(
                UUID.randomUUID().toString(),
                projectId,
                reportDate
        );
        report.setCreatedBy(createdBy);
        report.setCreatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    @Override
    @Transactional
    public DailyReport updateReport(String reportId, String notes, String updatedBy) {
        log.info("Updating daily report {}", reportId);

        DailyReport report = getReportOrThrow(reportId);

        // Validate report is editable
        validateReportIsEditable(report);

        // Update report fields
        report.setNotes(notes);
        report.setUpdatedBy(updatedBy);
        report.setUpdatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    @Override
    @Transactional
    public DailyReport submitReport(String reportId, String submittedBy) {
        log.info("Submitting daily report {} for approval", reportId);

        DailyReport report = getReportOrThrow(reportId);

        // Validate report can be submitted
        if (report.getStatus() != ReportStatus.DRAFT && report.getStatus() != ReportStatus.REJECTED) {
            throw new IllegalStateException(
                    "Cannot submit report in " + report.getStatus() + " state");
        }

        // Validate report has activities
        if (report.getActivities() == null || report.getActivities().isEmpty()) {
            throw new IllegalStateException("Cannot submit a report with no activities");
        }

        // Update status to submitted
        report.setStatus(ReportStatus.SUBMITTED);
        report.setUpdatedBy(submittedBy);
        report.setUpdatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    @Override
    @Transactional
    public DailyReport approveReport(String reportId, String approvedBy) {
        log.info("Approving daily report {}", reportId);

        DailyReport report = getReportOrThrow(reportId);

        // Validate report can be approved
        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Cannot approve report in " + report.getStatus() + " state");
        }

        // Update status to approved
        report.setStatus(ReportStatus.APPROVED);
        report.setUpdatedBy(approvedBy);
        report.setUpdatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    @Override
    @Transactional
    public DailyReport rejectReport(String reportId, String reason, String rejectedBy) {
        log.info("Rejecting daily report {}: {}", reportId, reason);

        DailyReport report = getReportOrThrow(reportId);

        // Validate report can be rejected
        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Cannot reject report in " + report.getStatus() + " state");
        }

        // Update status to rejected
        report.setStatus(ReportStatus.REJECTED);

        // Add rejection reason to notes
        String notes = report.getNotes();
        if (notes == null) {
            notes = "";
        }

        notes += "\nREJECTION REASON: " + reason;
        report.setNotes(notes);

        report.setUpdatedBy(rejectedBy);
        report.setUpdatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DailyReport> getReport(String reportId) {
        log.info("Fetching daily report {}", reportId);
        return reportRepository.findByIdWithActivities(reportId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyReport> getReportsByProject(String projectId) {
        log.info("Fetching reports for project {}", projectId);
        return reportRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyReport> getReportsByDateRange(String projectId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching reports for project {} between {} and {}", projectId, startDate, endDate);
        return reportRepository.findByProjectIdAndDateRange(projectId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyReport> getReportsByStatus(ReportStatus status) {
        log.info("Fetching reports with status {}", status);
        return reportRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public void deleteReport(String reportId) {
        log.info("Deleting daily report {}", reportId);

        DailyReport report = getReportOrThrow(reportId);

        // Validate report can be deleted
        validateReportIsEditable(report);

        // Delete activities first
        activityRepository.deleteByReportId(reportId);

        // Delete report
        reportRepository.deleteById(reportId);
    }

    @Override
    @Transactional
    public ActivityEntry addActivityToReport(String reportId, ActivityEntry activity) {
        log.info("Adding activity to report {}", reportId);

        DailyReport report = getReportOrThrow(reportId);

        // Validate report is editable
        validateReportIsEditable(report);

        // Set report ID if not already set
        activity.setReportId(reportId);

        // Generate ID if not provided
        if (activity.getId() == null || activity.getId().isEmpty()) {
            activity.setId(UUID.randomUUID().toString());
        }

        // Set created timestamp if not set
        if (activity.getCreatedAt() == null) {
            activity.setCreatedAt(LocalDateTime.now());
        }

        // Set initial status if not set
        if (activity.getStatus() == null) {
            activity.setStatus(ActivityStatus.PLANNED);
        }

        // Save activity
        ActivityEntry savedActivity = activityRepository.save(activity);

        // Add to report
        report.addActivity(savedActivity);
        reportRepository.save(report);

        return savedActivity;
    }

    @Override
    @Transactional
    public ActivityEntry updateActivity(String activityId, ActivityEntry updatedActivity) {
        log.info("Updating activity {}", activityId);

        // Find existing activity
        ActivityEntry activity = getActivityOrThrow(activityId);

        // Find report and validate it's editable
        DailyReport report = getReportOrThrow(activity.getReportId());
        validateReportIsEditable(report);

        // Update fields
        activity.setDescription(updatedActivity.getDescription());
        activity.setCategory(updatedActivity.getCategory());
        activity.setStartTime(updatedActivity.getStartTime());
        activity.setEndTime(updatedActivity.getEndTime());
        activity.setProgress(updatedActivity.getProgress());
        activity.setStatus(updatedActivity.getStatus());
        activity.setNotes(updatedActivity.getNotes());

        if (updatedActivity.getPersonnel() != null) {
            activity.setPersonnel(updatedActivity.getPersonnel());
        }

        // Update timestamp
        activity.setUpdatedAt(LocalDateTime.now());

        return activityRepository.save(activity);
    }

    @Override
    @Transactional
    public ActivityEntry updateActivityProgress(String activityId, double progress, String updatedBy) {
        log.info("Updating progress for activity {} to {}%", activityId, progress);

        // Validate progress value
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }

        // Find existing activity
        ActivityEntry activity = getActivityOrThrow(activityId);

        // Find report and validate it's editable
        DailyReport report = getReportOrThrow(activity.getReportId());
        validateReportIsEditable(report);

        // Update progress and status
        activity.setProgress(progress);

        // Update status based on progress
        if (progress == 0) {
            activity.setStatus(ActivityStatus.PLANNED);
        } else if (progress < 100) {
            activity.setStatus(ActivityStatus.IN_PROGRESS);
        } else {
            activity.setStatus(ActivityStatus.COMPLETED);
        }

        // Update timestamp
        activity.setUpdatedAt(LocalDateTime.now());

        ActivityEntry updatedActivity = activityRepository.save(activity);

        // Update report
        report.setUpdatedBy(updatedBy);
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);

        return updatedActivity;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActivityEntry> getActivity(String activityId) {
        log.info("Fetching activity {}", activityId);
        return activityRepository.findById(activityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityEntry> getActivitiesByReport(String reportId) {
        log.info("Fetching activities for report {}", reportId);
        return activityRepository.findByReportId(reportId);
    }

    @Override
    @Transactional
    public void deleteActivity(String activityId) {
        log.info("Deleting activity {}", activityId);

        // Find existing activity
        ActivityEntry activity = getActivityOrThrow(activityId);

        // Find report and validate it's editable
        DailyReport report = getReportOrThrow(activity.getReportId());
        validateReportIsEditable(report);

        // Remove from report
        report.removeActivity(activityId);
        reportRepository.save(report);

        // Delete activity
        activityRepository.deleteById(activityId);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateReportProgress(String reportId) {
        log.info("Calculating progress for report {}", reportId);

        DailyReport report = getReportOrThrow(reportId);
        return report.calculateProgress();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isReportComplete(String reportId) {
        log.info("Checking if report {} is complete", reportId);

        DailyReport report = getReportOrThrow(reportId);
        return report.isComplete();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalActivityDurationMinutes(String reportId) {
        log.info("Calculating total activity duration for report {}", reportId);

        List<ActivityEntry> activities = activityRepository.findByReportId(reportId);

        return activities.stream()
                .map(ActivityEntry::calculateDuration)
                .mapToLong(duration -> duration.toMinutes())
                .sum();
    }

    // Helper methods

    private DailyReport getReportOrThrow(String reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
    }

    private ActivityEntry getActivityOrThrow(String activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityId));
    }

    private void validateReportIsEditable(DailyReport report) {
        if (report.getStatus() == ReportStatus.SUBMITTED ||
                report.getStatus() == ReportStatus.APPROVED) {
            throw new IllegalStateException(
                    "Cannot modify report in " + report.getStatus() + " state");
        }
    }
}