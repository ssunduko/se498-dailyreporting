package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.dto.ActivityEntryRequest;
import com.se498.dailyreporting.dto.DailyReportRequest;
import com.se498.dailyreporting.service.DailyReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller for daily report UI views
 */
@Slf4j
@Controller
@RequestMapping("/ui/reports")
@RequiredArgsConstructor
public class DailyReportViewController {

    @Autowired
    private final DailyReportingService reportingService;

    /**
     * List all reports
     */
    @GetMapping
    public String listReports(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            Model model) {

        List<DailyReport> reports = new ArrayList<>();

        if (projectId != null && !projectId.isEmpty()) {
            if (startDate != null && endDate != null) {
                reports = reportingService.getReportsByDateRange(projectId, startDate, endDate);
            } else {
                reports = reportingService.getReportsByProject(projectId);
            }
            model.addAttribute("projectId", projectId);
        } else if (status != null && !status.isEmpty()) {
            try {
                ReportStatus reportStatus = ReportStatus.valueOf(status.toUpperCase());
                reports = reportingService.getReportsByStatus(reportStatus);
                model.addAttribute("status", status);
            } catch (IllegalArgumentException e) {
                log.error("Invalid report status: {}", status);
                model.addAttribute("error", "Invalid report status: " + status);
            }
        }

        model.addAttribute("reports", reports);
        model.addAttribute("reportStatuses", ReportStatus.values());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("pageTitle", "Daily Reports");

        return "dailyreport/list";
    }

    /**
     * View a single report
     */
    @GetMapping("/{reportId}")
    public String viewReport(@PathVariable String reportId, Model model) {
        try {
            Optional<DailyReport> reportOpt = reportingService.getReport(reportId);

            if (reportOpt.isPresent()) {
                DailyReport report = reportOpt.get();
                model.addAttribute("report", report);

                // Get activities for the report
                List<ActivityEntry> activities = reportingService.getActivitiesByReport(reportId);
                model.addAttribute("activities", activities);

                // Calculate report metrics
                double progress = reportingService.calculateReportProgress(reportId);
                boolean isComplete = reportingService.isReportComplete(reportId);
                long totalDuration = reportingService.getTotalActivityDurationMinutes(reportId);

                model.addAttribute("progress", progress);
                model.addAttribute("isComplete", isComplete);
                model.addAttribute("totalDuration", totalDuration);
                model.addAttribute("pageTitle", "Report: " + report.getProjectId() + " - " + report.getReportDate());

                return "dailyreport/view";
            } else {
                model.addAttribute("error", "Report not found with ID: " + reportId);
                return "redirect:/ui/reports";
            }
        } catch (Exception e) {
            log.error("Error retrieving report: {}", e.getMessage(), e);
            model.addAttribute("error", "Error retrieving report: " + e.getMessage());
            return "redirect:/ui/reports";
        }
    }

    /**
     * Form to create a new report
     */
    @GetMapping("/new")
    public String newReportForm(Model model) {
        model.addAttribute("report", new DailyReportRequest());
        model.addAttribute("pageTitle", "Create New Report");
        return "dailyreport/edit";
    }

    /**
     * Create a new report
     */
    @PostMapping("/new")
    public String createReport(
            @ModelAttribute DailyReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        userDetails = userDetails == null ? getDefaultUser() : userDetails;

        try {
            DailyReport report = reportingService.createReport(
                    request.getProjectId(),
                    request.getReportDate(),
                    userDetails.getUsername()
            );

            // Update notes if provided
            if (request.getNotes() != null && !request.getNotes().isEmpty()) {
                report = reportingService.updateReport(
                        report.getId(),
                        request.getNotes(),
                        userDetails.getUsername()
                );
            }

            redirectAttributes.addFlashAttribute("success", "Report created successfully");
            return "redirect:/ui/reports/" + report.getId();
        } catch (Exception e) {
            log.error("Error creating report: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error creating report: " + e.getMessage());
            return "redirect:/ui/reports/new";
        }
    }

