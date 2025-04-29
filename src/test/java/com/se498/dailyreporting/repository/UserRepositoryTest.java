package com.se498.dailyreporting.repository;

import com.se498.dailyreporting.TestDailyReportingApplication;
import com.se498.dailyreporting.domain.bo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestDailyReportingApplication.class)
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private final String testUsername = "testuser";
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        // Clear previous test data
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setId(UUID.randomUUID().toString());
        testUser.setUsername(testUsername);
        testUser.setEmail(testEmail);
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole("USER");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setActive(true);

        // Save test user
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        // When
        Optional<User> foundUser = userRepository.findById(testUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        assertEquals(testUsername, foundUser.get().getUsername());
        assertEquals(testEmail, foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        // When
        Optional<User> foundUser = userRepository.findByUsername(testUsername);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        assertEquals(testUsername, foundUser.get().getUsername());
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail(testEmail);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        assertEquals(testEmail, foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Should check if user exists by username")
    void shouldCheckIfUserExistsByUsername() {
        // When & Then
        assertTrue(userRepository.existsByUsername(testUsername));
        assertFalse(userRepository.existsByUsername("nonexistentuser"));
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void shouldCheckIfUserExistsByEmail() {
        // When & Then
        assertTrue(userRepository.existsByEmail(testEmail));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    @DisplayName("Should update user")
    void shouldUpdateUser() {
        // Given
        String newFirstName = "Updated";
        String newLastName = "Name";

        // When
        testUser.setFirstName(newFirstName);
        testUser.setLastName(newLastName);
        testUser.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(testUser);

        // Then
        assertEquals(testUser.getId(), updatedUser.getId());
        assertEquals(newFirstName, updatedUser.getFirstName());
        assertEquals(newLastName, updatedUser.getLastName());
        assertNotNull(updatedUser.getUpdatedAt());
    }

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() {
        // When
        userRepository.deleteById(testUser.getId());

        // Then
        assertFalse(userRepository.findById(testUser.getId()).isPresent());
    }

    @Test
    @DisplayName("Should handle multiple users")
    void shouldHandleMultipleUsers() {
        // Given
        User secondUser = new User();
        secondUser.setId(UUID.randomUUID().toString());
        secondUser.setUsername("seconduser");
        secondUser.setEmail("second@example.com");
        secondUser.setPassword("password456");
        secondUser.setRole("ADMIN");
        secondUser.setCreatedAt(LocalDateTime.now());
        secondUser.setActive(true);

        userRepository.save(secondUser);

        // When
        long count = userRepository.count();

        // Then
        assertEquals(2, count);
    }
}