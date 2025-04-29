package com.se498.dailyreporting.repository;

import com.se498.dailyreporting.TestDailyReportingApplication;
import com.se498.dailyreporting.domain.bo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {TestDailyReportingApplication.class, FakeUserRepository.class})
@ActiveProfiles("test")
@DisplayName("User Repository Mock Tests")
class UserRepositoryMockTest {

    @Autowired
    private UserRepository userRepository;
    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        // Clear any existing test data
        userRepository.deleteAll();

        // Create test users
        testUser1 = new User();
        testUser1.setId(UUID.randomUUID().toString());
        testUser1.setUsername("testuser1");
        testUser1.setEmail("test1@example.com");
        testUser1.setPassword("password123");
        testUser1.setFirstName("Test");
        testUser1.setLastName("User1");
        testUser1.setRole("USER");
        testUser1.setCreatedAt(LocalDateTime.now());
        testUser1.setActive(true);

        testUser2 = new User();
        testUser2.setId(UUID.randomUUID().toString());
        testUser2.setUsername("testuser2");
        testUser2.setEmail("test2@example.com");
        testUser2.setPassword("password456");
        testUser2.setFirstName("Test");
        testUser2.setLastName("User2");
        testUser2.setRole("ADMIN");
        testUser2.setCreatedAt(LocalDateTime.now());
        testUser2.setActive(true);

