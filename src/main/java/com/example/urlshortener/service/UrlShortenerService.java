package com.example.urlshortener.service;

import com.example.urlshortener.entity.ClickAnalytics;
import com.example.urlshortener.entity.Url;
import com.example.urlshortener.repository.ClickAnalyticsRepository;
import com.example.urlshortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.urlshortener.kafka.ClickEventProducer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UrlShortenerService {

    private final UrlRepository urlRepository;
    private final ClickAnalyticsRepository analyticsRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ClickEventProducer clickEventProducer;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final AtomicLong counter = new AtomicLong(1_000_000_000L);


    public UrlShortenerService(
            UrlRepository urlRepository,
            ClickAnalyticsRepository analyticsRepository,
            RedisTemplate<String, String> redisTemplate,
            ClickEventProducer clickEventProducer) {  // ADD THIS
        this.urlRepository = urlRepository;
        this.analyticsRepository = analyticsRepository;
        this.redisTemplate = redisTemplate;
        this.clickEventProducer = clickEventProducer;  // ADD THIS
    }

    private String encode(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(CHARS.charAt((int)(id % 62)));
            id /= 62;
        }
        return sb.reverse().toString();
    }

    // ── URL SHORTEN ──────────────────────────────────────────────────────────

    @Transactional
    public String shortenUrl(String originalUrl, String customAlias, Integer expiryHours) {

        // Step 1: Code decide karo
        String code;
        if (customAlias != null && !customAlias.isBlank()) {
            // User ne custom alias diya
            if (urlRepository.existsByShortCode(customAlias)) {
                throw new RuntimeException(
                        "Alias '" + customAlias + "' already use ho raha hai!");
            }
            code = customAlias;
        } else {
            // Auto generate karo
            do {
                code = encode(counter.getAndIncrement());
            } while (urlRepository.existsByShortCode(code));
        }

        // Step 2: Expiry time set karo
        LocalDateTime expiresAt = null;
        if (expiryHours != null && expiryHours > 0) {
            expiresAt = LocalDateTime.now().plusHours(expiryHours);
        }

        // Step 3: PostgreSQL mein save karo
        Url url = new Url();
        url.setShortCode(code);
        url.setOriginalUrl(originalUrl);
        url.setExpiresAt(expiresAt);
        urlRepository.save(url);

        // Step 4: Redis mein cache karo
        long cacheTtl = 86400L; // 24 hours default
        if (expiresAt != null) {
            long secondsLeft = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
            cacheTtl = Math.min(cacheTtl, secondsLeft);
        }
        redisTemplate.opsForValue().set(
                "url:" + code,
                originalUrl,
                Duration.ofSeconds(cacheTtl)
        );

        return baseUrl + "/" + code;
    }

    // ── REDIRECT ─────────────────────────────────────────────────────────────

    @Transactional
    public String getOriginalUrl(String code, String ipAddress, String userAgent) {

        // Step 1: Redis cache check karo
        String cachedUrl = redisTemplate.opsForValue().get("url:" + code);
        if (cachedUrl != null) {
            recordClick(code, ipAddress, userAgent);
            return cachedUrl;
        }

        // Step 2: PostgreSQL mein dhundho
        Url url = urlRepository.findByShortCodeAndActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("URL nahi mili: " + code));

        // Step 3: Expiry check karo
        if (url.getExpiresAt() != null &&
                url.getExpiresAt().isBefore(LocalDateTime.now())) {
            url.setActive(false);
            urlRepository.save(url);
            redisTemplate.delete("url:" + code);
            throw new RuntimeException("Ye URL expire ho gayi hai!");
        }

        // Step 4: Redis mein wapas daal do
        redisTemplate.opsForValue().set(
                "url:" + code,
                url.getOriginalUrl(),
                Duration.ofHours(24)
        );

        recordClick(code, ipAddress, userAgent);
        return url.getOriginalUrl();
    }

    // ── ANALYTICS ────────────────────────────────────────────────────────────

    public Map<String, Object> getStats(String code) {
        Url url = urlRepository.findByShortCodeAndActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("URL nahi mili: " + code));

        List<ClickAnalytics> clicks =
                analyticsRepository.findByUrlOrderByClickedAtDesc(url);

        Map<String, Object> stats = new HashMap<>();
        stats.put("shortCode", url.getShortCode());
        stats.put("originalUrl", url.getOriginalUrl());
        stats.put("createdAt", url.getCreatedAt());
        stats.put("expiresAt", url.getExpiresAt());
        stats.put("totalClicks", url.getClickCount());
        stats.put("clickHistory", clicks);
        stats.put("active", url.isActive());

        return stats;
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Transactional
    public void deleteUrl(String code) {
        Url url = urlRepository.findByShortCodeAndActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("URL nahi mili: " + code));
        url.setActive(false);
        urlRepository.save(url);
        redisTemplate.delete("url:" + code);
    }

    // ── SCHEDULED CLEANUP ────────────────────────────────────────────────────

    // Har ek ghante mein automatically expired URLs clean karta hai
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void cleanupExpiredUrls() {
        List<Url> expired = urlRepository.findExpiredUrls(LocalDateTime.now());
        for (Url url : expired) {
            url.setActive(false);
            redisTemplate.delete("url:" + url.getShortCode());
        }
        urlRepository.saveAll(expired);
        System.out.println("Cleanup: " + expired.size() + " expired URLs deactivate kiye");
    }

    // ── PRIVATE HELPER ───────────────────────────────────────────────────────



    private void recordClick(String code, String ipAddress, String userAgent) {
        // Sirf Kafka pe publish karo — instant return
        // Consumer background mein DB save karega
        clickEventProducer.publishClickEvent(code, ipAddress, userAgent);
    }
}