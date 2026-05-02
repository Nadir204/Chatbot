package com.example.Chatbot.controller;

import com.example.Chatbot.model.ChatMessage;
import com.example.Chatbot.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ekane chat page er sob logic control
 * REST controller for chat endpoints.
 * Handles sending messages and retrieving chat history.
 *
 * Base path: /chat
 */

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*") // Allow requests from the frontend HTML pages
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * postman test:
     * POST /chat?userId={id}&message={text}
     *
     * Send a message to the bot and receive a reply.
     * The bot uses the last 5 messages as context (memory window).
     *
     * Query params:
     *   userId  – the authenticated user's ID
     *   message – the message text
     *
     * Response (JSON):
     * { "reply": "Hi! I'm ChatBot..." }
     */


    @PostMapping
    public ResponseEntity<Map<String, String>> sendMessage(

            @RequestParam Long userId,
            @RequestParam String message) {

        // Validate inputs
        //used hashmap way for better mapping
        if (userId == null || message == null || message.isBlank()) {
            Map<String, String> error = new HashMap<>();
            //sob e jodi null hoi
            error.put("error", "userId and message are required.");
            //bad req =400 series
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Delegate to ChatService; it saves both messages and returns the reply
        //processMessage() from service page
        String botReply = chatService.processMessage(userId, message.trim());

        Map<String, String> response = new HashMap<>();
        response.put("reply", botReply);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /chat/history?userId={id}
     *
     * Retrieve the full chat history for a given user,
     * ordered from oldest to newest.
     *
     * Query params:
     *   userId – the authenticated user's ID
     *
     * Response (JSON):
     * [
     *   { "id": 1, "userId": 1, "sender": "USER", "content": "hello", "timestamp": "..." },
     *   { "id": 2, "userId": 1, "sender": "BOT",  "content": "Hi!",   "timestamp": "..." },
     *   ...
     * ]
     */


    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory(
            @RequestParam Long userId) {

        //getHistory from service page
        List<ChatMessage> history = chatService.getHistory(userId);
        return ResponseEntity.ok(history);
    }
}