        testUser3 = new User();
        testUser3.setId(UUID.randomUUID().toString());
        testUser3.setUsername("inactiveuser");
        testUser3.setEmail("inactive@example.com");
        testUser3.setPassword("password789");
        testUser3.setFirstName("Inactive");
        testUser3.setLastName("User");
        testUser3.setRole("USER");
        testUser3.setCreatedAt(LocalDateTime.now());
        testUser3.setActive(false);
    }

    @Nested
    @DisplayName("Repository Operations Tests")
    class RepositoryOperationsTests {

        @Test
        @DisplayName("Should save users and find them by ID")
        void shouldSaveUsersAndFindThemById() {
            // Given & When
            User savedUser1 = userRepository.save(testUser1);
            User savedUser2 = userRepository.save(testUser2);

            // Then
            assertNotNull(savedUser1, "Saved user should not be null");
            assertEquals(testUser1.getId(), savedUser1.getId(), "ID should match");

            Optional<User> foundUser1 = userRepository.findById(testUser1.getId());
            Optional<User> foundUser2 = userRepository.findById(testUser2.getId());

            assertTrue(foundUser1.isPresent(), "User 1 should be found by ID");
            assertTrue(foundUser2.isPresent(), "User 2 should be found by ID");
            assertEquals("testuser1", foundUser1.get().getUsername(), "Username should match");
            assertEquals("ADMIN", foundUser2.get().getRole(), "Role should match");
        }

        @Test
        @DisplayName("Should find all users")
        void shouldFindAllUsers() {
            // Given
            userRepository.save(testUser1);
            userRepository.save(testUser2);
            userRepository.save(testUser3);

            // When
            List<User> allUsers = userRepository.findAll();

            // Then
            assertEquals(3, allUsers.size(), "Should find all 3 users");
            assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("testuser1")),
                    "Should include testuser1");
            assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("testuser2")),
                    "Should include testuser2");
            assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("inactiveuser")),
                    "Should include inactiveuser");
        }

        @Test
        @DisplayName("Should save all users")
        void shouldSaveAllUsers() {
            // Given
            List<User> usersToSave = Arrays.asList(testUser1, testUser2);

            // When
            List<User> savedUsers = userRepository.saveAll(usersToSave);

            // Then
            assertEquals(2, savedUsers.size(), "Should save all 2 users");

            // Verify all users can be found
            List<User> allUsers = userRepository.findAll();
            assertEquals(2, allUsers.size(), "Should find all 2 saved users");
        }

        @Test
        @DisplayName("Should delete user by ID")
        void shouldDeleteUserById() {
            // Given
            userRepository.save(testUser1);
            userRepository.save(testUser2);

            // When
            userRepository.deleteById(testUser1.getId());

            // Then
            Optional<User> deletedUser = userRepository.findById(testUser1.getId());
            Optional<User> remainingUser = userRepository.findById(testUser2.getId());

            assertFalse(deletedUser.isPresent(), "Deleted user should not be found");
            assertTrue(remainingUser.isPresent(), "Remaining user should still be found");

            List<User> allUsers = userRepository.findAll();
            assertEquals(1, allUsers.size(), "Should have 1 user remaining");
        }

        @Test
        @DisplayName("Should delete user entity")
        void shouldDeleteUserEntity() {
            // Given
            User savedUser1 = userRepository.save(testUser1);
            userRepository.save(testUser2);

            // When
            userRepository.delete(savedUser1);

            // Then
            Optional<User> deletedUser = userRepository.findById(testUser1.getId());
            assertFalse(deletedUser.isPresent(), "Deleted user should not be found");

            List<User> allUsers = userRepository.findAll();
            assertEquals(1, allUsers.size(), "Should have 1 user remaining");
        }

        @Test
        @DisplayName("Should delete all users")
        void shouldDeleteAllUsers() {
            // Given
            userRepository.save(testUser1);
            userRepository.save(testUser2);

            // When
            userRepository.deleteAll();

            // Then
            List<User> allUsers = userRepository.findAll();
            assertTrue(allUsers.isEmpty(), "All users should be deleted");
        }

        @Test
        @DisplayName("Should count users")
        void shouldCountUsers() {
            // Given
            userRepository.save(testUser1);
            userRepository.save(testUser2);

            // When
            long count = userRepository.count();

            // Then
            assertEquals(2, count, "Should count 2 users");
        }

        @Test
        @DisplayName("Should check if user exists by ID")
        void shouldCheckIfUserExistsById() {
            // Given
            userRepository.save(testUser1);

            // When
            boolean exists = userRepository.existsById(testUser1.getId());
            boolean notExists = userRepository.existsById("non-existent-id");

            // Then
            assertTrue(exists, "User should exist");
            assertFalse(notExists, "Non-existent user should not exist");
        }
    }

    @Nested
    @DisplayName("User-Specific Repository Method Tests")
    class UserSpecificRepositoryMethodTests {

        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
            // Given
            userRepository.save(testUser1);
            userRepository.save(testUser2);

            // When
            Optional<User> foundUser = userRepository.findByUsername("testuser1");
            Optional<User> notFoundUser = userRepository.findByUsername("nonexistent");

            // Then
            assertTrue(foundUser.isPresent(), "User should be found by username");
            assertEquals(testUser1.getId(), foundUser.get().getId(), "User ID should match");
            assertFalse(notFoundUser.isPresent(), "Non-existent user should not be found");
        }

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // Given
            userRepository.save(testUser1);
            userRepository.save(testUser2);

            // When
            Optional<User> foundUser = userRepository.findByEmail("test2@example.com");
            Optional<User> notFoundUser = userRepository.findByEmail("nonexistent@example.com");

            // Then
            assertTrue(foundUser.isPresent(), "User should be found by email");
            assertEquals(testUser2.getId(), foundUser.get().getId(), "User ID should match");
            assertFalse(notFoundUser.isPresent(), "Non-existent user should not be found");
        }

        @Test
        @DisplayName("Should check if user exists by username")
        void shouldCheckIfUserExistsByUsername() {
            // Given
            userRepository.save(testUser1);

            // When
            boolean exists = userRepository.existsByUsername("testuser1");
            boolean notExists = userRepository.existsByUsername("nonexistent");

            // Then
            assertTrue(exists, "User should exist by username");
            assertFalse(notExists, "Non-existent user should not exist");
        }

        @Test
        @DisplayName("Should check if user exists by email")
        void shouldCheckIfUserExistsByEmail() {
            // Given
            userRepository.save(testUser1);

            // When
            boolean exists = userRepository.existsByEmail("test1@example.com");
            boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

            // Then
            assertTrue(exists, "User should exist by email");
            assertFalse(notExists, "Non-existent user should not exist");
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update existing user")
        void shouldUpdateExistingUser() {
            // Given
            User savedUser = userRepository.save(testUser1);

            // When - update user data
            savedUser.setFirstName("Updated");
            savedUser.setLastName("Name");
            savedUser.setEmail("updated@example.com");
            User updatedUser = userRepository.save(savedUser);

            // Then
            assertEquals("Updated", updatedUser.getFirstName(), "First name should be updated");
            assertEquals("Name", updatedUser.getLastName(), "Last name should be updated");
            assertEquals("updated@example.com", updatedUser.getEmail(), "Email should be updated");

            // Verify the update was persisted
            Optional<User> foundUser = userRepository.findById(testUser1.getId());
            assertTrue(foundUser.isPresent(), "User should still exist");
            assertEquals("Updated", foundUser.get().getFirstName(), "First name should be updated in storage");
            assertEquals("updated@example.com", foundUser.get().getEmail(), "Email should be updated in storage");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty repository")
        void shouldHandleEmptyRepository() {
            // When & Then
            assertTrue(userRepository.findAll().isEmpty(), "Empty repository should return empty list");
            assertEquals(0, userRepository.count(), "Empty repository should have count 0");
            assertFalse(userRepository.findById("any-id").isPresent(), "Empty repository should not find any user");
        }

        @Test
        @DisplayName("Should handle duplicate IDs by overwriting")
        void shouldHandleDuplicateIdsByOverwriting() {
            // Given
            String sameId = UUID.randomUUID().toString();
            testUser1.setId(sameId);
            testUser2.setId(sameId);

            // When
            userRepository.save(testUser1);
            userRepository.save(testUser2);

            // Then
            Optional<User> foundUser = userRepository.findById(sameId);
            assertTrue(foundUser.isPresent(), "User with duplicate ID should be found");
            assertEquals("testuser2", foundUser.get().getUsername(), "Later user should overwrite earlier one");
        }
    }
}