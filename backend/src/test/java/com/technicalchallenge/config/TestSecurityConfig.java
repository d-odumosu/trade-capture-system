package com.technicalchallenge.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TestSecurityConfig

 * This configuration disables all Spring Security mechanisms
 * (authentication, authorization, CSRF protection, form login, and HTTP Basic)
 * for test environments.

 * It allows controller tests (annotated with @WebMvcTest)
 * to access endpoints freely without being blocked by security filters.
 */
@TestConfiguration
public class TestSecurityConfig {
    /**
     * Creates a simplified SecurityFilterChain for testing purposes.
     *
     * @param http the HttpSecurity object used to configure web-based security.
     * @return a SecurityFilterChain instance with security disabled.
     * @throws Exception if any configuration error occurs.
     */
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable Cross-Site Request Forgery protection (not needed in tests)

                .csrf(AbstractHttpConfigurer::disable)
                // Allow all requests to all endpoints (no authentication or roles required)

                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // Disable both HTTP Basic and Form-based login mechanisms

                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        // Return the finalised security configuration

        return http.build();
    }
}
