package com.se498.dailyreporting.controller;

import com.google.protobuf.StringValue;
import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.grpc.*;
import com.se498.dailyreporting.service.DailyReportingService;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DailyReportGrpcControllerTest {

    @Mock
    private DailyReportingService reportingService;

    @InjectMocks
    private DailyReportGrpcController controller;

    private DailyReport mockDailyReport;
    private ActivityEntry mockActivityEntry;

    @BeforeEach
    public void setUp() {
        // Set up mock report
        mockDailyReport = new DailyReport();
        mockDailyReport.setId("test-report-id");
        mockDailyReport.setProjectId("test-project");
        mockDailyReport.setReportDate(LocalDate.of(2024, 1, 1));
        mockDailyReport.setStatus(ReportStatus.DRAFT);
        mockDailyReport.setCreatedBy("test-user");
        mockDailyReport.setCreatedAt(LocalDateTime.now());

        // Set up mock activity
        mockActivityEntry = new ActivityEntry();
        mockActivityEntry.setId("test-activity-id");
        mockActivityEntry.setReportId("test-report-id");
        mockActivityEntry.setDescription("Test Activity");
        mockActivityEntry.setCategory("Testing");
        mockActivityEntry.setStartTime(LocalDateTime.now().minusHours(2));
        mockActivityEntry.setEndTime(LocalDateTime.now().minusHours(1));
        mockActivityEntry.setProgress(75.0);
        mockActivityEntry.setStatus(ActivityStatus.IN_PROGRESS);
        mockActivityEntry.setCreatedBy("test-user");
        mockActivityEntry.setCreatedAt(LocalDateTime.now());

        // Add activity to report
        List<ActivityEntry> activities = new ArrayList<>();
        activities.add(mockActivityEntry);
        mockDailyReport.setActivities(activities);
    }

    @Test
    public void testCreateReport() throws Exception {
        // Prepare request
        CreateReportRequest request = CreateReportRequest.newBuilder()
                .setProjectId("test-project")
                .setReportDate(com.se498.dailyreporting.grpc.Date.newBuilder()
                        .setYear(2024)
                        .setMonth(1)
                        .setDay(1)
                        .build())
                .setNotes(StringValue.of("Test notes"))
                .setUsername("test-user")
                .build();

        // Set up mock behavior
        when(reportingService.createReport(anyString(), any(LocalDate.class), anyString()))
                .thenReturn(mockDailyReport);
        when(reportingService.updateReport(anyString(), anyString(), anyString()))
                .thenReturn(mockDailyReport);

        // Call the gRPC method
        StreamRecorder<DailyReportResponse> responseObserver = StreamRecorder.create();
        controller.createReport(request, responseObserver);

        // Wait and verify
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail("The call did not terminate in time");
        }

        // Verify response
        List<DailyReportResponse> results = responseObserver.getValues();
        assertThat(results).hasSize(1);
        DailyReportResponse response = results.get(0);
        assertThat(response.getReport().getId()).isEqualTo("test-report-id");
        assertThat(response.getReport().getProjectId()).isEqualTo("test-project");

        // Verify service calls
        verify(reportingService).createReport(eq("test-project"), any(LocalDate.class), eq("test-user"));
        verify(reportingService).updateReport(eq("test-report-id"), eq("Test notes"), eq("test-user"));
    }

    @Test
    public void testGetReport() throws Exception {
        // Prepare request
        GetReportRequest request = GetReportRequest.newBuilder()
                .setReportId("test-report-id")
                .build();

        // Set up mock behavior
        when(reportingService.getReport("test-report-id"))
                .thenReturn(Optional.of(mockDailyReport));

        // Call the gRPC method
        StreamRecorder<DailyReportResponse> responseObserver = StreamRecorder.create();
        controller.getReport(request, responseObserver);

        // Wait and verify
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail("The call did not terminate in time");
        }

        // Verify response
        List<DailyReportResponse> results = responseObserver.getValues();
        assertThat(results).hasSize(1);
        DailyReportResponse response = results.get(0);
        assertThat(response.getReport().getId()).isEqualTo("test-report-id");

        // Verify service call
        verify(reportingService).getReport("test-report-id");
    }

    @Test
    public void testGetReportNotFound() throws Exception {
        // Prepare request
        GetReportRequest request = GetReportRequest.newBuilder()
                .setReportId("non-existent-id")
                .build();

        // Set up mock behavior
        when(reportingService.getReport("non-existent-id"))
                .thenReturn(Optional.empty());

        // Call the gRPC method
        StreamRecorder<DailyReportResponse> responseObserver = StreamRecorder.create();
        controller.getReport(request, responseObserver);

        // Wait and verify
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail("The call did not terminate in time");
        }

        // Verify response
        List<DailyReportResponse> results = responseObserver.getValues();
        assertThat(results).hasSize(1);
        DailyReportResponse response = results.get(0);
        assertThat(response.hasReport()).isFalse();

        // Verify service call
        verify(reportingService).getReport("non-existent-id");
    }

    @Test
    public void testGetReportsByProject() throws Exception {
        // Prepare request
        GetReportsByProjectRequest request = GetReportsByProjectRequest.newBuilder()
                .setProjectId("test-project")
                .build();

        // Set up mock behavior
        List<DailyReport> mockReports = Collections.singletonList(mockDailyReport);
        when(reportingService.getReportsByProject("test-project"))
                .thenReturn(mockReports);

        // Call the gRPC method
        StreamRecorder<GetReportsByProjectResponse> responseObserver = StreamRecorder.create();
        controller.getReportsByProject(request, responseObserver);

        // Wait and verify
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail("The call did not terminate in time");
        }

        // Verify response
        List<GetReportsByProjectResponse> results = responseObserver.getValues();
        assertThat(results).hasSize(1);
        GetReportsByProjectResponse response = results.get(0);
        assertThat(response.getReportsList()).hasSize(1);
        assertThat(response.getReports(0).getId()).isEqualTo("test-report-id");

        // Verify service call
        verify(reportingService).getReportsByProject("test-project");
    }

    @Test
    public void testAddActivity() throws Exception {
        // Prepare request
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        LocalDateTime endTime = LocalDateTime.now().minusHours(1);

        AddActivityRequest request = AddActivityRequest.newBuilder()
                .setReportId("test-report-id")
                .setDescription("Test Activity")
                .setCategory("Testing")
                .setStartTime(toProtoTimestamp(startTime))
                .setEndTime(toProtoTimestamp(endTime))
                .setProgress(75.0)
                .setStatus(com.se498.dailyreporting.grpc.ActivityStatus.ACTIVITY_STATUS_IN_PROGRESS)
                .setUsername("test-user")
                .build();

        // Set up mock behavior
        when(reportingService.addActivityToReport(eq("test-report-id"), any(ActivityEntry.class)))
                .thenReturn(mockActivityEntry);

        // Call the gRPC method
        StreamRecorder<ActivityResponse> responseObserver = StreamRecorder.create();
        controller.addActivity(request, responseObserver);

        // Wait and verify
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail("The call did not terminate in time");
        }

        // Verify response
        List<ActivityResponse> results = responseObserver.getValues();
        assertThat(results).hasSize(1);
        ActivityResponse response = results.get(0);
        assertThat(response.getActivity().getId()).isEqualTo("test-activity-id");
        assertThat(response.getActivity().getDescription()).isEqualTo("Test Activity");

        // Verify service call
        verify(reportingService).addActivityToReport(eq("test-report-id"), any(ActivityEntry.class));
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }

    private com.google.protobuf.Timestamp toProtoTimestamp(LocalDateTime localDateTime) {
        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond())
                .setNanos(localDateTime.getNano())
                .build();
    }
}