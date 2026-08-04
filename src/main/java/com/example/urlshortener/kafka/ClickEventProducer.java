package com.example.urlshortener.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ClickEventProducer {

    private static final String TOPIC = "click-events";
    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;

    public ClickEventProducer(KafkaTemplate<String, ClickEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishClickEvent(String shortCode, String ip, String userAgent) {
        try {
            ClickEvent event = new ClickEvent(shortCode, ip, userAgent, LocalDateTime.now());
            kafkaTemplate.send(TOPIC, shortCode, event);
        } catch (Exception e) {
            // Kafka nahi hai deployment pe — silently ignore
            System.out.println("Kafka unavailable — skipping click event: " + shortCode);
        }
    }
}