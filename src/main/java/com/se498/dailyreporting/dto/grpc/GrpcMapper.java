package com.se498.dailyreporting.dto.grpc;

import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.grpc.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between domain objects and gRPC DTOs
 */
@Component
public class GrpcMapper {

    /**
     * Convert a domain DailyReport to a gRPC DailyReportResponse
     */
    public DailyReportResponse toGrpcResponse(DailyReport report) {
        if (report == null) {
            return null;
        }

        DailyReportResponse.Builder builder = DailyReportResponse.newBuilder()
                .setId(report.getId())
                .setProjectId(report.getProjectId())
                .setReportDate(toGrpcDate(report.getReportDate()))
                .setStatus(toGrpcReportStatus(report.getStatus()))
                .setCreatedBy(report.getCreatedBy())
                .setCreatedAt(toGrpcTimestamp(report.getCreatedAt()))
                .setProgress(report.calculateProgress())
                .setComplete(report.isComplete());

        // Set optional fields
        if (report.getNotes() != null) {
            builder.setNotes(StringValue.of(report.getNotes()));
        }
        if (report.getUpdatedAt() != null) {
            builder.setUpdatedAt(toGrpcTimestamp(report.getUpdatedAt()));
        }
        if (report.getUpdatedBy() != null) {
            builder.setUpdatedBy(StringValue.of(report.getUpdatedBy()));
        }

        // Add activities if any
        if (report.getActivities() != null && !report.getActivities().isEmpty()) {
            report.getActivities().forEach(
                    activity -> builder.addActivities(toGrpcActivity(activity))
            );
        }

        return builder.build();
    }

    /**
     * Convert a domain ActivityEntry to a gRPC ActivityResponse
     */
    public ActivityResponse toGrpcActivity(ActivityEntry activity) {
        if (activity == null) {
            return null;
        }

        ActivityResponse.Builder builder = ActivityResponse.newBuilder()
                .setId(activity.getId())
                .setReportId(activity.getReportId())
                .setDescription(activity.getDescription())
                .setCategory(activity.getCategory())
                .setStartTime(toGrpcTimestamp(activity.getStartTime()))
                .setEndTime(toGrpcTimestamp(activity.getEndTime()))
                .setProgress(activity.getProgress())
                .setStatus(toGrpcActivityStatus(activity.getStatus()))
                .setCreatedBy(activity.getCreatedBy())
                .setCreatedAt(toGrpcTimestamp(activity.getCreatedAt()))
                .setDurationMinutes(activity.calculateDuration().toMinutes());

        // Set optional fields
        if (activity.getNotes() != null) {
            builder.setNotes(StringValue.of(activity.getNotes()));
        }
        if (activity.getUpdatedAt() != null) {
            builder.setUpdatedAt(toGrpcTimestamp(activity.getUpdatedAt()));
        }
        if (activity.getUpdatedBy() != null) {
            builder.setUpdatedBy(StringValue.of(activity.getUpdatedBy()));
        }

        // Add personnel if any
        if (activity.getPersonnel() != null && !activity.getPersonnel().isEmpty()) {
            builder.addAllPersonnel(activity.getPersonnel());
        }

        return builder.build();
    }

    /**
     * Create a new ActivityEntry from gRPC AddActivityRequest
     */
    public ActivityEntry fromGrpcAddActivityRequest(AddActivityRequest request) {
        ActivityEntry activity = new ActivityEntry();
        activity.setReportId(request.getReportId());
        activity.setDescription(request.getDescription());
        activity.setCategory(request.getCategory());
        activity.setStartTime(fromGrpcTimestamp(request.getStartTime()));
        activity.setEndTime(fromGrpcTimestamp(request.getEndTime()));
        activity.setProgress(request.getProgress());
        activity.setStatus(fromGrpcActivityStatus(request.getStatus()));
        activity.setCreatedBy(request.getUsername());
        activity.setCreatedAt(LocalDateTime.now());

        // Set notes if provided
        if (request.hasNotes()) {
            activity.setNotes(request.getNotes().getValue());
        }

        // Set personnel if provided
        if (request.getPersonnelCount() > 0) {
            Set<String> personnel = new HashSet<>(request.getPersonnelList());
            activity.setPersonnel(personnel);
        }

        return activity;
    }

