package com.example.urlshortener.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;

    // Max 10 requests per minute per IP
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_SECONDS = 60;

    public RateLimiterService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void checkRateLimit(String ipAddress) {

        String key = "rate_limit:" + ipAddress;

        // Redis mein counter badhaao
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            // Pehli request — 60 second ka timer start karo
            // 60 sec baad key automatically delete ho jaayegi
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        if (count > MAX_REQUESTS) {
            throw new RuntimeException(
                    "Rate limit exceed ho gaya! Max " + MAX_REQUESTS +
                            " requests per minute allowed hain. Try again in 60 seconds."
            );
        }
    }
}