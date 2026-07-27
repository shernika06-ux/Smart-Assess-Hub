package com.univ.smartassesshub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permit pre-flight OPTIONS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Allow static frontend resources
                        .requestMatchers("/", "/index.html", "/*.html", "/*.css", "/*.js", "/*.ico", "/assets/**").permitAll()
                        // Allow auth endpoints (login, register)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Allow ALL read (GET) operations publicly — students must be able to see assignments
                        // even if their session token is expired or a mock fallback token
                        .requestMatchers(HttpMethod.GET, "/api/assignments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/submissions/**").permitAll()
                        // Allow public file downloads/viewing
                        .requestMatchers("/api/submissions/file/**", "/api/submissions/view/**").permitAll()
                        // Secure write operations (create, upload, grade, delete) — require valid JWT
                        .requestMatchers(HttpMethod.POST, "/api/assignments/**", "/api/submissions/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/submissions/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/assignments/**").authenticated()
                        // Allow remaining fallback requests
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}