package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.User;
import com.se498.dailyreporting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceMockTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private final String testId = UUID.randomUUID().toString();
    private final String testUsername = "testuser";
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
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

    @Nested
    @DisplayName("findAllUsers tests")
    class FindAllUsersTests {

        @Test
        @DisplayName("Should return all users")
        void shouldReturnAllUsers() {
            // Given
            User secondUser = new User();
            secondUser.setId(UUID.randomUUID().toString());
            secondUser.setUsername("seconduser");
            List<User> expectedUsers = Arrays.asList(testUser, secondUser);

            when(userRepository.findAll()).thenReturn(expectedUsers);

            // When
            List<User> actualUsers = userService.findAllUsers();

            // Then
            assertEquals(expectedUsers.size(), actualUsers.size());
            assertEquals(expectedUsers, actualUsers);
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("findUserById tests")
    class FindUserByIdTests {

        @Test
        @DisplayName("Should return user when ID exists")
        void shouldReturnUserWhenIdExists() {
            // Given
            when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));

            // When
            Optional<User> foundUser = userService.findUserById(testId);

            // Then
            assertTrue(foundUser.isPresent());
            assertEquals(testUser, foundUser.get());
            verify(userRepository).findById(testId);
        }

        @Test
        @DisplayName("Should return empty when ID doesn't exist")
        void shouldReturnEmptyWhenIdDoesntExist() {
            // Given
            String nonExistentId = "non-existent-id";
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When
            Optional<User> foundUser = userService.findUserById(nonExistentId);

            // Then
            assertFalse(foundUser.isPresent());
            verify(userRepository).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("findByUsername tests")
    class FindByUsernameTests {

        @Test
        @DisplayName("Should return user when username exists")
        void shouldReturnUserWhenUsernameExists() {
            // Given
            when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

            // When
            Optional<User> foundUser = userService.findByUsername(testUsername);

            // Then
            assertTrue(foundUser.isPresent());
            assertEquals(testUser, foundUser.get());
            verify(userRepository).findByUsername(testUsername);
        }

        @Test
        @DisplayName("Should return empty when username doesn't exist")
        void shouldReturnEmptyWhenUsernameDoesntExist() {
            // Given
            String nonExistentUsername = "nonexistentuser";
            when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

            // When
            Optional<User> foundUser = userService.findByUsername(nonExistentUsername);

            // Then
            assertFalse(foundUser.isPresent());
            verify(userRepository).findByUsername(nonExistentUsername);
        }
    }

    @Nested
    @DisplayName("findByEmail tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should return user when email exists")
        void shouldReturnUserWhenEmailExists() {
            // Given
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

            // When
            Optional<User> foundUser = userService.findByEmail(testEmail);

            // Then
            assertTrue(foundUser.isPresent());
            assertEquals(testUser, foundUser.get());
            verify(userRepository).findByEmail(testEmail);
        }
    }

    @Nested
    @DisplayName("existsByUsername tests")
    class ExistsByUsernameTests {

        @Test
        @DisplayName("Should return true when username exists")
        void shouldReturnTrueWhenUsernameExists() {
            // Given
            when(userRepository.existsByUsername(testUsername)).thenReturn(true);

            // When
            boolean exists = userService.existsByUsername(testUsername);

            // Then
            assertTrue(exists);
            verify(userRepository).existsByUsername(testUsername);
        }

        @Test
        @DisplayName("Should return false when username doesn't exist")
        void shouldReturnFalseWhenUsernameDoesntExist() {
            // Given
            String nonExistentUsername = "nonexistentuser";
            when(userRepository.existsByUsername(nonExistentUsername)).thenReturn(false);

            // When
            boolean exists = userService.existsByUsername(nonExistentUsername);

            // Then
            assertFalse(exists);
            verify(userRepository).existsByUsername(nonExistentUsername);
        }
    }

    @Nested
    @DisplayName("existsByEmail tests")
    class ExistsByEmailTests {

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            // Given
            when(userRepository.existsByEmail(testEmail)).thenReturn(true);

            // When
            boolean exists = userService.existsByEmail(testEmail);

            // Then
            assertTrue(exists);
            verify(userRepository).existsByEmail(testEmail);
        }
    }

    @Nested
    @DisplayName("saveUser tests")
    class SaveUserTests {

        @Test
        @DisplayName("Should save new user")
        void shouldSaveNewUser() {
            // Given
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            User savedUser = userService.saveUser(testUser);

            // Then
            assertEquals(testUser, savedUser);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should update existing user")
        void shouldUpdateExistingUser() {
            // Given
            testUser.setFirstName("Updated");
            testUser.setLastName("Name");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            User updatedUser = userService.saveUser(testUser);

            // Then
            assertEquals("Updated", updatedUser.getFirstName());
            assertEquals("Name", updatedUser.getLastName());
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("deleteUser tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user")
        void shouldDeleteUser() {
            // Given
            doNothing().when(userRepository).deleteById(anyString());

            // When
            userService.deleteUser(testId);

            // Then
            verify(userRepository).deleteById(testId);
        }
    }

    @Nested
    @DisplayName("findByUsernameContaining tests")
    class FindByUsernameContainingTests {

        @Test
        @DisplayName("Should return users with username containing text")
        void shouldReturnUsersWithUsernameContainingText() {
            // Given
            User user1 = new User();
            user1.setUsername("testuser1");
            User user2 = new User();
            user2.setUsername("testuser2");
            User user3 = new User();
            user3.setUsername("otheruser");

            List<User> allUsers = Arrays.asList(user1, user2, user3);
            when(userRepository.findAll()).thenReturn(allUsers);

            // When
            List<User> result = userService.findByUsernameContaining("test");

            // Then
            assertEquals(2, result.size());
            assertTrue(result.contains(user1));
            assertTrue(result.contains(user2));
            assertFalse(result.contains(user3));
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("findByEmailContaining tests")
    class FindByEmailContainingTests {

        @Test
        @DisplayName("Should return users with email containing text")
        void shouldReturnUsersWithEmailContainingText() {
            // Given
            User user1 = new User();
            user1.setEmail("test1@example.com");
            User user2 = new User();
            user2.setEmail("test2@example.com");
            User user3 = new User();
            user3.setEmail("other@domain.com");

            List<User> allUsers = Arrays.asList(user1, user2, user3);
            when(userRepository.findAll()).thenReturn(allUsers);

            // When
            List<User> result = userService.findByEmailContaining("example");

            // Then
            assertEquals(2, result.size());
            assertTrue(result.contains(user1));
            assertTrue(result.contains(user2));
            assertFalse(result.contains(user3));
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("countActiveUsers tests")
    class CountActiveUsersTests {

        @Test
        @DisplayName("Should count active users")
        void shouldCountActiveUsers() {
            // Given
            User activeUser1 = new User();
            activeUser1.setActive(true);
            User activeUser2 = new User();
            activeUser2.setActive(true);
            User inactiveUser = new User();
            inactiveUser.setActive(false);

            List<User> allUsers = Arrays.asList(activeUser1, activeUser2, inactiveUser);
            when(userRepository.findAll()).thenReturn(allUsers);

            // When
            long count = userService.countActiveUsers();

            // Then
            assertEquals(2, count);
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("countUsersByRole tests")
    class CountUsersByRoleTests {

        @Test
        @DisplayName("Should count users by role")
        void shouldCountUsersByRole() {
            // Given
            User userRole1 = new User();
            userRole1.setRole("USER");
            User userRole2 = new User();
            userRole2.setRole("USER");
            User adminRole = new User();
            adminRole.setRole("ADMIN");

            List<User> allUsers = Arrays.asList(userRole1, userRole2, adminRole);
            when(userRepository.findAll()).thenReturn(allUsers);

            // When
            long userCount = userService.countUsersByRole("USER");
            long adminCount = userService.countUsersByRole("ADMIN");

            // Then
            assertEquals(2, userCount);
            assertEquals(1, adminCount);
            verify(userRepository, times(2)).findAll();
        }
    }
}