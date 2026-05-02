package com.example.Chatbot.controller;

import com.example.Chatbot.model.User;
import com.example.Chatbot.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
  REST controller for authentication endpoints.
 aita handel kore
   user registration and login.

  Base path: /auth
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // Allow requests from the frontend HTML pages
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * for testing purpose in post man:
     * POST /auth/register
     *
     * Register a new user account.
     *
     * Request body (JSON):
     * {
     *   "username": "alice",
     *   "password": "secret123"
     * }
     *
     * Response (JSON):
     * { "message": "SUCCESS: ..." }  → 201 Created
     * { "message": "ERROR: ..." }    → 400 Bad Request
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody Map<String, String> body) {

        String username = body.get("username");
        String password = body.get("password");

        //register function er sob logic service page e
        String result = authService.register(username, password);

        Map<String, String> response = new HashMap<>();
        response.put("message", result);

        if (result.startsWith("ERROR")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Postman testing:
     * POST /auth/login
     *
     * Authenticate an existing user.
     *
     * Request body (JSON):
     * {
     *   "username": "alice",
     *   "password": "secret123"
     * }
     *
     * Response on success (JSON):
     * { "message": "Login successful", "userId": 1, "username": "alice" }
     *
     * Response on failure (JSON):
     * { "message": "Invalid username or password" }
     */


    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body) {

        String username = body.get("username");

        String password = body.get("password");
        //login function from service page:
        //login function e paramiter diye value pass korlam
        User user = authService.login(username, password);

        Map<String, Object> response = new HashMap<>();

        //service page er login function return korbe user object,and aita niche check hobe

        if (user == null) {
            response.put("message", "Invalid username or password.");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        //user null(wrong) na hole porer golo hobe
        // Return user info so the frontend can store the userId for chat calls
        response.put("message", "Login successful");
        response.put("userId", user.getId());

        response.put("username", user.getUsername());

        return ResponseEntity.ok(response);
    }
}