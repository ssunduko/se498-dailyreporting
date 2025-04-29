package com.se498.dailyreporting.controller;

import com.se498.dailyreporting.domain.bo.User;
import com.se498.dailyreporting.dto.UserRequest;
import com.se498.dailyreporting.dto.UserResponse;
import com.se498.dailyreporting.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    @Autowired
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userService.findAllUsers();
        List<UserResponse> response = users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        log.info("Fetching user with ID: {}", id);
        return userService.findUserById(id)
                .map(user -> ResponseEntity.ok(mapToUserResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new user",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid user data")
            })
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest request) {
        log.info("Creating user with username: {}", request.getUsername());

        // Check if username or email already exists
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().build();
        }

        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().build();
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Note: In a real application, this should be encrypted
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);

        User savedUser = userService.saveUser(user);
        return new ResponseEntity<>(mapToUserResponse(savedUser), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @RequestBody @Valid UserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Updating user with ID: {}", id);

        Optional<User> userOpt = userService.findUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        // Check if the user being updated is the authenticated user or if the authenticated user is an admin
        if (!userDetails.getUsername().equals(user.getUsername()) &&
                !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check if email is already in use by another user
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().build();
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            // Note: In a real application, this should be encrypted
            user.setPassword(request.getPassword());
        }

        // Only admins can update user roles
        if (request.getRole() != null &&
                userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            user.setRole(request.getRole());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userService.saveUser(user);
        return ResponseEntity.ok(mapToUserResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Deleting user with ID: {}", id);

        Optional<User> userOpt = userService.findUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        // Check if the user being deleted is the authenticated user or if the authenticated user is an admin
        if (!userDetails.getUsername().equals(user.getUsername()) &&
                !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a user")
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Deactivating user with ID: {}", id);

        Optional<User> userOpt = userService.findUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        // Only admins can deactivate other users
        if (!userDetails.getUsername().equals(user.getUsername()) &&
                !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userService.saveUser(user);
        return ResponseEntity.ok(mapToUserResponse(updatedUser));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a user")
    public ResponseEntity<UserResponse> activateUser(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Activating user with ID: {}", id);

        // Only admins can activate users
        if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<User> userOpt = userService.findUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userService.saveUser(user);
        return ResponseEntity.ok(mapToUserResponse(updatedUser));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by username or email")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {

        log.info("Searching users with username: {}, email: {}", username, email);

        List<User> users;
        if (username != null && !username.isEmpty()) {
            users = userService.findByUsernameContaining(username);
        } else if (email != null && !email.isEmpty()) {
            users = userService.findByEmailContaining(email);
        } else {
            return ResponseEntity.badRequest().build();
        }

        List<UserResponse> response = users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Helper method to map User to UserResponse
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}