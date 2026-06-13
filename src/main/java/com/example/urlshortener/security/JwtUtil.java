package com.example.urlshortener.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    // Secret key banao
    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Token generate karo — login ke baad
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)           // username store karo
                .setIssuedAt(new Date())        // kab banaya
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // expiry
                .signWith(getKey())             // sign karo secret se
                .compact();
    }

    // Token se username nikalo
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Token valid hai?
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // expired ya invalid
        }
    }
}