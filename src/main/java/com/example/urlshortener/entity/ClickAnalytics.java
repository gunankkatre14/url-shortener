package com.example.urlshortener.entity;

import jakarta.persistence.*;
        import java.time.LocalDateTime;

@Entity
@Table(name = "click_analytics")
public class ClickAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kaun si URL click hui — many clicks belong to one URL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    // Kis IP se click aaya
    @Column(length = 45)
    private String ipAddress;

    // Kis browser/device se aaya
    @Column(length = 500)
    private String userAgent;

    // Save hone se pehle time set hoga automatically
    @PrePersist
    public void prePersist() {
        clickedAt = LocalDateTime.now();
    }

    // Getters aur Setters
    public Long getId() { return id; }

    public Url getUrl() { return url; }
    public void setUrl(Url url) { this.url = url; }

    public LocalDateTime getClickedAt() { return clickedAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
