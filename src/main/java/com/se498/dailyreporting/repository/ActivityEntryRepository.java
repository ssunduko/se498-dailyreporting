package com.se498.dailyreporting.repository;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityEntryRepository extends JpaRepository<ActivityEntry, String> {

    List<ActivityEntry> findByReportId(String reportId);

    List<ActivityEntry> findByStatus(ActivityStatus status);

    void deleteByReportId(String reportId);

    @Query("SELECT a FROM ActivityEntry a WHERE a.startTime >= :startTime AND a.endTime <= :endTime")
    List<ActivityEntry> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM ActivityEntry a WHERE a.reportId = :reportId AND a.category = :category")
    List<ActivityEntry> findByReportIdAndCategory(
            @Param("reportId") String reportId,
            @Param("category") String category);

    @Query("SELECT COUNT(a) FROM ActivityEntry a WHERE a.reportId = :reportId AND a.status = 'COMPLETED'")
    long countCompletedActivitiesByReportId(@Param("reportId") String reportId);
}