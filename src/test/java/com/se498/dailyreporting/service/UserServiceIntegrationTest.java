package com.se498.dailyreporting.service;

import com.se498.dailyreporting.TestDailyReportingApplication;
import com.se498.dailyreporting.domain.bo.User;
import com.se498.dailyreporting.domain.vo.ReportStatus;
import com.se498.dailyreporting.repository.DailyReportRepository;
import com.se498.dailyreporting.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestDailyReportingApplication.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("User Service Integration Tests")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User regularUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        userRepository.deleteAll();

        // Create test users
        adminUser = new User();
        adminUser.setId(UUID.randomUUID().toString());
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole("ADMIN");
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setActive(true);

        regularUser = new User();
        regularUser.setId(UUID.randomUUID().toString());
        regularUser.setUsername("regular");
        regularUser.setEmail("regular@example.com");
        regularUser.setPassword("password");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRole("USER");
        regularUser.setCreatedAt(LocalDateTime.now());
        regularUser.setActive(true);

        inactiveUser = new User();
        inactiveUser.setId(UUID.randomUUID().toString());
        inactiveUser.setUsername("inactive");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setPassword("password");
        inactiveUser.setFirstName("Inactive");
        inactiveUser.setLastName("User");
        inactiveUser.setRole("USER");
        inactiveUser.setCreatedAt(LocalDateTime.now());
        inactiveUser.setActive(false);

        // Save users to database
        adminUser = userRepository.save(adminUser);
        regularUser = userRepository.save(regularUser);
        inactiveUser = userRepository.save(inactiveUser);
    }

    @AfterEach
    void tearDown() {
        // Clean up after tests
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("User Retrieval Tests")
    class UserRetrievalTests {

        @Test
        @DisplayName("Should find all users")
        void shouldFindAllUsers() {
            // When
            List<User> allUsers = userService.findAllUsers();

            // Then
            assertEquals(3, allUsers.size(), "Should find all 3 users");
            assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("admin")),
                    "Should include admin user");
            assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("regular")),
                    "Should include regular user");
            assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("inactive")),
                    "Should include inactive user");
        }

        @Test
        @DisplayName("Should find user by ID")
        void shouldFindUserById() {
            // When
            Optional<User> foundUser = userService.findUserById(adminUser.getId());

            // Then
            assertTrue(foundUser.isPresent(), "Should find user by ID");
            assertEquals("admin", foundUser.get().getUsername(), "Username should match");
            assertEquals("ADMIN", foundUser.get().getRole(), "Role should match");
        }

        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
            // When
            Optional<User> foundUser = userService.findByUsername("regular");

            // Then
            assertTrue(foundUser.isPresent(), "Should find user by username");
            assertEquals(regularUser.getId(), foundUser.get().getId(), "ID should match");
            assertEquals("USER", foundUser.get().getRole(), "Role should match");
        }

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // When
            Optional<User> foundUser = userService.findByEmail("inactive@example.com");

            // Then
            assertTrue(foundUser.isPresent(), "Should find user by email");
            assertEquals("inactive", foundUser.get().getUsername(), "Username should match");
        }

        @Test
        @DisplayName("Should return empty when finding by non-existent ID")
        void shouldReturnEmptyWhenFindingByNonExistentId() {
            // When
            Optional<User> foundUser = userService.findUserById("non-existent-id");

            // Then
            assertFalse(foundUser.isPresent(), "Should return empty for non-existent ID");
        }

        @Test
        @DisplayName("Should return empty when finding by non-existent username")
        void shouldReturnEmptyWhenFindingByNonExistentUsername() {
            // When
            Optional<User> foundUser = userService.findByUsername("non-existent-user");

            // Then
            assertFalse(foundUser.isPresent(), "Should return empty for non-existent username");
        }
    }

    @Nested
    @DisplayName("User Existence Check Tests")
    class UserExistenceCheckTests {

        @Test
        @DisplayName("Should check if user exists by username")
        void shouldCheckIfUserExistsByUsername() {
            // When
            boolean exists = userService.existsByUsername("admin");
            boolean notExists = userService.existsByUsername("non-existent-user");

            // Then
            assertTrue(exists, "Should confirm existing username");
            assertFalse(notExists, "Should deny non-existent username");
        }

        @Test
        @DisplayName("Should check if user exists by email")
        void shouldCheckIfUserExistsByEmail() {
            // When
            boolean exists = userService.existsByEmail("regular@example.com");
            boolean notExists = userService.existsByEmail("non-existent@example.com");

            // Then
            assertTrue(exists, "Should confirm existing email");
            assertFalse(notExists, "Should deny non-existent email");
        }
    }

    @Nested
    @DisplayName("User Creation Tests")
    class UserCreationTests {

        @Test
        @DisplayName("Should create new user")
        @Transactional
        void shouldCreateNewUser() {
            // Given
            User newUser = new User();
            newUser.setId(UUID.randomUUID().toString());
            newUser.setUsername("newuser");
            newUser.setEmail("new@example.com");
            newUser.setPassword("password");
            newUser.setFirstName("New");
            newUser.setLastName("User");
            newUser.setRole("USER");

            // When
            User savedUser = userService.saveUser(newUser);

            // Then
            assertNotNull(savedUser, "Saved user should not be null");
            assertNotNull(savedUser.getId(), "Saved user should have an ID");
            assertEquals("newuser", savedUser.getUsername(), "Username should match");
            assertEquals("new@example.com", savedUser.getEmail(), "Email should match");
            assertEquals("USER", savedUser.getRole(), "Role should match");
            assertNotNull(savedUser.getCreatedAt(), "Created timestamp should be set");
            assertTrue(savedUser.isActive(), "New user should be active by default");

            // Verify user was saved to database
            Optional<User> foundUser = userRepository.findById(savedUser.getId());
            assertTrue(foundUser.isPresent(), "User should exist in database");
            assertEquals("newuser", foundUser.get().getUsername(), "Username should match in database");
        }

    }

    @Nested
    @DisplayName("User Update Tests")
    class UserUpdateTests {

        @Test
        @DisplayName("Should update user role")
        @Transactional
        void shouldUpdateUserRole() {
            // Given
            String newRole = "MANAGER";

            // When
            regularUser.setRole(newRole);
            User updatedUser = userService.saveUser(regularUser);

            // Then
            assertEquals(newRole, updatedUser.getRole(), "Role should be updated");

            // Verify changes in database
            Optional<User> foundUser = userRepository.findById(regularUser.getId());
            assertTrue(foundUser.isPresent(), "User should exist in database");
            assertEquals(newRole, foundUser.get().getRole(), "Role should be updated in database");
        }
    }

    @Nested
    @DisplayName("User Deletion Tests")
    class UserDeletionTests {

        @Test
        @DisplayName("Should delete user")
        @Transactional
        void shouldDeleteUser() {
            // Given
            String userId = regularUser.getId();

            // When
            userService.deleteUser(userId);

            // Then
            Optional<User> deletedUser = userRepository.findById(userId);
            assertFalse(deletedUser.isPresent(), "User should be deleted from database");
        }

        @Test
        @DisplayName("Should handle deleting non-existent user")
        @Transactional
        void shouldHandleDeletingNonExistentUser() {
            // Given
            String nonExistentId = "non-existent-id";

            // When & Then
            assertDoesNotThrow(() -> {
                userService.deleteUser(nonExistentId);
            }, "Should not throw exception when deleting non-existent user");
        }
    }

    @Nested
    @DisplayName("User Search Tests")
    class UserSearchTests {

        @Test
        @DisplayName("Should find users by username containing")
        void shouldFindUsersByUsernameContaining() {
            // When
            List<User> usersWithAd = userService.findByUsernameContaining("ad");

            // Then
            assertEquals(1, usersWithAd.size(), "Should find 1 user with 'ad' in username");
            assertEquals("admin", usersWithAd.get(0).getUsername(), "Should find admin user");
        }

        @Test
        @DisplayName("Should find users by email containing")
        void shouldFindUsersByEmailContaining() {
            // When
            List<User> usersWithExample = userService.findByEmailContaining("example");

            // Then
            assertEquals(3, usersWithExample.size(), "Should find all users with 'example' in email");

            List<String> emails = usersWithExample.stream()
                    .map(User::getEmail)
                    .collect(Collectors.toList());

            assertTrue(emails.contains("admin@example.com"), "Should find admin email");
            assertTrue(emails.contains("regular@example.com"), "Should find regular user email");
            assertTrue(emails.contains("inactive@example.com"), "Should find inactive user email");
        }

        @Test
        @DisplayName("Should return empty list when no users match search")
        void shouldReturnEmptyListWhenNoUsersMatchSearch() {
            // When
            List<User> nonExistentUsers = userService.findByUsernameContaining("nonexistent");

            // Then
            assertTrue(nonExistentUsers.isEmpty(), "Should return empty list for non-matching search");
        }
    }

    @Nested
    @DisplayName("User Statistics Tests")
    class UserStatisticsTests {

        @Test
        @DisplayName("Should count users by role")
        void shouldCountUsersByRole() {
            // When
            long adminCount = userService.countUsersByRole("ADMIN");
            long userCount = userService.countUsersByRole("USER");
            long managerCount = userService.countUsersByRole("MANAGER");

            // Then
            assertEquals(1, adminCount, "Should count 1 admin user");
            assertEquals(2, userCount, "Should count 2 regular users");
            assertEquals(0, managerCount, "Should count 0 manager users");
        }
    }

    @Nested
    @DisplayName("User Activation Tests")
    class UserActivationTests {

        @Test
        @DisplayName("Should activate inactive user")
        @Transactional
        void shouldActivateInactiveUser() {

            // When
            inactiveUser.setActive(true);
            User activatedUser = userService.saveUser(inactiveUser);

            // Then
            assertTrue(activatedUser.isActive(), "User should be activated");
            // Verify changes in database
            Optional<User> foundUser = userRepository.findById(inactiveUser.getId());
            assertTrue(foundUser.isPresent(), "User should exist in database");
            assertTrue(foundUser.get().isActive(), "User should be active in database");
        }

        @Test
        @DisplayName("Should deactivate active user")
        @Transactional
        void shouldDeactivateActiveUser() {
            // Given
            assertTrue(regularUser.isActive(), "User should start as active");

            // When
            regularUser.setActive(false);
            User deactivatedUser = userService.saveUser(regularUser);

            // Then
            assertFalse(deactivatedUser.isActive(), "User should be deactivated");

            // Verify changes in database
            Optional<User> foundUser = userRepository.findById(regularUser.getId());
            assertTrue(foundUser.isPresent(), "User should exist in database");
            assertFalse(foundUser.get().isActive(), "User should be inactive in database");
        }
    }

    @Nested
    @DisplayName("User Password Tests")
    class UserPasswordTests {

        @Test
        @DisplayName("Should update user password")
        @Transactional
        void shouldUpdateUserPassword() {
            // Given
            String newPassword = "newpassword123";

            // When
            regularUser.setPassword(newPassword);
            User updatedUser = userService.saveUser(regularUser);

            // Then
            assertEquals(newPassword, updatedUser.getPassword(), "Password should be updated");
            // Note: In a real implementation, we would verify the password is properly hashed

            // Verify changes in database
            Optional<User> foundUser = userRepository.findById(regularUser.getId());
            assertTrue(foundUser.isPresent(), "User should exist in database");
            assertEquals(newPassword, foundUser.get().getPassword(), "Password should be updated in database");
        }
    }
}