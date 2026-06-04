package com.example.urlshortener.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ClickEventProducer {

    // Topic naam — Consumer isi naam se sunega
    private static final String TOPIC = "click-events";

    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;

    public ClickEventProducer(KafkaTemplate<String, ClickEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishClickEvent(String shortCode, String ip, String userAgent) {
        // Event object banao
        ClickEvent event = new ClickEvent(
                shortCode, ip, userAgent, LocalDateTime.now()
        );

        // Kafka pe bhejo — NON-BLOCKING
        // Ye method immediately return karta hai
        // DB write ka wait NAHI karta
        kafkaTemplate.send(TOPIC, shortCode, event);

        System.out.println("Click event published to Kafka: " + shortCode);
    }
}