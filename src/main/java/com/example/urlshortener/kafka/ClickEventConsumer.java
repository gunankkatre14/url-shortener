package com.example.urlshortener.kafka;

import com.example.urlshortener.entity.ClickAnalytics;
import com.example.urlshortener.repository.ClickAnalyticsRepository;
import com.example.urlshortener.repository.UrlRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClickEventConsumer {

    private final ClickAnalyticsRepository analyticsRepository;
    private final UrlRepository urlRepository;

    public ClickEventConsumer(
            ClickAnalyticsRepository analyticsRepository,
            UrlRepository urlRepository) {
        this.analyticsRepository = analyticsRepository;
        this.urlRepository = urlRepository;
    }

    @KafkaListener(topics = "click-events", groupId = "analytics-group")
    @Transactional
    public void handleClickEvent(ClickEvent event) {
        try {
            urlRepository.findByShortCodeAndActiveTrue(event.getShortCode())
                .ifPresent(url -> {
                    urlRepository.incrementClickCount(event.getShortCode());
                    ClickAnalytics analytics = new ClickAnalytics();
                    analytics.setUrl(url);
                    analytics.setIpAddress(event.getIpAddress());
                    analytics.setUserAgent(event.getUserAgent());
                    analyticsRepository.save(analytics);
                });
        } catch (Exception e) {
            System.out.println("Analytics save failed: " + e.getMessage());
        }
    }
}