    /**
     * Create an ActivityEntry update from gRPC UpdateActivityRequest
     */
    public ActivityEntry fromGrpcUpdateActivityRequest(UpdateActivityRequest request) {
        ActivityEntry activity = new ActivityEntry();
        activity.setDescription(request.getDescription());
        activity.setCategory(request.getCategory());
        activity.setStartTime(fromGrpcTimestamp(request.getStartTime()));
        activity.setEndTime(fromGrpcTimestamp(request.getEndTime()));
        activity.setProgress(request.getProgress());
        activity.setStatus(fromGrpcActivityStatus(request.getStatus()));

        // Set notes if provided
        if (request.hasNotes()) {
            activity.setNotes(request.getNotes().getValue());
        }

        // Set personnel if provided
        if (request.getPersonnelCount() > 0) {
            Set<String> personnel = new HashSet<>(request.getPersonnelList());
            activity.setPersonnel(personnel);
        }

        return activity;
    }

    /**
     * Convert a list of DailyReports to a gRPC ReportListResponse
     */
    public ReportListResponse toGrpcReportListResponse(List<DailyReport> reports) {
        ReportListResponse.Builder builder = ReportListResponse.newBuilder();

        if (reports != null && !reports.isEmpty()) {
            reports.forEach(report -> builder.addReports(toGrpcResponse(report)));
        }

        return builder.build();
    }

    /**
     * Convert a list of ActivityEntries to a gRPC ActivityListResponse
     */
    public ActivityListResponse toGrpcActivityListResponse(List<ActivityEntry> activities) {
        ActivityListResponse.Builder builder = ActivityListResponse.newBuilder();

        if (activities != null && !activities.isEmpty()) {
            activities.forEach(activity -> builder.addActivities(toGrpcActivity(activity)));
        }

        return builder.build();
    }

    /**
     * Convert a java.time.LocalDate to a gRPC Date
     */
    public com.se498.dailyreporting.grpc.Date toGrpcDate(LocalDate localDate) {
        if (localDate == null) {
            return com.se498.dailyreporting.grpc.Date.getDefaultInstance();
        }

        return com.se498.dailyreporting.grpc.Date.newBuilder()
                .setYear(localDate.getYear())
                .setMonth(localDate.getMonthValue())
                .setDay(localDate.getDayOfMonth())
                .build();
    }

    /**
     * Convert a gRPC Date to a java.time.LocalDate
     */
    public LocalDate fromGrpcDate(com.se498.dailyreporting.grpc.Date date) {
        if (date == null || date.equals(com.se498.dailyreporting.grpc.Date.getDefaultInstance())) {
            return null;
        }

        return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }

    /**
     * Convert a string representation of a date to a gRPC Date
     */
    public Date dateFromString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        LocalDate localDate = LocalDate.parse(dateStr);
        return toGrpcDate(localDate);
    }

    /**
     * Convert a java.time.LocalDateTime to a gRPC Timestamp
     */
    public Timestamp toGrpcTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return Timestamp.getDefaultInstance();
        }

        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    /**
     * Convert a gRPC Timestamp to a java.time.LocalDateTime
     */
    public LocalDateTime fromGrpcTimestamp(Timestamp timestamp) {
        if (timestamp == null || timestamp.equals(Timestamp.getDefaultInstance())) {
            return null;
        }

        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
                ZoneOffset.UTC
        );
    }

    /**
     * Convert a domain ReportStatus to a gRPC ReportStatus
     */
    public com.se498.dailyreporting.grpc.ReportStatus toGrpcReportStatus(com.se498.dailyreporting.domain.vo.ReportStatus status) {
        if (status == null) {
            return com.se498.dailyreporting.grpc.ReportStatus.UNRECOGNIZED;
        }

        switch (status) {
            case DRAFT:
                return com.se498.dailyreporting.grpc.ReportStatus.DRAFT;
            case SUBMITTED:
                return com.se498.dailyreporting.grpc.ReportStatus.SUBMITTED;
            case APPROVED:
                return com.se498.dailyreporting.grpc.ReportStatus.APPROVED;
            case REJECTED:
                return com.se498.dailyreporting.grpc.ReportStatus.REJECTED;
            default:
                return com.se498.dailyreporting.grpc.ReportStatus.UNRECOGNIZED;
        }
    }

    /**
     * Convert a gRPC ReportStatus to a domain ReportStatus
     */
    public com.se498.dailyreporting.domain.vo.ReportStatus fromGrpcReportStatus(com.se498.dailyreporting.grpc.ReportStatus status) {
        if (status == null || status == com.se498.dailyreporting.grpc.ReportStatus.UNRECOGNIZED) {
            return com.se498.dailyreporting.domain.vo.ReportStatus.DRAFT;
        }

        switch (status) {
            case DRAFT:
                return com.se498.dailyreporting.domain.vo.ReportStatus.DRAFT;
            case SUBMITTED:
                return com.se498.dailyreporting.domain.vo.ReportStatus.SUBMITTED;
            case APPROVED:
                return com.se498.dailyreporting.domain.vo.ReportStatus.APPROVED;
            case REJECTED:
                return com.se498.dailyreporting.domain.vo.ReportStatus.REJECTED;
            default:
                return com.se498.dailyreporting.domain.vo.ReportStatus.DRAFT;
        }
    }

    /**
     * Convert a domain ActivityStatus to a gRPC ActivityStatus
     */
    public com.se498.dailyreporting.grpc.ActivityStatus toGrpcActivityStatus(com.se498.dailyreporting.domain.vo.ActivityStatus status) {
        if (status == null) {
            return com.se498.dailyreporting.grpc.ActivityStatus.UNRECOGNIZED;
        }

        switch (status) {
            case PLANNED:
                return com.se498.dailyreporting.grpc.ActivityStatus.PLANNED;
            case IN_PROGRESS:
                return com.se498.dailyreporting.grpc.ActivityStatus.IN_PROGRESS;
            case COMPLETED:
                return com.se498.dailyreporting.grpc.ActivityStatus.COMPLETED;
            case DELAYED:
                return com.se498.dailyreporting.grpc.ActivityStatus.DELAYED;
            case CANCELLED:
                return com.se498.dailyreporting.grpc.ActivityStatus.CANCELLED;
            default:
                return com.se498.dailyreporting.grpc.ActivityStatus.UNRECOGNIZED;
        }
    }

    /**
     * Convert a gRPC ActivityStatus to a domain ActivityStatus
     */
    public com.se498.dailyreporting.domain.vo.ActivityStatus fromGrpcActivityStatus(com.se498.dailyreporting.grpc.ActivityStatus status) {
        if (status == null || status == com.se498.dailyreporting.grpc.ActivityStatus.UNRECOGNIZED) {
            return com.se498.dailyreporting.domain.vo.ActivityStatus.PLANNED;
        }

        switch (status) {
            case PLANNED:
                return com.se498.dailyreporting.domain.vo.ActivityStatus.PLANNED;
            case IN_PROGRESS:
                return com.se498.dailyreporting.domain.vo.ActivityStatus.IN_PROGRESS;
            case COMPLETED:
                return com.se498.dailyreporting.domain.vo.ActivityStatus.COMPLETED;
            case DELAYED:
                return com.se498.dailyreporting.domain.vo.ActivityStatus.DELAYED;
            case CANCELLED:
                return com.se498.dailyreporting.domain.vo.ActivityStatus.CANCELLED;
            default:
                return com.se498.dailyreporting.domain.vo.ActivityStatus.PLANNED;
        }
    }
}