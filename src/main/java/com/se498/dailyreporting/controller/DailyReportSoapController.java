package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.service.DailyReportingService;
import com.se498.dailyreporting.soap.gen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SOAP Web Service Endpoint for Daily Reports
 */
@Endpoint
public class DailyReportSoapController {

    private static final String NAMESPACE_URI = "http://se498.com/dailyreporting/soap";

    @Autowired
    private DailyReportingService reportingService;

    /**
     * Get Daily Report by ID
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDailyReportRequest")
    @ResponsePayload
    public GetDailyReportResponse getDailyReport(@RequestPayload GetDailyReportRequest request) {
        GetDailyReportResponse response = new GetDailyReportResponse();

        try {
            Optional<DailyReport> reportOpt = reportingService.getReport(request.getReportId());

            if (reportOpt.isPresent()) {
                DailyReportType reportType = mapDailyReportToSoap(reportOpt.get());
                response.setDailyReport(reportType);
            } else {
                throw new RuntimeException("Report not found with ID: " + request.getReportId());
            }
        } catch (Exception e) {
            throw createServiceFault("Error retrieving daily report", e.getMessage());
        }

        return response;
    }

    /**
     * Create a new Daily Report
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createDailyReportRequest")
    @ResponsePayload
    public CreateDailyReportResponse createDailyReport(@RequestPayload CreateDailyReportRequest request) {
        CreateDailyReportResponse response = new CreateDailyReportResponse();

        try {
            // Convert XML date to LocalDate
            LocalDate reportDate = convertToLocalDate(request.getReportDate());

            // Create the report
            DailyReport report = reportingService.createReport(
                    request.getProjectId(),
                    reportDate,
                    request.getCreatedBy()
            );

            // Update notes if provided
            if (request.getNotes() != null && !request.getNotes().isEmpty()) {
                report = reportingService.updateReport(
                        report.getId(),
                        request.getNotes(),
                        request.getCreatedBy()
                );
            }

            // Map to SOAP response
            DailyReportType reportType = mapDailyReportToSoap(report);
            response.setDailyReport(reportType);

        } catch (Exception e) {
            throw createServiceFault("Error creating daily report", e.getMessage());
        }

        return response;
    }

    /**
     * Update an existing Daily Report
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateDailyReportRequest")
    @ResponsePayload
    public UpdateDailyReportResponse updateDailyReport(@RequestPayload UpdateDailyReportRequest request) {
        UpdateDailyReportResponse response = new UpdateDailyReportResponse();

        try {
            DailyReport report = reportingService.updateReport(
                    request.getReportId(),
                    request.getNotes(),
                    request.getUpdatedBy()
            );

            DailyReportType reportType = mapDailyReportToSoap(report);
            response.setDailyReport(reportType);

        } catch (Exception e) {
            throw createServiceFault("Error updating daily report", e.getMessage());
        }

        return response;
    }

    /**
     * Submit a Daily Report for approval
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "submitDailyReportRequest")
    @ResponsePayload
    public SubmitDailyReportResponse submitDailyReport(@RequestPayload SubmitDailyReportRequest request) {
        SubmitDailyReportResponse response = new SubmitDailyReportResponse();

        try {
            DailyReport report = reportingService.submitReport(
                    request.getReportId(),
                    request.getSubmittedBy()
            );

            DailyReportType reportType = mapDailyReportToSoap(report);
            response.setDailyReport(reportType);

        } catch (Exception e) {
            throw createServiceFault("Error submitting daily report", e.getMessage());
        }

        return response;
    }

    /**
     * Add an Activity to a Daily Report
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addActivityRequest")
    @ResponsePayload
    public AddActivityResponse addActivity(@RequestPayload AddActivityRequest request) {
        AddActivityResponse response = new AddActivityResponse();

        try {
            // Extract activity data from request
            ActivityEntryType activityType = request.getActivity();
            ActivityEntry activity = mapSoapToActivityEntry(activityType);

            // Add activity to report
            ActivityEntry savedActivity = reportingService.addActivityToReport(
                    request.getReportId(),
                    activity
            );

            // Map to response
            ActivityEntryType responseActivity = mapActivityEntryToSoap(savedActivity);
            response.setActivityEntry(responseActivity);

        } catch (Exception e) {
            throw createServiceFault("Error adding activity to report", e.getMessage());
        }

        return response;
    }

    /**
     * Get Reports by Project ID
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getReportsByProjectRequest")
    @ResponsePayload
    public GetReportsByProjectResponse getReportsByProject(@RequestPayload GetReportsByProjectRequest request) {
        GetReportsByProjectResponse response = new GetReportsByProjectResponse();

        try {
            List<DailyReport> reports;

            // Check if date range is provided
            if (request.getStartDate() != null && request.getEndDate() != null) {
                LocalDate startDate = convertToLocalDate(request.getStartDate());
                LocalDate endDate = convertToLocalDate(request.getEndDate());

                reports = reportingService.getReportsByDateRange(
                        request.getProjectId(),
                        startDate,
                        endDate
                );
            } else {
                reports = reportingService.getReportsByProject(request.getProjectId());
            }

            // Map to response
            List<DailyReportType> reportTypes = reports.stream()
                    .map(this::mapDailyReportToSoap)
                    .collect(Collectors.toList());

            response.getDailyReports().addAll(reportTypes);

        } catch (Exception e) {
            throw createServiceFault("Error retrieving reports by project", e.getMessage());
        }

        return response;
    }

    /**
     * Map DailyReport domain object to SOAP type
     */
    private DailyReportType mapDailyReportToSoap(DailyReport report) {
        try {
            DailyReportType reportType = new DailyReportType();
            reportType.setId(report.getId());
            reportType.setProjectId(report.getProjectId());
            reportType.setReportDate(convertToXMLDate(report.getReportDate()));
            reportType.setStatus(ReportStatusType.fromValue(report.getStatus().name()));
            reportType.setNotes(report.getNotes());
            reportType.setCreatedAt(convertToXMLDateTime(report.getCreatedAt()));
            reportType.setCreatedBy(report.getCreatedBy());

            if (report.getUpdatedAt() != null) {
                reportType.setUpdatedAt(convertToXMLDateTime(report.getUpdatedAt()));
            }

            reportType.setUpdatedBy(report.getUpdatedBy());
            reportType.setProgress(report.calculateProgress());
            reportType.setComplete(report.isComplete());

            // Map activities if present
            if (report.getActivities() != null && !report.getActivities().isEmpty()) {
                for (ActivityEntry activity : report.getActivities()) {
                    reportType.getActivities().add(mapActivityEntryToSoap(activity));
                }
            }

            return reportType;

        } catch (Exception e) {
            throw new RuntimeException("Error mapping daily report to SOAP type", e);
        }
    }

