package com.example.Chatbot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a single chat message.
 * Stores both user messages ("USER") and bot replies ("BOT").
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who sent or received this message
    //aita diye uniqely identify kora hobe
    @Column(nullable = false)
    private Long userId;

    // "USER" = sent by the user, "BOT" = sent by the bot
    @Column(nullable = false)
    private String sender;

    // The actual text content of the message
    @Column(nullable = false, length = 2000)
    private String content;

    // Timestamp recorded when the message is saved
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // ── Constructors ────────────────────────────────────

    public ChatMessage() {}

    public ChatMessage(Long userId, String sender, String content) {
        this.userId = userId;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    // ── Getters & Setters ───────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}