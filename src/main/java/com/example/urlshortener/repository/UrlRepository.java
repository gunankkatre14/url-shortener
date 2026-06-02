package com.example.urlshortener.repository;

import com.example.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    // shortCode se URL dhundho — sirf active URLs
    Optional<Url> findByShortCodeAndActiveTrue(String shortCode);

    // Kya ye shortCode already exist karta hai?
    boolean existsByShortCode(String shortCode);

    // Click count ek se badhaao directly DB mein
    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    void incrementClickCount(String shortCode);

    @Query("SELECT u FROM Url u WHERE u.expiresAt < :now AND u.active = true")
    List<Url> findExpiredUrls(LocalDateTime now);
}