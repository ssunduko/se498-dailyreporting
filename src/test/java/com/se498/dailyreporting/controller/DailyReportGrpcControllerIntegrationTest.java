package com.se498.dailyreporting.controller;

import com.google.protobuf.StringValue;
import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.dto.grpc.GrpcMapper;
import com.se498.dailyreporting.grpc.*;
import com.se498.dailyreporting.grpc.Date;
import com.se498.dailyreporting.service.DailyReportingService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for DailyReportGrpcController
 * Tests the gRPC service endpoints with a running server
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DailyReportGrpcControllerIntegrationTest {

    @LocalServerPort
    private int webPort;

    // Assuming gRPC server is running on a fixed port or can be determined from configuration
    private static final int GRPC_PORT = 9090;

    @MockBean
    private DailyReportingService reportingService;

    @Autowired
    private GrpcMapper grpcMapper;

    private ManagedChannel channel;
    private DailyReportServiceGrpc.DailyReportServiceBlockingStub blockingStub;

    private final String TEST_PROJECT_ID = "test-project-" + UUID.randomUUID();
    private final String TEST_REPORT_ID = UUID.randomUUID().toString();
    private final String TEST_ACTIVITY_ID = UUID.randomUUID().toString();
    private final String TEST_USERNAME = "test-user";
    private final LocalDate TEST_DATE = LocalDate.now();

    private DailyReport testReport;
    private ActivityEntry testActivity;

    @BeforeAll
    public void setupChannel() {
        // Create a channel to the gRPC server
        channel = ManagedChannelBuilder.forAddress("localhost", GRPC_PORT)
                .usePlaintext() // For testing only, don't use in production
                .build();
        blockingStub = DailyReportServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    public void tearDown() throws Exception {
        // Shutdown the channel
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @BeforeEach
    public void setUp() {
        // Create test report
        testReport = new DailyReport();
        testReport.setId(TEST_REPORT_ID);
        testReport.setProjectId(TEST_PROJECT_ID);
        testReport.setReportDate(TEST_DATE);
        testReport.setStatus(ReportStatus.DRAFT);
        testReport.setNotes("Test Notes");
        testReport.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        testReport.setCreatedBy(TEST_USERNAME);

        // Create test activity
        testActivity = new ActivityEntry();
        testActivity.setId(TEST_ACTIVITY_ID);
        testActivity.setReportId(TEST_REPORT_ID);
        testActivity.setDescription("Test Activity");
        testActivity.setCategory("Test Category");
        testActivity.setStartTime(LocalDateTime.now().minusHours(2).truncatedTo(ChronoUnit.MILLIS));
        testActivity.setEndTime(LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MILLIS));
        testActivity.setProgress(50.0);
        testActivity.setStatus(ActivityStatus.IN_PROGRESS);
        testActivity.setNotes("Test Notes");
        testActivity.setCreatedBy(TEST_USERNAME);
        testActivity.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));

        // Set up personnel
        Set<String> personnel = new HashSet<>();
        personnel.add("Person 1");
        personnel.add("Person 2");
        testActivity.setPersonnel(personnel);

        // Add activity to report
        List<ActivityEntry> activities = new ArrayList<>();
        activities.add(testActivity);
        testReport.setActivities(activities);

        // Mock service methods
        when(reportingService.createReport(anyString(), any(LocalDate.class), anyString()))
                .thenReturn(testReport);
        when(reportingService.getReport(TEST_REPORT_ID))
                .thenReturn(Optional.of(testReport));
        when(reportingService.updateReport(eq(TEST_REPORT_ID), anyString(), anyString()))
                .thenReturn(testReport);
        when(reportingService.submitReport(TEST_REPORT_ID, TEST_USERNAME))
                .thenAnswer(inv -> {
                    testReport.setStatus(ReportStatus.SUBMITTED);
                    return testReport;
                });
        when(reportingService.approveReport(TEST_REPORT_ID, TEST_USERNAME))
                .thenAnswer(inv -> {
                    testReport.setStatus(ReportStatus.APPROVED);
                    return testReport;
                });
        when(reportingService.addActivityToReport(eq(TEST_REPORT_ID), any(ActivityEntry.class)))
                .thenReturn(testActivity);
        when(reportingService.getActivity(TEST_ACTIVITY_ID))
                .thenReturn(Optional.of(testActivity));
        when(reportingService.getActivitiesByReport(TEST_REPORT_ID))
                .thenReturn(Collections.singletonList(testActivity));
        when(reportingService.calculateReportProgress(TEST_REPORT_ID))
                .thenReturn(50.0);
        when(reportingService.isReportComplete(TEST_REPORT_ID))
                .thenReturn(false);
    }

    @Test
    @DisplayName("Test creating a report via gRPC")
    public void testCreateReport() {
        // Build request
        Date reportDate = Date.newBuilder()
                .setYear(TEST_DATE.getYear())
                .setMonth(TEST_DATE.getMonthValue())
                .setDay(TEST_DATE.getDayOfMonth())
                .build();

        CreateReportRequest request = CreateReportRequest.newBuilder()
                .setProjectId(TEST_PROJECT_ID)
                .setReportDate(reportDate)
                .setNotes(StringValue.of("Test Notes"))
                .setUsername(TEST_USERNAME)
                .build();

        // Call service
        DailyReportResponse response = blockingStub.createReport(request);

        // Verify response
        assertNotNull(response);
        assertEquals(TEST_REPORT_ID, response.getId());
        assertEquals(TEST_PROJECT_ID, response.getProjectId());
        assertEquals(ReportStatus.DRAFT.name(), response.getStatus().name());

        // Verify service was called
        verify(reportingService).createReport(eq(TEST_PROJECT_ID), any(LocalDate.class), eq(TEST_USERNAME));
    }

    @Test
    @DisplayName("Test getting a report via gRPC")
    public void testGetReport() {
        // Build request
        GetReportRequest request = GetReportRequest.newBuilder()
                .setReportId(TEST_REPORT_ID)
                .build();

        // Call service
        DailyReportResponse response = blockingStub.getReport(request);

        // Verify response
        assertNotNull(response);
        assertEquals(TEST_REPORT_ID, response.getId());
        assertEquals(TEST_PROJECT_ID, response.getProjectId());
        assertEquals(ReportStatus.DRAFT.name(), response.getStatus().name());

        // Verify service was called
        verify(reportingService).getReport(TEST_REPORT_ID);
    }

    @Test
    @DisplayName("Test submitting a report via gRPC")
    public void testSubmitReport() {
        // Build request
        SubmitReportRequest request = SubmitReportRequest.newBuilder()
                .setReportId(TEST_REPORT_ID)
                .setUsername(TEST_USERNAME)
                .build();

        // Call service
        DailyReportResponse response = blockingStub.submitReport(request);

        // Verify response
        assertNotNull(response);
        assertEquals(TEST_REPORT_ID, response.getId());
        assertEquals(ReportStatus.SUBMITTED.name(), response.getStatus().name());

        // Verify service was called
        verify(reportingService).submitReport(TEST_REPORT_ID, TEST_USERNAME);
    }

    @Test
    @DisplayName("Test approving a report via gRPC")
    public void testApproveReport() {
        // Build request
        ApproveReportRequest request = ApproveReportRequest.newBuilder()
                .setReportId(TEST_REPORT_ID)
                .setUsername(TEST_USERNAME)
                .build();

        // Call service
        DailyReportResponse response = blockingStub.approveReport(request);

        // Verify response
        assertNotNull(response);
        assertEquals(TEST_REPORT_ID, response.getId());
        assertEquals(ReportStatus.APPROVED.name(), response.getStatus().name());

        // Verify service was called
        verify(reportingService).approveReport(TEST_REPORT_ID, TEST_USERNAME);
    }

    @Test
    @DisplayName("Test adding an activity via gRPC")
    public void testAddActivity() {
        // Build request
        com.google.protobuf.Timestamp startTime =
                com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(testActivity.getStartTime().toEpochSecond(java.time.ZoneOffset.UTC))
                        .setNanos(testActivity.getStartTime().getNano())
                        .build();

        com.google.protobuf.Timestamp endTime =
                com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(testActivity.getEndTime().toEpochSecond(java.time.ZoneOffset.UTC))
                        .setNanos(testActivity.getEndTime().getNano())
                        .build();

        AddActivityRequest request = AddActivityRequest.newBuilder()
                .setReportId(TEST_REPORT_ID)
                .setDescription("Test Activity")
                .setCategory("Test Category")
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setProgress(50.0)
                .setStatus(com.se498.dailyreporting.grpc.ActivityStatus.IN_PROGRESS)
                .setNotes(StringValue.of("Test Notes"))
                .addPersonnel("Person 1")
                .addPersonnel("Person 2")
                .setUsername(TEST_USERNAME)
                .build();

        // Call service
        ActivityResponse response = blockingStub.addActivity(request);

        // Verify response
        assertNotNull(response);
        assertEquals(TEST_ACTIVITY_ID, response.getId());
        assertEquals(TEST_REPORT_ID, response.getReportId());
        assertEquals("Test Activity", response.getDescription());
        assertEquals("Test Category", response.getCategory());
        assertEquals(50.0, response.getProgress());
        assertEquals(com.se498.dailyreporting.grpc.ActivityStatus.IN_PROGRESS, response.getStatus());

        // Verify service was called
        verify(reportingService).addActivityToReport(eq(TEST_REPORT_ID), any(ActivityEntry.class));
    }

    @Test
    @DisplayName("Test getting activities by report via gRPC")
    public void testGetActivitiesByReport() {
        // Build request
        GetActivitiesByReportRequest request = GetActivitiesByReportRequest.newBuilder()
                .setReportId(TEST_REPORT_ID)
                .build();

        // Call service
        ActivityListResponse response = blockingStub.getActivitiesByReport(request);

        // Verify response
        assertNotNull(response);
        assertEquals(1, response.getActivitiesCount());
        ActivityResponse activity = response.getActivities(0);
        assertEquals(TEST_ACTIVITY_ID, activity.getId());
        assertEquals(TEST_REPORT_ID, activity.getReportId());
        assertEquals("Test Activity", activity.getDescription());

        // Verify service was called
        verify(reportingService).getActivitiesByReport(TEST_REPORT_ID);
    }

    @Test
    @DisplayName("Test calculating report progress via gRPC")
    public void testGetReportProgress() {
        // Build request
        GetReportProgressRequest request = GetReportProgressRequest.newBuilder()
                .setReportId(TEST_REPORT_ID)
                .build();

        // Call service
        DoubleResponse response = blockingStub.getReportProgress(request);

        // Verify response
        assertNotNull(response);
        assertEquals(50.0, response.getValue());

        // Verify service was called
        verify(reportingService).calculateReportProgress(TEST_REPORT_ID);
    }

    @Test
    @DisplayName("Test checking if report is complete via gRPC")
    public void testIsReportComplete() {
        // Build request
        IsReportCompleteRequest request = IsReportCompleteRequest.newBuilder()
                .setReportId(TEST_REPORT_ID)
                .build();

        // Call service
        BooleanResponse response = blockingStub.isReportComplete(request);

        // Verify response
        assertNotNull(response);
        assertFalse(response.getValue());

        // Verify service was called
        verify(reportingService).isReportComplete(TEST_REPORT_ID);
    }

    @Test
    @DisplayName("Test getting a non-existent report")
    public void testGetNonExistentReport() {
        // Mock service to return empty
        when(reportingService.getReport("non-existent-id")).thenReturn(Optional.empty());

        // Build request
        GetReportRequest request = GetReportRequest.newBuilder()
                .setReportId("non-existent-id")
                .build();

        // Call service and expect exception
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> {
            blockingStub.getReport(request);
        });

        // Verify exception status
        assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
    }
}