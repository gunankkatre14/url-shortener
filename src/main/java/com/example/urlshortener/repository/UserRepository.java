package com.example.urlshortener.repository;

import com.example.urlshortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Login ke liye — username se user dhundho
    Optional<User> findByUsername(String username);

    // Registration ke liye — already exist karta hai?
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}