package com.example.urlshortener.controller;

import com.example.urlshortener.service.RateLimiterService;
import com.example.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "URL Shortener API", description = "URLs shorten karo, redirect karo, analytics dekho")
public class UrlShortenerController {

    private final UrlShortenerService urlService;
    private final RateLimiterService rateLimiter;

    public UrlShortenerController(
            UrlShortenerService urlService,
            RateLimiterService rateLimiter) {
        this.urlService = urlService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/api/shorten")
    @Operation(
            summary = "URL Shorten Karo",
            description = "Long URL do — short URL wapas milega. Optional: customAlias aur expiryHours bhi de sakte ho."
    )
    public ResponseEntity<Map<String, String>> shorten(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        String ip = getClientIp(request);
        rateLimiter.checkRateLimit(ip);

        String originalUrl = (String) body.get("originalUrl");
        String customAlias = (String) body.get("customAlias");
        Integer expiryHours = body.get("expiryHours") != null
                ? ((Number) body.get("expiryHours")).intValue()
                : null;

        String shortUrl = urlService.shortenUrl(originalUrl, customAlias, expiryHours);
        return ResponseEntity.ok(Map.of("shortUrl", shortUrl));
    }

    @GetMapping("/{code}")
    @Operation(
            summary = "Redirect Karo",
            description = "Short code do — original URL pe redirect ho jaao (HTTP 302)"
    )
    public ResponseEntity<Void> redirect(
            @Parameter(description = "Short code — jaise bfP3Qq ya mygit")
            @PathVariable String code,
            HttpServletRequest request) {

        String originalUrl = urlService.getOriginalUrl(
                code,
                getClientIp(request),
                request.getHeader("User-Agent")
        );
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", originalUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/api/stats/{code}")
    @Operation(
            summary = "Analytics Dekho",
            description = "Short code ka click count, click history, expiry info sab dekho"
    )
    public ResponseEntity<Map<String, Object>> getStats(
            @Parameter(description = "Short code jiska stats chahiye")
            @PathVariable String code) {
        return ResponseEntity.ok(urlService.getStats(code));
    }

    @DeleteMapping("/api/urls/{code}")
    @Operation(
            summary = "URL Delete Karo",
            description = "Short URL ko deactivate karo — soft delete, data preserved"
    )
    public ResponseEntity<Map<String, String>> delete(
            @Parameter(description = "Delete karne wala short code")
            @PathVariable String code) {
        urlService.deleteUrl(code);
        return ResponseEntity.ok(Map.of("message", "URL successfully delete ho gayi!"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}