    /**
     * Form to edit a report
     */
    @GetMapping("/{reportId}/edit")
    public String editReportForm(@PathVariable String reportId, Model model) {
        try {
            Optional<DailyReport> reportOpt = reportingService.getReport(reportId);

            if (reportOpt.isPresent()) {
                DailyReport report = reportOpt.get();

                DailyReportRequest request = new DailyReportRequest();
                request.setProjectId(report.getProjectId());
                request.setReportDate(report.getReportDate());
                request.setNotes(report.getNotes());

                model.addAttribute("report", request);
                model.addAttribute("reportId", reportId);
                model.addAttribute("pageTitle", "Edit Report");

                return "dailyreport/edit";
            } else {
                model.addAttribute("error", "Report not found with ID: " + reportId);
                return "redirect:/ui/reports";
            }
        } catch (Exception e) {
            log.error("Error retrieving report for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Error retrieving report: " + e.getMessage());
            return "redirect:/ui/reports";
        }
    }

    /**
     * Update a report
     */
    @PostMapping("/{reportId}/edit")
    public String updateReport(
            @PathVariable String reportId,
            @ModelAttribute DailyReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        userDetails = userDetails == null ? getDefaultUser() : userDetails;

        try {
            reportingService.updateReport(
                    reportId,
                    request.getNotes(),
                    userDetails.getUsername()
            );

            redirectAttributes.addFlashAttribute("success", "Report updated successfully");
            return "redirect:/ui/reports/" + reportId;
        } catch (Exception e) {
            log.error("Error updating report: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error updating report: " + e.getMessage());
            return "redirect:/ui/reports/" + reportId + "/edit";
        }
    }

    /**
     * Submit a report
     */
    @PostMapping("/{reportId}/submit")
    public String submitReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        userDetails = userDetails == null ? getDefaultUser() : userDetails;

        try {
            reportingService.submitReport(reportId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Report submitted successfully");
        } catch (Exception e) {
            log.error("Error submitting report: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error submitting report: " + e.getMessage());
        }

        return "redirect:/ui/reports/" + reportId;
    }

    /**
     * Approve a report
     */
    @PostMapping("/{reportId}/approve")
    public String approveReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        userDetails = userDetails == null ? getDefaultUser() : userDetails;

        try {
            reportingService.approveReport(reportId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Report approved successfully");
        } catch (Exception e) {
            log.error("Error approving report: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error approving report: " + e.getMessage());
        }

        return "redirect:/ui/reports/" + reportId;
    }

