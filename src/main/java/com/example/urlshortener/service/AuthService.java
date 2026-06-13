package com.example.urlshortener.service;

import com.example.urlshortener.entity.User;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // Register
    public Map<String, String> register(String username, String email, String password) {

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already taken!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered!");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // BCrypt hash
        userRepository.save(user);

        String token = jwtUtil.generateToken(username);
        return Map.of(
                "message", "Registration successful!",
                "token", token,
                "username", username
        );
    }

    // Login
    public Map<String, String> login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        // BCrypt se compare karo
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        String token = jwtUtil.generateToken(username);
        return Map.of(
                "message", "Login successful!",
                "token", token,
                "username", username
        );
    }
}