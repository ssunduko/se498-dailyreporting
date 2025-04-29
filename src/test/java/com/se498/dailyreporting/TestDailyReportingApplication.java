
package com.se498.dailyreporting;

import com.se498.dailyreporting.domain.bo.User;
import com.se498.dailyreporting.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootApplication
public class TestDailyReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestDailyReportingApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(UserService userService) {
        return args -> {
            System.out.println("Test application initialized. Beans provided by Spring Boot ready for testing.");

            // Initialize test users if the repository is empty
            if (userService.findAllUsers().isEmpty()) {
                initializeTestUsers(userService);
            }
        };
    }

    /**
     * Create standard test users for integration tests
     */
    private void initializeTestUsers(UserService userService) {
        System.out.println("Initializing test users...");

        // Admin user
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID().toString());
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole("ADMIN");
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setActive(true);
        userService.saveUser(adminUser);

        // Regular user
        User regularUser = new User();
        regularUser.setId(UUID.randomUUID().toString());
        regularUser.setUsername("user");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword("password");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRole("USER");
        regularUser.setCreatedAt(LocalDateTime.now());
        regularUser.setActive(true);
        userService.saveUser(regularUser);

        // Manager user
        User managerUser = new User();
        managerUser.setId(UUID.randomUUID().toString());
        managerUser.setUsername("manager");
        managerUser.setEmail("manager@example.com");
        managerUser.setPassword("password");
        managerUser.setFirstName("Project");
        managerUser.setLastName("Manager");
        managerUser.setRole("MANAGER");
        managerUser.setCreatedAt(LocalDateTime.now());
        managerUser.setActive(true);
        userService.saveUser(managerUser);

        // Inactive user
        User inactiveUser = new User();
        inactiveUser.setId(UUID.randomUUID().toString());
        inactiveUser.setUsername("inactive");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setPassword("password");
        inactiveUser.setFirstName("Inactive");
        inactiveUser.setLastName("User");
        inactiveUser.setRole("USER");
        inactiveUser.setCreatedAt(LocalDateTime.now());
        inactiveUser.setActive(false);
        userService.saveUser(inactiveUser);

        // Test users for specific test cases
        User testUser1 = new User();
        testUser1.setId(UUID.randomUUID().toString());
        testUser1.setUsername("testuser1");
        testUser1.setEmail("test1@example.com");
        testUser1.setPassword("password123");
        testUser1.setFirstName("Test");
        testUser1.setLastName("User1");
        testUser1.setRole("USER");
        testUser1.setCreatedAt(LocalDateTime.now());
        testUser1.setActive(true);
        userService.saveUser(testUser1);

        User testUser2 = new User();
        testUser2.setId(UUID.randomUUID().toString());
        testUser2.setUsername("testuser2");
        testUser2.setEmail("test2@example.com");
        testUser2.setPassword("password123");
        testUser2.setFirstName("Test");
        testUser2.setLastName("User2");
        testUser2.setRole("USER");
        testUser2.setCreatedAt(LocalDateTime.now());
        testUser2.setActive(true);
        userService.saveUser(testUser2);

        System.out.println("Test users created successfully");
    }
}