    /**
     * Map ActivityEntry domain object to SOAP type
     */
    private ActivityEntryType mapActivityEntryToSoap(ActivityEntry activity) {
        try {
            ActivityEntryType activityType = new ActivityEntryType();
            activityType.setId(activity.getId());
            activityType.setReportId(activity.getReportId());
            activityType.setDescription(activity.getDescription());
            activityType.setCategory(activity.getCategory());
            activityType.setStartTime(convertToXMLDateTime(activity.getStartTime()));
            activityType.setEndTime(convertToXMLDateTime(activity.getEndTime()));
            activityType.setProgress(activity.getProgress());
            activityType.setStatus(ActivityStatusType.fromValue(activity.getStatus().name()));
            activityType.setNotes(activity.getNotes());

            // Set personnel
            if (activity.getPersonnel() != null) {
                activityType.getPersonnel().addAll(activity.getPersonnel());
            }

            activityType.setCreatedAt(convertToXMLDateTime(activity.getCreatedAt()));
            activityType.setCreatedBy(activity.getCreatedBy());

            if (activity.getUpdatedAt() != null) {
                activityType.setUpdatedAt(convertToXMLDateTime(activity.getUpdatedAt()));
            }

            activityType.setUpdatedBy(activity.getUpdatedBy());
            activityType.setDurationMinutes(activity.calculateDuration().toMinutes());

            return activityType;

        } catch (Exception e) {
            throw new RuntimeException("Error mapping activity to SOAP type", e);
        }
    }

