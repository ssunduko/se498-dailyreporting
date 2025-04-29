package com.se498.dailyreporting.service;

import com.se498.dailyreporting.domain.bo.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAllUsers();

    Optional<User> findUserById(String id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User saveUser(User user);

    void deleteUser(String id);

    List<User> findByUsernameContaining(String username);

    List<User> findByEmailContaining(String email);

    long countActiveUsers();

    long countUsersByRole(String role);
}