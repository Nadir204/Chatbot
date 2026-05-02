package com.example.Chatbot.service;

import com.example.Chatbot.model.ChatMessage;
import com.example.Chatbot.repo.ChatRepo;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * system hobe:
 * Manages the short-term memory window for each user.
 * Fetches the last 5 messages to give the bot contextual awareness.
 */
@Service
public class MemoryService {

    private final ChatRepo chatRepo;

    public MemoryService(ChatRepo chatRepo) {
        this.chatRepo = chatRepo;
    }

    /**
     * logic:
     * Retrieve the last 5 messages for a user, returned in
     * chronological order (oldest first) so the bot can read
     * the conversation naturally.
     *
     * @param userId the user whose recent context to fetch
     * @return list of up to 5 messages, oldest → newest
     */
    public List<ChatMessage> getRecentMessages(Long userId) {
        // findTop5... returns newest first; we reverse for natural reading order
        List<ChatMessage> recentMessages =
                chatRepo.findTop5ByUserIdOrderByTimestampDesc(userId);

        Collections.reverse(recentMessages); // now oldest → newest
        return recentMessages;
    }

    /**
     * Build a plain-text summary of the recent conversation window.
     * This summary is passed to the AI logic as context.
     *
     * @param userId the user whose context to summarize
     * @return formatted conversation history string
     */
    public String buildContextSummary(Long userId) {
        List<ChatMessage> messages = getRecentMessages(userId);

        if (messages.isEmpty()) {
            return ""; // No prior context
        }

        StringBuilder sb = new StringBuilder("Recent conversation:\n");
        for (ChatMessage msg : messages) {
            sb.append("[")
                    .append(msg.getSender())
                    .append("]: ")
                    .append(msg.getContent())
                    .append("\n");
        }
        return sb.toString();
    }
}