package com.univ.smartassesshub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow static frontend files (index.html, CSS, JS, images)
                        .requestMatchers("/", "/index.html", "/*.html", "/*.css", "/*.js", "/*.ico", "/assets/**").permitAll()
                        // Allow all auth endpoints (login, register)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Allow all API endpoints (open access for training project)
                        .requestMatchers("/api/**").permitAll()
                        // Allow everything else too (static resources, uploads)
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}