    /**
     * Map SOAP ActivityEntryType to domain ActivityEntry
     */
    private ActivityEntry mapSoapToActivityEntry(ActivityEntryType activityType) {
        ActivityEntry activity = new ActivityEntry();

        // Generate ID if not provided
        if (activityType.getId() == null || activityType.getId().isEmpty()) {
            activity.setId(UUID.randomUUID().toString());
        } else {
            activity.setId(activityType.getId());
        }

        activity.setReportId(activityType.getReportId());
        activity.setDescription(activityType.getDescription());
        activity.setCategory(activityType.getCategory());
        activity.setStartTime(convertToLocalDateTime(activityType.getStartTime()));
        activity.setEndTime(convertToLocalDateTime(activityType.getEndTime()));
        activity.setProgress(activityType.getProgress());
        activity.setStatus(ActivityStatus.valueOf(activityType.getStatus().value()));
        activity.setNotes(activityType.getNotes());

        // Set personnel
        if (!activityType.getPersonnel().isEmpty()) {
            activity.setPersonnel(new HashSet<>(activityType.getPersonnel()));
        }

        // Set created info if provided, otherwise use current
        if (activityType.getCreatedAt() != null) {
            activity.setCreatedAt(convertToLocalDateTime(activityType.getCreatedAt()));
        } else {
            activity.setCreatedAt(LocalDateTime.now());
        }

        if (activityType.getCreatedBy() != null && !activityType.getCreatedBy().isEmpty()) {
            activity.setCreatedBy(activityType.getCreatedBy());
        }

        // Set updated info if provided
        if (activityType.getUpdatedAt() != null) {
            activity.setUpdatedAt(convertToLocalDateTime(activityType.getUpdatedAt()));
        }

        activity.setUpdatedBy(activityType.getUpdatedBy());

        return activity;
    }

    /**
     * Helper method to convert LocalDate to XMLGregorianCalendar
     */
    private XMLGregorianCalendar convertToXMLDate(LocalDate date) throws Exception {
        GregorianCalendar gc = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
    }

    /**
     * Helper method to convert LocalDateTime to XMLGregorianCalendar
     */
    private XMLGregorianCalendar convertToXMLDateTime(LocalDateTime dateTime) throws Exception {
        if (dateTime == null) return null;
        GregorianCalendar gc = GregorianCalendar.from(dateTime.atZone(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
    }

    /**
     * Helper method to convert XMLGregorianCalendar to LocalDate
     */
    private LocalDate convertToLocalDate(XMLGregorianCalendar calendar) {
        return calendar.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }

    /**
     * Helper method to convert XMLGregorianCalendar to LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(XMLGregorianCalendar calendar) {
        if (calendar == null) return null;
        return calendar.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
    }

    /**
     * Create a SOAP fault exception with additional details
     */
    private RuntimeException createServiceFault(String message, String details) {
        ServiceFault fault = new ServiceFault();
        fault.setMessage(message);
        fault.setDetails(details);
        return new ServiceFaultException(message, fault);
    }

    /**
     * Custom Exception for SOAP faults
     */
    public static class ServiceFaultException extends RuntimeException {
        private final ServiceFault fault;

        public ServiceFaultException(String message, ServiceFault fault) {
            super(message);
            this.fault = fault;
        }

        public ServiceFault getFault() {
            return fault;
        }
    }
}