package com.example.Chatbot.service;

import com.example.Chatbot.model.User;
import com.example.Chatbot.repo.UserRepo;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Handles user registration and login with full validation.
 */
@Service
public class AuthService {

    private final UserRepo userRepo;

    // Minimum allowed lengths
    //aita diye username and password er minimum length fix korlam,8 dile 8 length er pass dite hobe
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

    public AuthService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }



    /**
     * Register er jonno system/logic hobe:
     * Register a new user with full backend validation.
     * Even if frontend validation is bypassed, backend catches it.
     *
     * @param username desired username
     * @param password desired password
     * @return result message starting with "SUCCESS" or "ERROR"
     */
    //used in @PostMapping("/register")
    public String register(String username, String password) {

        // ── Check for blank fields ────────────────────────
        if (username == null || username.isBlank()) {
            return "ERROR: Username is required.";
        }

        if (password == null || password.isBlank()) {
            return "ERROR: Password is required.";
        }

        // Clean the username
        username = username.trim();

        // ── Username validations ──────────────────────────
        if (username.length() < MIN_USERNAME_LENGTH) {
            return "ERROR: Username must be at least "
                    + MIN_USERNAME_LENGTH + " characters.";
        }

        if (username.contains(" ")) {
            return "ERROR: Username cannot contain spaces.";
        }

        // ── Password validations ──────────────────────────
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "ERROR: Password must be at least "
                    + MIN_PASSWORD_LENGTH + " characters.";
        }

        // ── Check username availability ───────────────────
        //aita akta feature cz ami jodi same same username allow kori tahole password same username er jonno vinno vinno hobe ,major problem hobe :)
        if (userRepo.existsByUsername(username)) {
            return "ERROR: Username '" + username + "' is already taken.";
        }

        // ── All validations passed — save the user ────────
        //user details go to db by repo
        User newUser = new User(username, password);

        //.save by default function from repo
        userRepo.save(newUser);

        return "SUCCESS: Account created for '" + username + "'.";
    }

    /**
     *
     * Login er jonno system and logic :
     * Authenticate a user by verifying username + password.
     *
     * @param username the username to log in with
     * @param password the password to verify
     * @return the matching User, or null if credentials are wrong
     */
    //used in     @PostMapping("/login")
    public User login(String username, String password) {

        //ekanae jekono akta condition true hoye gelei login() return korbe null jeita controller page er "user" object e jeye save hobe,and then user check hoye wrong pass,id show

        // Basic null/blank check
        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            return null;
        }
        //username field jodi null na hoi tahole oi username userOpt te save
        Optional<User> userOpt = userRepo.findByUsername(username.trim());


        //userOpt theke usename check hobe db te ase kina
        // User not found
        if (userOpt.isEmpty()) {
            return null;
        }

        //jodi take username db te tahole user object e value golo save(all field save hobe cz opere findbyusername use hoise)
        User user = userOpt.get();


        //jodi user paoya jay then pass check
        // Wrong password
        if (!user.getPassword().equals(password)) {
            return null;


        }


        //ei user controller page e jabe
        //jodi sob kiso thik tak take tahole jei value golo findbyusername use kore save kora hoise "userOpt te" oita return

        return user;
    }
}