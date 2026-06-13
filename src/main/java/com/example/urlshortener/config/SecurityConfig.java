package com.example.urlshortener.config;

import com.example.urlshortener.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        // JWT stateless hai — server pe session nahi rakhte
                )
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC — koi bhi access kar sakta
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/{code}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stats/**").permitAll()

                        // PROTECTED — JWT token required
                        .requestMatchers(HttpMethod.POST, "/api/shorten").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/urls/**").authenticated()
                        .anyRequest().authenticated()
                )
                // JWT filter add karo — har request pe pehle chalega
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}