    /**
     * Reject a report
     */
    @PostMapping("/{reportId}/reject")
    public String rejectReport(
            @PathVariable String reportId,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        userDetails = userDetails == null ? getDefaultUser() : userDetails;

        try {
            reportingService.rejectReport(reportId, reason, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Report rejected successfully");
        } catch (Exception e) {
            log.error("Error rejecting report: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error rejecting report: " + e.getMessage());
        }

        return "redirect:/ui/reports/" + reportId;
    }

    /**
     * Delete a report
     */
    @PostMapping("/{reportId}/delete")
    public String deleteReport(
            @PathVariable String reportId,
            RedirectAttributes redirectAttributes) {

        try {
            reportingService.deleteReport(reportId);
            redirectAttributes.addFlashAttribute("success", "Report deleted successfully");
            return "redirect:/ui/reports";
        } catch (Exception e) {
            log.error("Error deleting report: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error deleting report: " + e.getMessage());
            return "redirect:/ui/reports/" + reportId;
        }
    }

    /**
     * Form to add a new activity to a report
     */
    @GetMapping("/{reportId}/activities/new")
    public String newActivityForm(@PathVariable String reportId, Model model) {
        try {
            Optional<DailyReport> reportOpt = reportingService.getReport(reportId);

            if (reportOpt.isPresent()) {
                model.addAttribute("reportId", reportId);
                model.addAttribute("report", reportOpt.get());
                model.addAttribute("activity", new ActivityEntryRequest());
                model.addAttribute("activityStatuses", ActivityStatus.values());
                model.addAttribute("pageTitle", "Add Activity");

                return "dailyreport/activity-edit";
            } else {
                model.addAttribute("error", "Report not found with ID: " + reportId);
                return "redirect:/ui/reports";
            }
        } catch (Exception e) {
            log.error("Error preparing activity form: {}", e.getMessage(), e);
            model.addAttribute("error", "Error preparing activity form: " + e.getMessage());
            return "redirect:/ui/reports/" + reportId;
        }
    }

    /**
     * Add a new activity to a report
     */
    @PostMapping("/{reportId}/activities/new")
    public String addActivity(
            @PathVariable String reportId,
            @ModelAttribute ActivityEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        userDetails = userDetails == null ? getDefaultUser() : userDetails;

        try {
            // Create activity from request
            ActivityEntry activity = new ActivityEntry();
            activity.setId(UUID.randomUUID().toString());
            activity.setReportId(reportId);
            activity.setDescription(request.getDescription());
            activity.setCategory(request.getCategory());
            activity.setStartTime(request.getStartTime());
            activity.setEndTime(request.getEndTime());
            activity.setProgress(request.getProgress());
            activity.setStatus(request.getStatus());
            activity.setNotes(request.getNotes());
            activity.setCreatedBy(userDetails.getUsername());
            activity.setCreatedAt(LocalDateTime.now());

            // Add personnel if provided
            if (request.getPersonnel() != null) {
                activity.setPersonnel(request.getPersonnel());
            }

            // Add activity to report
            reportingService.addActivityToReport(reportId, activity);

            redirectAttributes.addFlashAttribute("success", "Activity added successfully");
            return "redirect:/ui/reports/" + reportId;
        } catch (Exception e) {
            log.error("Error adding activity: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error adding activity: " + e.getMessage());
            return "redirect:/ui/reports/" + reportId + "/activities/new";
        }
    }

    /**
     * Form to edit an activity
     */
    @GetMapping("/activities/{activityId}/edit")
    public String editActivityForm(@PathVariable String activityId, Model model) {
        try {
            Optional<ActivityEntry> activityOpt = reportingService.getActivity(activityId);

            if (activityOpt.isPresent()) {
                ActivityEntry activity = activityOpt.get();

                ActivityEntryRequest request = new ActivityEntryRequest();
                request.setDescription(activity.getDescription());
                request.setCategory(activity.getCategory());
                request.setStartTime(activity.getStartTime());
                request.setEndTime(activity.getEndTime());
                request.setProgress(activity.getProgress());
                request.setStatus(activity.getStatus());
                request.setNotes(activity.getNotes());

                if (activity.getPersonnel() != null) {
                    request.setPersonnel(activity.getPersonnel());
                }

                model.addAttribute("activity", request);
                model.addAttribute("activityId", activityId);
                model.addAttribute("reportId", activity.getReportId());
                model.addAttribute("activityStatuses", ActivityStatus.values());
                model.addAttribute("pageTitle", "Edit Activity");

                return "dailyreport/activity-edit";
            } else {
                model.addAttribute("error", "Activity not found with ID: " + activityId);
                return "redirect:/ui/reports";
            }
        } catch (Exception e) {
            log.error("Error retrieving activity for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Error retrieving activity: " + e.getMessage());
            return "redirect:/ui/reports";
        }
    }

    /**
     * Update an activity
     */
    @PostMapping("/activities/{activityId}/edit")
    public String updateActivity(
            @PathVariable String activityId,
            @ModelAttribute ActivityEntryRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            // Create updated activity from request
            ActivityEntry activity = new ActivityEntry();
            activity.setDescription(request.getDescription());
            activity.setCategory(request.getCategory());
            activity.setStartTime(request.getStartTime());
            activity.setEndTime(request.getEndTime());
            activity.setProgress(request.getProgress());
            activity.setStatus(request.getStatus());
            activity.setNotes(request.getNotes());

            // Add personnel if provided
            if (request.getPersonnel() != null) {
                activity.setPersonnel(request.getPersonnel());
            }

            // Update activity
            ActivityEntry updatedActivity = reportingService.updateActivity(activityId, activity);

            redirectAttributes.addFlashAttribute("success", "Activity updated successfully");
            return "redirect:/ui/reports/" + updatedActivity.getReportId();
        } catch (Exception e) {
            log.error("Error updating activity: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error updating activity: " + e.getMessage());

            // Try to get the activity to redirect back to the report
            try {
                Optional<ActivityEntry> activityOpt = reportingService.getActivity(activityId);
                if (activityOpt.isPresent()) {
                    return "redirect:/ui/reports/" + activityOpt.get().getReportId();
                }
            } catch (Exception ex) {
                // If we can't get the activity, redirect to reports list
                return "redirect:/ui/reports";
            }

            return "redirect:/ui/reports";
        }
    }

