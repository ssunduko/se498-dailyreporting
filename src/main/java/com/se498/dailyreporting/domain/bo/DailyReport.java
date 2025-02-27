package com.se498.dailyreporting.domain.bo;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_reports",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_project_date",
                        columnNames = {"project_id", "report_date"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyReport {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    @Column(name = "notes", length = 2000)
    private String notes;

    @OneToMany(mappedBy = "reportId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ActivityEntry> activities = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    public DailyReport(String id, String projectId, LocalDate reportDate) {
        this.id = id;
        this.projectId = projectId;
        this.reportDate = reportDate;
        this.status = ReportStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Add an activity to this report
     */
    public void addActivity(ActivityEntry activity) {
        if (activities == null) {
            activities = new ArrayList<>();
        }
        activities.add(activity);
        activity.setReportId(this.id);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Remove an activity from this report
     */
    public void removeActivity(String activityId) {
        if (activities != null) {
            activities.removeIf(activity -> activity.getId().equals(activityId));
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Calculate the overall progress of this report
     *
     * @return The average progress percentage of all activities
     */
    public double calculateProgress() {
        if (activities == null || activities.isEmpty()) {
            return 0.0;
        }

        double totalProgress = activities.stream()
                .mapToDouble(ActivityEntry::getProgress)
                .sum();

        return totalProgress / activities.size();
    }

    /**
     * Check if this report is complete
     *
     * @return true if all activities are complete
     */
    public boolean isComplete() {
        return activities != null &&
                !activities.isEmpty() &&
                activities.stream().allMatch(ActivityEntry::isComplete);
    }

    /**
     * Updates audit information
     */
    public void setAuditInfo(String updatedBy) {
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Pre-persist hook
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ReportStatus.DRAFT;
        }
    }

    /**
     * Pre-update hook
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
