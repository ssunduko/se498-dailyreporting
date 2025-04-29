package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    private User user;
    private final String testId = "test-id";
    private final String testUsername = "testuser";
    private final String testEmail = "test@example.com";
    private final String testPassword = "password123";
    private final String testFirstName = "Test";
    private final String testLastName = "User";
    private final String testRole = "USER";
    private final LocalDateTime testTime = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(testId);
        user.setUsername(testUsername);
        user.setEmail(testEmail);
        user.setPassword(testPassword);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setRole(testRole);
        user.setCreatedAt(testTime);
        user.setActive(true);
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersAndSettersTests {

        @Test
        @DisplayName("Test getter and setter for id")
        void testIdGetterAndSetter() {
            assertEquals(testId, user.getId());
            String newId = "new-id";
            user.setId(newId);
            assertEquals(newId, user.getId());
        }

        @Test
        @DisplayName("Test getter and setter for username")
        void testUsernameGetterAndSetter() {
            assertEquals(testUsername, user.getUsername());
            String newUsername = "newuser";
            user.setUsername(newUsername);
            assertEquals(newUsername, user.getUsername());
        }

        @Test
        @DisplayName("Test getter and setter for email")
        void testEmailGetterAndSetter() {
            assertEquals(testEmail, user.getEmail());
            String newEmail = "new@example.com";
            user.setEmail(newEmail);
            assertEquals(newEmail, user.getEmail());
        }

        @Test
        @DisplayName("Test getter and setter for password")
        void testPasswordGetterAndSetter() {
            assertEquals(testPassword, user.getPassword());
            String newPassword = "newpassword123";
            user.setPassword(newPassword);
            assertEquals(newPassword, user.getPassword());
        }

        @Test
        @DisplayName("Test getter and setter for first name")
        void testFirstNameGetterAndSetter() {
            assertEquals(testFirstName, user.getFirstName());
            String newFirstName = "NewFirst";
            user.setFirstName(newFirstName);
            assertEquals(newFirstName, user.getFirstName());
        }

        @Test
        @DisplayName("Test getter and setter for last name")
        void testLastNameGetterAndSetter() {
            assertEquals(testLastName, user.getLastName());
            String newLastName = "NewLast";
            user.setLastName(newLastName);
            assertEquals(newLastName, user.getLastName());
        }

        @Test
        @DisplayName("Test getter and setter for role")
        void testRoleGetterAndSetter() {
            assertEquals(testRole, user.getRole());
            String newRole = "ADMIN";
            user.setRole(newRole);
            assertEquals(newRole, user.getRole());
        }

        @Test
        @DisplayName("Test getter and setter for created at")
        void testCreatedAtGetterAndSetter() {
            assertEquals(testTime, user.getCreatedAt());
            LocalDateTime newTime = LocalDateTime.now().plusDays(1);
            user.setCreatedAt(newTime);
            assertEquals(newTime, user.getCreatedAt());
        }

        @Test
        @DisplayName("Test getter and setter for updated at")
        void testUpdatedAtGetterAndSetter() {
            assertNull(user.getUpdatedAt());
            LocalDateTime updateTime = LocalDateTime.now().plusHours(1);
            user.setUpdatedAt(updateTime);
            assertEquals(updateTime, user.getUpdatedAt());
        }

        @Test
        @DisplayName("Test getter and setter for active status")
        void testActiveGetterAndSetter() {
            assertTrue(user.isActive());
            user.setActive(false);
            assertFalse(user.isActive());
        }
    }

    @Nested
    @DisplayName("JPA Lifecycle Method Tests")
    class JpaLifecycleMethodTests {

        @Test
        @DisplayName("Test onCreate method sets default values")
        void testOnCreate() {
            // Create a new user without setting default values
            User newUser = new User();
            newUser.setUsername("newuser");
            newUser.setEmail("new@example.com");
            newUser.setPassword("password");

            // Call onCreate to simulate JPA lifecycle
            newUser.onCreate();

            // Verify defaults are set
            assertNotNull(newUser.getCreatedAt());
            assertEquals("USER", newUser.getRole());
            assertTrue(newUser.isActive());
        }

        @Test
        @DisplayName("Test onUpdate method sets updated timestamp")
        void testOnUpdate() {
            // Initially updated at should be null
            assertNull(user.getUpdatedAt());

            // Call onUpdate to simulate JPA lifecycle
            user.onUpdate();

            // Verify updated at is set
            assertNotNull(user.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Test no-args constructor")
        void testNoArgsConstructor() {
            User newUser = new User();
            assertNull(newUser.getId());
            assertNull(newUser.getUsername());
            assertNull(newUser.getEmail());
            assertNull(newUser.getPassword());
            assertNull(newUser.getFirstName());
            assertNull(newUser.getLastName());
            assertNull(newUser.getRole());
            assertNull(newUser.getCreatedAt());
            assertNull(newUser.getUpdatedAt());
            assertFalse(newUser.isActive());
        }

        @Test
        @DisplayName("Test all-args constructor")
        void testAllArgsConstructor() {
            User newUser = new User(
                    testId,
                    testUsername,
                    testEmail,
                    testPassword,
                    testFirstName,
                    testLastName,
                    testRole,
                    testTime,
                    null,
                    true
            );

            assertEquals(testId, newUser.getId());
            assertEquals(testUsername, newUser.getUsername());
            assertEquals(testEmail, newUser.getEmail());
            assertEquals(testPassword, newUser.getPassword());
            assertEquals(testFirstName, newUser.getFirstName());
            assertEquals(testLastName, newUser.getLastName());
            assertEquals(testRole, newUser.getRole());
            assertEquals(testTime, newUser.getCreatedAt());
            assertNull(newUser.getUpdatedAt());
            assertTrue(newUser.isActive());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Test equals and hashCode contracts")
        void testEqualsAndHashCode() {
            // Create an identical user
            User sameUser = new User();
            sameUser.setId(testId);
            sameUser.setUsername(testUsername);
            sameUser.setEmail(testEmail);
            sameUser.setPassword(testPassword);
            sameUser.setFirstName(testFirstName);
            sameUser.setLastName(testLastName);
            sameUser.setRole(testRole);
            sameUser.setCreatedAt(testTime);
            sameUser.setActive(true);

            // Create a different user
            User differentUser = new User();
            differentUser.setId("different-id");
            differentUser.setUsername("differentuser");
            differentUser.setEmail("different@example.com");
            differentUser.setPassword("differentpassword");
            differentUser.setFirstName("Different");
            differentUser.setLastName("User");
            differentUser.setRole("ADMIN");
            differentUser.setCreatedAt(LocalDateTime.now().plusDays(1));
            differentUser.setActive(false);

            // Test equality
            assertEquals(user, user); // same object
            assertEquals(user, sameUser); // "equal" object
            assertNotEquals(user, differentUser); // different object
            assertNotEquals(user, null); // null
            assertNotEquals(user, new Object()); // different class

            // Test hashCode
            assertEquals(user.hashCode(), sameUser.hashCode());
            assertNotEquals(user.hashCode(), differentUser.hashCode());
        }
    }
}