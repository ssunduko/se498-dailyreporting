package com.se498.dailyreporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se498.dailyreporting.domain.bo.User;
import com.se498.dailyreporting.dto.UserRequest;
import com.se498.dailyreporting.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Controller Mock Tests")
class UserControllerMockTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private final String testId = UUID.randomUUID().toString();
    private final String testUsername = "testuser";
    private final String testEmail = "test@example.com";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization

        // Create test user
        testUser = new User();
        testUser.setId(testId);
        testUser.setUsername(testUsername);
        testUser.setEmail(testEmail);
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole("USER");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setActive(true);
    }

    @Test
    @DisplayName("GET /users - Should return all users")
    void shouldReturnAllUsers() throws Exception {
        // Given
        User secondUser = new User();
        secondUser.setId(UUID.randomUUID().toString());
        secondUser.setUsername("seconduser");
        secondUser.setEmail("second@example.com");

        when(userService.findAllUsers()).thenReturn(Arrays.asList(testUser, secondUser));

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(testId))
                .andExpect(jsonPath("$[0].username").value(testUsername))
                .andExpect(jsonPath("$[1].username").value("seconduser"));

        verify(userService).findAllUsers();
    }

    @Test
    @DisplayName("GET /users/{id} - Should return user by ID")
    void shouldReturnUserById() throws Exception {
        // Given
        when(userService.findUserById(testId)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/users/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testId))
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.email").value(testEmail));

        verify(userService).findUserById(testId);
    }

    @Test
    @DisplayName("GET /users/{id} - Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        // Given
        String nonExistentId = "non-existent-id";
        when(userService.findUserById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(userService).findUserById(nonExistentId);
    }

    @Test
    @DisplayName("POST /users - Should create new user")
    void shouldCreateNewUser() throws Exception {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(testUsername);
        userRequest.setEmail(testEmail);
        userRequest.setPassword("password123");
        userRequest.setFirstName("Test");
        userRequest.setLastName("User");
        userRequest.setRole("USER");

        when(userService.existsByUsername(testUsername)).thenReturn(false);
        when(userService.existsByEmail(testEmail)).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.email").value(testEmail));

        verify(userService).existsByUsername(testUsername);
        verify(userService).existsByEmail(testEmail);
        verify(userService).saveUser(any(User.class));
    }

    @Test
    @DisplayName("POST /users - Should return 400 when username exists")
    void shouldReturn400WhenUsernameExists() throws Exception {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(testUsername);
        userRequest.setEmail("new@example.com");
        userRequest.setPassword("password123");

        when(userService.existsByUsername(testUsername)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());

        verify(userService).existsByUsername(testUsername);
        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    @DisplayName("POST /users - Should return 400 when email exists")
    void shouldReturn400WhenEmailExists() throws Exception {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("newuser");
        userRequest.setEmail(testEmail);
        userRequest.setPassword("password123");

        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail(testEmail)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());

        verify(userService).existsByUsername("newuser");
        verify(userService).existsByEmail(testEmail);
        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    @DisplayName("GET /users/search - Should search users by username")
    void shouldSearchUsersByUsername() throws Exception {
        // Given
        User user1 = new User();
        user1.setId(UUID.randomUUID().toString());
        user1.setUsername("testuser1");
        User user2 = new User();
        user2.setId(UUID.randomUUID().toString());
        user2.setUsername("testuser2");

        when(userService.findByUsernameContaining("test")).thenReturn(Arrays.asList(user1, user2));

        // When & Then
        mockMvc.perform(get("/users/search")
                        .param("username", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser1"))
                .andExpect(jsonPath("$[1].username").value("testuser2"));

        verify(userService).findByUsernameContaining("test");
    }

    @Test
    @DisplayName("GET /users/search - Should search users by email")
    void shouldSearchUsersByEmail() throws Exception {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail("test@example.com");

        when(userService.findByEmailContaining("example")).thenReturn(Collections.singletonList(user));

        // When & Then
        mockMvc.perform(get("/users/search")
                        .param("email", "example"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@example.com"));

        verify(userService).findByEmailContaining("example");
    }

    @Test
    @DisplayName("GET /users/search - Should return 400 when no search parameters")
    void shouldReturn400WhenNoSearchParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/search"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}