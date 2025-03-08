package com.se498.dailyreporting.controller;

import com.google.protobuf.StringValue;
import com.se498.dailyreporting.domain.bo.ActivityEntry;
import com.se498.dailyreporting.domain.bo.DailyReport;
import com.se498.dailyreporting.domain.vo.ActivityStatus;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.dto.grpc.GrpcMapper;
import com.se498.dailyreporting.grpc.*;
import com.se498.dailyreporting.service.DailyReportingService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DailyReportGrpcControllerIntegrationSpyTest {

    private static final String SERVER_NAME = InProcessServerBuilder.generateName();

    @Mock
    private DailyReportingService reportingService;

    @Spy
    private GrpcMapper grpcMapper = new GrpcMapper();

    private Server server;
    private ManagedChannel channel;
    private DailyReportServiceGrpc.DailyReportServiceBlockingStub blockingStub;

    // Test data
    private final String TEST_PROJECT_ID = "test-project-" + UUID.randomUUID();
    private final String TEST_REPORT_ID = UUID.randomUUID().toString();
    private final String TEST_ACTIVITY_ID = UUID.randomUUID().toString();
    private final String TEST_USERNAME = "test-user";
    private final LocalDate TEST_DATE = LocalDate.now();

    private DailyReport testReport;
    private ActivityEntry testActivity;

    @BeforeEach
    public void setUp() throws IOException {
        // Set up test data
        setupTestData();

        // Configure mock service behavior
        setupMockServiceBehavior();

        // Create and start the server
        DailyReportGrpcController grpcController = new DailyReportGrpcController(reportingService, grpcMapper);

        server = InProcessServerBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .addService(grpcController)
                .build()
                .start();

        System.out.println("Started test gRPC server: " + SERVER_NAME);

        // Create the channel
        channel = InProcessChannelBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .build();

        // Create the blocking stub
        blockingStub = DailyReportServiceGrpc.newBlockingStub(channel);
    }

    private void setupTestData() {
        // Create test report
        testReport = new DailyReport();
        testReport.setId(TEST_REPORT_ID);
        testReport.setProjectId(TEST_PROJECT_ID);
        testReport.setReportDate(TEST_DATE);
        testReport.setStatus(ReportStatus.DRAFT);
        testReport.setNotes("Test Notes");
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setCreatedBy(TEST_USERNAME);
        testReport.setActivities(new ArrayList<>());

        // Create test activity
        testActivity = new ActivityEntry();
        testActivity.setId(TEST_ACTIVITY_ID);
        testActivity.setReportId(TEST_REPORT_ID);
        testActivity.setDescription("Test Activity");
        testActivity.setCategory("Test Category");
        testActivity.setStartTime(LocalDateTime.now().minusHours(2));
        testActivity.setEndTime(LocalDateTime.now().minusHours(1));
        testActivity.setProgress(50.0);
        testActivity.setStatus(ActivityStatus.IN_PROGRESS);
        testActivity.setNotes("Test Notes");
        testActivity.setCreatedBy(TEST_USERNAME);
        testActivity.setCreatedAt(LocalDateTime.now());
    }

    private void setupMockServiceBehavior() {
        // Only setup stubs for methods that are actually tested
        when(reportingService.createReport(anyString(), any(LocalDate.class), anyString()))
                .thenReturn(testReport);

        when(reportingService.getReport(TEST_REPORT_ID))
                .thenReturn(Optional.of(testReport));

        when(reportingService.submitReport(TEST_REPORT_ID, TEST_USERNAME))
                .thenAnswer(inv -> {
                    testReport.setStatus(ReportStatus.SUBMITTED);
                    return testReport;
                });

        when(reportingService.getActivitiesByReport(TEST_REPORT_ID))
                .thenReturn(Collections.singletonList(testActivity));

        when(reportingService.calculateReportProgress(TEST_REPORT_ID))
                .thenReturn(50.0);

        // Mark other stubs as lenient so they don't cause UnnecessaryStubbingException
        lenient().when(reportingService.updateReport(eq(TEST_REPORT_ID), anyString(), anyString()))
                .thenReturn(testReport);

        lenient().when(reportingService.approveReport(TEST_REPORT_ID, TEST_USERNAME))
                .thenAnswer(inv -> {
                    testReport.setStatus(ReportStatus.APPROVED);
                    return testReport;
                });

        lenient().when(reportingService.addActivityToReport(eq(TEST_REPORT_ID), any(ActivityEntry.class)))
                .thenReturn(testActivity);

        lenient().when(reportingService.getActivity(TEST_ACTIVITY_ID))
                .thenReturn(Optional.of(testActivity));

        lenient().when(reportingService.isReportComplete(TEST_REPORT_ID))
                .thenReturn(false);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }

        if (server != null) {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Shut down test gRPC server");
        }
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
        assertEquals(50.0, response.getValue(), 0.001);

        // Verify service was called
        verify(reportingService).calculateReportProgress(TEST_REPORT_ID);
    }

    // To test the stubs marked as lenient, add the corresponding test methods here...
}