package com.se498.dailyreporting.domain.bo;

import com.se498.dailyreporting.domain.vo.ActivityStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "activity_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEntry {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "report_id", nullable = false)
    private String reportId;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "progress", nullable = false)
    private double progress;

    @ElementCollection
    @CollectionTable(
            name = "activity_personnel",
            joinColumns = @JoinColumn(name = "activity_id")
    )
    @Column(name = "personnel_id")
    private Set<String> personnel = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ActivityStatus status;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Calculate the duration of this activity
     *
     * @return Duration between start and end time
     */
    @Transient
    public Duration calculateDuration() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime);
        }
        return Duration.ZERO;
    }

    /**
     * Check if this activity is complete
     *
     * @return true if status is COMPLETED or progress is 100%
     */
    @Transient
    public boolean isComplete() {
        return this.status == ActivityStatus.COMPLETED || this.progress >= 100.0;
    }

    /**
     * Check if this activity is in progress
     *
     * @return true if status is IN_PROGRESS or progress is between 0 and 100
     */
    @Transient
    public boolean isInProgress() {
        return this.status == ActivityStatus.IN_PROGRESS ||
                (this.progress > 0 && this.progress < 100);
    }

    /**
     * Set audit information
     *
     * @param updatedBy The user updating this activity
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
            status = ActivityStatus.PLANNED;
        }
    }

    /**
     * Pre-update hook
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Validate time range
     */
    private void validateTimeRange() {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }
}