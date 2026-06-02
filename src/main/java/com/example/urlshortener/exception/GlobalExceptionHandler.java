package com.example.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Ye class saari exceptions ek jagah handle karti hai
// Har controller mein try-catch nahi likhna padta
@RestControllerAdvice
public class GlobalExceptionHandler {

    // RuntimeException — URL not found, alias taken, etc.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {

        // Check karo — rate limit wali exception hai?
        if (ex.getMessage() != null && ex.getMessage().contains("Rate limit")) {
            return buildError(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        }

        // URL not found?
        if (ex.getMessage() != null && ex.getMessage().contains("nahi mili")) {
            return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        // URL expired?
        if (ex.getMessage() != null && ex.getMessage().contains("expire")) {
            return buildError(HttpStatus.GONE, ex.getMessage());
        }

        // Baaki sab — 400 Bad Request
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Koi bhi unexpected error — 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Kuch galat hua — please try again");
    }

    // Clean JSON error response banata hai
    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}