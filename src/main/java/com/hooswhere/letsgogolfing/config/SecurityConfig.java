package com.hooswhere.letsgogolfing.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration
 * Configures API key authentication for all endpoints
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ApiKeyConfigProps.class)
public class SecurityConfig {

    private final ApiKeyAuthFilter apiKeyAuthFilter;

    public SecurityConfig(ApiKeyAuthFilter apiKeyAuthFilter) {
        this.apiKeyAuthFilter = apiKeyAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF since we're using API key authentication
            .csrf(AbstractHttpConfigurer::disable)

            // Configure session management - stateless for API
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Allow actuator health endpoint without auth
                .requestMatchers("/actuator/health").permitAll()
                // Allow Swagger UI without auth (optional - remove if you want to protect it)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // TODO: Require authentication for all other endpoints, for now. API key is enough
                .anyRequest().permitAll()
            )

            // Add API key filter before standard authentication
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
