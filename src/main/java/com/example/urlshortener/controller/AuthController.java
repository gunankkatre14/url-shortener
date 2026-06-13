package com.example.urlshortener.controller;

import com.example.urlshortener.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Register aur login karo")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /auth/register
    @PostMapping("/register")
    @Operation(summary = "New user register karo")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.register(
                body.get("username"),
                body.get("email"),
                body.get("password")
        ));
    }

    // POST /auth/login
    @PostMapping("/login")
    @Operation(summary = "Login karo — JWT token milega")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.login(
                body.get("username"),
                body.get("password")
        ));
    }
}