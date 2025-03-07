package com.se498.dailyreporting.controller;

import com.google.protobuf.StringValue;
import com.se498.dailyreporting.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DailyReportGrpcClientTest {

    private static final String SERVER_NAME = InProcessServerBuilder.generateName();

    private io.grpc.Server server;
    private ManagedChannel channel;
    private DailyReportGrpcClient client;

    // Create a test implementation of the gRPC service
    private static class TestDailyReportService extends DailyReportServiceGrpc.DailyReportServiceImplBase {
        // We'll override methods as needed in each test
    }

    private TestDailyReportService serviceImpl;

    @BeforeEach
    public void setUp() throws IOException {
        // Create the service implementation
        serviceImpl = spy(new TestDailyReportService());

        // Create the server
        server = InProcessServerBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .addService(serviceImpl)
                .build();

        // Start the server
        server.start();
        System.out.println("Started test gRPC server: " + SERVER_NAME);

        // Create the channel
        channel = InProcessChannelBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .build();

        // Create the client
        client = new DailyReportGrpcClient(channel);
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
    @DisplayName("Should successfully create report")
    public void createReport_Success() {
        // Arrange
        String projectId = "test-project";
        LocalDate reportDate = LocalDate.now();
        String notes = "Test notes";
        String username = "test-user";
        String reportId = "test-report-id";

        // Prepare the mock response
        final DailyReportResponse mockResponse = DailyReportResponse.newBuilder()
                .setId(reportId)
                .setProjectId(projectId)
                .setStatus(ReportStatus.DRAFT)
                .setNotes(StringValue.of(notes))
                .build();

        // Override the createReport method to return our mock response
        doAnswer(invocation -> {
            CreateReportRequest request = invocation.getArgument(0);
            StreamObserver<DailyReportResponse> responseObserver = invocation.getArgument(1);

            // Verify request data
            assertEquals(projectId, request.getProjectId());
            assertEquals(reportDate.getYear(), request.getReportDate().getYear());
            assertEquals(reportDate.getMonthValue(), request.getReportDate().getMonth());
            assertEquals(reportDate.getDayOfMonth(), request.getReportDate().getDay());

            // Return the mock response
            responseObserver.onNext(mockResponse);
            responseObserver.onCompleted();
            return null;
        }).when(serviceImpl).createReport(any(CreateReportRequest.class), any(StreamObserver.class));

        // Act
        DailyReportResponse response = client.createReport(projectId, reportDate, notes, username);

        // Assert
        assertNotNull(response);
        assertEquals(reportId, response.getId());
        assertEquals(projectId, response.getProjectId());
        assertEquals(ReportStatus.DRAFT, response.getStatus());
        assertEquals(notes, response.getNotes().getValue());

        // Verify the service method was called
        verify(serviceImpl).createReport(any(CreateReportRequest.class), any(StreamObserver.class));
    }

    @Test
    @DisplayName("Should handle exception when creating report")
    public void createReport_Exception() {
        // Arrange
        String projectId = "test-project";
        LocalDate reportDate = LocalDate.now();
        String notes = "Test notes";
        String username = "test-user";

        StatusRuntimeException exception = new StatusRuntimeException(
                Status.INTERNAL.withDescription("Internal server error"));

        // Override the createReport method to throw an exception
        doAnswer(invocation -> {
            StreamObserver<DailyReportResponse> responseObserver = invocation.getArgument(1);
            responseObserver.onError(exception);
            return null;
        }).when(serviceImpl).createReport(any(CreateReportRequest.class), any(StreamObserver.class));

        // Act & Assert
        StatusRuntimeException thrownException = assertThrows(
                StatusRuntimeException.class,
                () -> client.createReport(projectId, reportDate, notes, username)
        );

        assertEquals(Status.Code.INTERNAL, thrownException.getStatus().getCode());
        assertEquals("Internal server error", thrownException.getStatus().getDescription());

        // Verify the service method was called
        verify(serviceImpl).createReport(any(CreateReportRequest.class), any(StreamObserver.class));
    }

    @Test
    @DisplayName("Should successfully get activities by report")
    public void getActivitiesByReport_Success() {
        // Arrange
        String reportId = "test-report-id";

        ActivityResponse activity1 = ActivityResponse.newBuilder()
                .setId("activity-1")
                .setReportId(reportId)
                .build();

        ActivityResponse activity2 = ActivityResponse.newBuilder()
                .setId("activity-2")
                .setReportId(reportId)
                .build();

        ActivityListResponse mockResponse = ActivityListResponse.newBuilder()
                .addActivities(activity1)
                .addActivities(activity2)
                .build();

        // Capture the request for verification
        ArgumentCaptor<GetActivitiesByReportRequest> requestCaptor =
                ArgumentCaptor.forClass(GetActivitiesByReportRequest.class);

        // Override the service method
        doAnswer(invocation -> {
            StreamObserver<ActivityListResponse> responseObserver = invocation.getArgument(1);
            responseObserver.onNext(mockResponse);
            responseObserver.onCompleted();
            return null;
        }).when(serviceImpl).getActivitiesByReport(requestCaptor.capture(), any(StreamObserver.class));

        // Act
        List<ActivityResponse> activities = client.getActivitiesByReport(reportId);

        // Assert
        assertNotNull(activities);
        assertEquals(2, activities.size());
        assertEquals("activity-1", activities.get(0).getId());
        assertEquals("activity-2", activities.get(1).getId());

        // Verify request contents
        assertEquals(reportId, requestCaptor.getValue().getReportId());

        // Verify the service method was called
        verify(serviceImpl).getActivitiesByReport(any(GetActivitiesByReportRequest.class), any(StreamObserver.class));
    }

    // Add more tests as needed following the same pattern
}