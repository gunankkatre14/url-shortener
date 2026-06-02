
package com.example.urlshortener.repository;

import com.example.urlshortener.entity.ClickAnalytics;
import com.example.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickAnalyticsRepository extends JpaRepository<ClickAnalytics, Long> {

    // Ek URL ke saare clicks — latest pehle
    List<ClickAnalytics> findByUrlOrderByClickedAtDesc(Url url);

    // Ek URL pe total kitne clicks hue
    long countByUrl(Url url);
}