    /**
     * Form to update activity progress
     */
    @GetMapping("/activities/{activityId}/progress")
    public String updateProgressForm(@PathVariable String activityId, Model model) {
        try {
            Optional<ActivityEntry> activityOpt = reportingService.getActivity(activityId);

            if (activityOpt.isPresent()) {
                ActivityEntry activity = activityOpt.get();

                model.addAttribute("activity", activity);
                model.addAttribute("pageTitle", "Update Progress");

                return "dailyreport/activity-progress";
            } else {
                model.addAttribute("error", "Activity not found with ID: " + activityId);
                return "redirect:/ui/reports";
            }
        } catch (Exception e) {
            log.error("Error retrieving activity for progress update: {}", e.getMessage(), e);
            model.addAttribute("error", "Error retrieving activity: " + e.getMessage());
            return "redirect:/ui/reports";
        }
    }

    /**
     * Update activity progress
     */
    @PostMapping("/activities/{activityId}/progress")
    public String updateActivityProgress(
            @PathVariable String activityId,
            @RequestParam double progress,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        userDetails = userDetails == null ? getDefaultUser() : userDetails;

        try {
            ActivityEntry updatedActivity = reportingService.updateActivityProgress(
                    activityId, progress, userDetails.getUsername());

            redirectAttributes.addFlashAttribute("success", "Activity progress updated successfully");
            return "redirect:/ui/reports/" + updatedActivity.getReportId();
        } catch (Exception e) {
            log.error("Error updating activity progress: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error updating activity progress: " + e.getMessage());

            // Try to get the activity to redirect back to the report
            try {
                Optional<ActivityEntry> activityOpt = reportingService.getActivity(activityId);
                if (activityOpt.isPresent()) {
                    return "redirect:/ui/reports/" + activityOpt.get().getReportId();
                }
            } catch (Exception ex) {
                // If we can't get the activity, redirect to reports list
                return "redirect:/ui/reports";
            }

            return "redirect:/ui/reports";
        }
    }

    /**
     * Delete an activity
     */
    @PostMapping("/activities/{activityId}/delete")
    public String deleteActivity(
            @PathVariable String activityId,
            RedirectAttributes redirectAttributes) {

        String reportId = "";

        try {
            // Get the report ID before deleting the activity
            Optional<ActivityEntry> activityOpt = reportingService.getActivity(activityId);
            if (activityOpt.isPresent()) {
                reportId = activityOpt.get().getReportId();
            }

            reportingService.deleteActivity(activityId);
            redirectAttributes.addFlashAttribute("success", "Activity deleted successfully");

            if (!reportId.isEmpty()) {
                return "redirect:/ui/reports/" + reportId;
            } else {
                return "redirect:/ui/reports";
            }
        } catch (Exception e) {
            log.error("Error deleting activity: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error deleting activity: " + e.getMessage());

            if (!reportId.isEmpty()) {
                return "redirect:/ui/reports/" + reportId;
            } else {
                return "redirect:/ui/reports";
            }
        }
    }

    private UserDetails getDefaultUser(){

        UserDetails userDetails = User.builder()
                .username("sergey")
                .password("chapman")
                .roles("USER", "ADMIN")
                .build();

        // Create authentication token with full details and authorities
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, "chapman", userDetails.getAuthorities());

        // Set in security context
        SecurityContextHolder.getContext().setAuthentication(auth);

        return userDetails;
    }
}
