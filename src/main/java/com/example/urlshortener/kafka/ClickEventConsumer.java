package com.example.urlshortener.kafka;

import com.example.urlshortener.entity.ClickAnalytics;
import com.example.urlshortener.repository.ClickAnalyticsRepository;
import com.example.urlshortener.repository.UrlRepository;
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

    // @KafkaListener = Kafka topic ko continuously sunte rehna
    // Jab bhi naya message aaye — ye method automatically call hoga
    // Background mein chalti hai — user ko pata bhi nahi chalta
    @KafkaListener(topics = "click-events", groupId = "analytics-group")
    @Transactional
    public void handleClickEvent(ClickEvent event) {
        System.out.println("Kafka event received for: " + event.getShortCode());

        // DB mein analytics save karo — background mein
        urlRepository.findByShortCodeAndActiveTrue(event.getShortCode())
                .ifPresent(url -> {
                    // Click count badhaao
                    urlRepository.incrementClickCount(event.getShortCode());

                    // Click record save karo
                    ClickAnalytics analytics = new ClickAnalytics();
                    analytics.setUrl(url);
                    analytics.setIpAddress(event.getIpAddress());
                    analytics.setUserAgent(event.getUserAgent());
                    analyticsRepository.save(analytics);

                    System.out.println("Analytics saved for: " + event.getShortCode());
                });
    }
}