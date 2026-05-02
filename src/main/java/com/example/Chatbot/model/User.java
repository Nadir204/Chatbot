package com.example.Chatbot.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a registered user.
 * Stored in the H2 "users" table.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Username must be unique across all users
    @Column(nullable = false, unique = true)
    private String username;

    // Plain-text password (for simplicity; use BCrypt in production)
    @Column(nullable = false)
    private String password;

    // ── Constructors ────────────────────────────────────

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ── Getters & Setters ───────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}