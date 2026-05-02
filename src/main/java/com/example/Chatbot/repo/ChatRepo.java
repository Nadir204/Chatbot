package com.example.Chatbot.repo;

import com.example.Chatbot.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for ChatMessage entity.
 * Provides CRUD operations plus per-user history queries.
 */
@Repository
public interface ChatRepo extends JpaRepository<ChatMessage, Long> {

    /**
     * Retrieve the full chat history for a user, oldest first.
     *
     * @param userId the ID of the user whose history to fetch
     * @return list of messages ordered by timestamp ascending
     */
    List<ChatMessage> findByUserIdOrderByTimestampAsc(Long userId);

    /**
     * Retrieve the N most recent messages for a user (for the memory window).
     * Spring Data interprets "Top5" automatically.
     *
     * @param userId the user whose recent messages to fetch
     * @return list of up to 5 messages ordered by timestamp descending
     */
    List<ChatMessage> findTop5ByUserIdOrderByTimestampDesc(Long userId);
}