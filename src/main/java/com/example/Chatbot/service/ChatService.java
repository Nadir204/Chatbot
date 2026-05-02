package com.example.Chatbot.service;

import com.example.Chatbot.model.ChatMessage;
import com.example.Chatbot.repo.ChatRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Core chat service.
 * Sends messages to Groq API (LLaMA 3 model) and saves responses.
 * Falls back to rule-based replies if API is unavailable.
 */
@Service
public class ChatService {

    // Reads GROQ_API_KEY from .env → application.properties
    @Value("${groq.api.key}")
    private String apiKey;

    // Groq API endpoint (same format as OpenAI)
    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    // Groq model to use
    //old model:llama3-8b-8192(not worked)
    private static final String GROQ_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";

    private final ChatRepo chatRepo;
    private final MemoryService memoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ChatRepo chatRepo, MemoryService memoryService) {
        this.chatRepo = chatRepo;
        this.memoryService = memoryService;
    }

    /**
     * the system/logic will be :
     * Process an incoming user message:
     * 1. Save user message to DB
     * 2. Build context from last 5 messages (memory)
     * 3. Call Groq API for AI reply
     * 4. Save bot reply to DB
     * 5. Return bot reply
     */

//save user and ai reply in db
    public String processMessage(Long userId, String message) {
        // Step 1 — Save user message to database
        ChatMessage userMsg = new ChatMessage(userId, "USER", message);
        chatRepo.save(userMsg);

        // Step 2 — Get memory context (last 5 messages)
        //aita from memoryservice page
        String context = memoryService.buildContextSummary(userId);

        // Step 3 — Call Groq API for intelligent reply
        String botReply = callGroqApi(message, context);

        // Step 4 — Save bot reply to database
        ChatMessage botMsg = new ChatMessage(userId, "BOT", botReply);
        chatRepo.save(botMsg);

        // Step 5 — Return reply to controller
        return botReply;
    }

    /**
     * Calls Groq API with user message + memory context.
     * Falls back to rule-based reply if API fails or quota exceeded.
     */
    private String callGroqApi(String message, String context) {
        try {
            // Build prompt combining memory context + new message
            String systemPrompt = "You are a helpful, friendly chatbot assistant. " +
                    "Keep responses concise and conversational.";

            // Build the conversation history for context
            String userContent = context.isBlank()
                    ? message
                    : context + "\nNow respond to: " + message;

            // Build JSON request body (OpenAI-compatible format) aita grok er jonno o use kora jabe
            //max token handel korbe akjon user kotota use korte parbe
            String requestBody = """
                {
                  "model": "%s",
                  "messages": [
                    {
                      "role": "system",
                      "content": "%s"
                    },
                    {
                      "role": "user",
                      "content": "%s"
                    }
                  ],
                  "max_tokens": 500,
                  "temperature": 0.7
                }
                """.formatted(
                    GROQ_MODEL,
                    escapeJson(systemPrompt),
                    escapeJson(userContent)
            );

            // Build HTTP request with Bearer token auth
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send the request
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());


            //server theke reply asar por
            // Handle quota exceeded
            if (response.statusCode() == 429) {
                System.err.println("Groq quota exceeded — using fallback.");
                return fallbackReply(message);
            }

            // Handle other errors
            if (response.statusCode() != 200) {
                System.err.println("Groq API error " + response.statusCode()
                        + ": " + response.body());
                return fallbackReply(message);
            }


            // Parse and return the AI reply
            return parseGroqResponse(response.body());

        } catch (Exception e) {
            System.err.println("Error calling Groq: " + e.getMessage());
            return fallbackReply(message);
        }
    }

    /**
     * Parse Groq API JSON response and extract reply text.
     *
     * Groq response structure (OpenAI-compatible):
     * {
     *   "choices": [
     *     {
     *       "message": {
     *         "content": "Hello! How can I help?"
     *       }
     *     }
     *   ]
     * }
     */
    private String parseGroqResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode contentNode = root
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content");

            if (contentNode.isMissingNode()) {
                System.err.println("Unexpected Groq response: " + responseBody);
                return "I received an unexpected response format.";
            }

            return contentNode.asText().trim();

        } catch (Exception e) {
            System.err.println("Failed to parse Groq response: " + e.getMessage());
            return "Sorry, I couldn't understand the response from Groq.";
        }
    }

    /**
     * offline mode :
     * Rule-based fallback replies used when Groq API is unavailable.
     * Ensures the chatbot always responds meaningfully.
     */
    private String fallbackReply(String message) {
        String lower = message.toLowerCase().trim();

        if (lower.contains("hello") || lower.contains("hi") ||
                lower.contains("hey")) {
            return "Hi there!  I'm ChatBot. (Running in offline mode)";
        }
        if (lower.contains("name") || lower.contains("who are you")) {
            return "I'm ChatBot, your AI assistant! ";
        }
        if (lower.contains("bye") || lower.contains("goodbye")) {
            return "Goodbye!  Have a great day!";
        }
        if (lower.contains("thank")) {
            return "You're welcome! ";
        }
        if (lower.contains("how are you")) {
            return "I'm doing great, thanks for asking! ";
        }
        if (lower.contains("help")) {
            return "I can chat with you and answer questions! Try saying hello.";
        }
        if (lower.contains("joke")) {
            return "Why do Java developers wear glasses? Because they don't C#! ";
        }

        // Default fallback
        return "You said: \"" + message + "\". " +
                "I'm in offline mode right now — please try again shortly! ";
    }

    /**
     * Escape special characters to prevent malformed JSON requests.
     */
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Get full chat history for a user (oldest → newest).
     */
    public List<ChatMessage> getHistory(Long userId) {
        return chatRepo.findByUserIdOrderByTimestampAsc(userId);
    }
}