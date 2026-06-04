package com.example.urlshortener.kafka;

import java.time.LocalDateTime;

// Ye wo object hai jo Kafka pe bheja jaayega
// Producer ye banata hai → Kafka store karta hai → Consumer receive karta hai
public class ClickEvent {

    private String shortCode;    // Kaun sa URL click hua
    private String ipAddress;    // Kis IP se
    private String userAgent;    // Kis browser se
    private LocalDateTime clickedAt; // Kab

    // Default constructor — Kafka deserialize karne ke liye zaroori
    public ClickEvent() {}

    public ClickEvent(String shortCode, String ipAddress,
                      String userAgent, LocalDateTime clickedAt) {
        this.shortCode = shortCode;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.clickedAt = clickedAt;
    }

    // Getters aur Setters
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getClickedAt() { return clickedAt; }
    public void setClickedAt(LocalDateTime clickedAt) { this.clickedAt = clickedAt; }
}