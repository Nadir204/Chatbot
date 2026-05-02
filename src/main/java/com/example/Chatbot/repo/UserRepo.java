package com.example.Chatbot.repo;

import com.example.Chatbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for User entity.
 * Provides CRUD operations plus custom finder methods.
 */
@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    /**
     * Find a user by their username (used during login).
     *
     * @param username the username to look up
     * @return an Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check whether a username already exists (used during registration).
     *
     * @param username the username to check
     * @return true if the username is taken
     */
    boolean existsByUsername(String username);
}