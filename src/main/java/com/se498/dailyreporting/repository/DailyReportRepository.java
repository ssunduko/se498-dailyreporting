package com.se498.dailyreporting.repository;

import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, String> {

    /**
     * Find all reports for a project
     */
    List<DailyReport> findByProjectId(String projectId);

    /**
     * Find all reports by status
     */
    List<DailyReport> findByStatus(ReportStatus status);

    /**
     * Find reports by project and date range
     */
    @Query("SELECT r FROM DailyReport r WHERE r.projectId = :projectId " +
            "AND r.reportDate BETWEEN :startDate AND :endDate " +
            "ORDER BY r.reportDate DESC")
    List<DailyReport> findByProjectIdAndDateRange(
            @Param("projectId") String projectId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find report by project ID and report date
     */
    Optional<DailyReport> findByProjectIdAndReportDate(String projectId, LocalDate reportDate);

    /**
     * Count reports by status
     */
    @Query("SELECT r.status, COUNT(r) FROM DailyReport r GROUP BY r.status")
    List<Object[]> countByStatus();

    /**
     * Find recent reports created by user
     */
    @Query("SELECT r FROM DailyReport r WHERE r.createdBy = :username " +
            "ORDER BY r.createdAt DESC")
    List<DailyReport> findRecentReportsByUser(
            @Param("username") String username, Pageable pageable);

    @Query("SELECT r FROM DailyReport r LEFT JOIN FETCH r.activities " +
            "WHERE r.id = :reportId")
    Optional<DailyReport> findByIdWithActivities(@Param("reportId") String reportId);

    @Query("SELECT COUNT(r) FROM DailyReport r WHERE r.projectId = :projectId " +
            "AND r.status = :status")
    long countByProjectIdAndStatus(
            @Param("projectId") String projectId,
            @Param("status") ReportStatus status);
}
