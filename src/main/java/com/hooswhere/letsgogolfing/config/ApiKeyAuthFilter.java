package com.hooswhere.letsgogolfing.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API Key Authentication Filter
 * Validates Bearer token from Authorization header against configured API key
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ApiKeyConfigProps apiKeyConfig;

    public ApiKeyAuthFilter(ApiKeyConfigProps apiKeyConfig) {
        this.apiKeyConfig = apiKeyConfig;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/api/stripe/webhook".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Check if Authorization header is present
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }

        // Extract token
        String token = authHeader.substring(BEARER_PREFIX.length());

        // Validate token
        if (!apiKeyConfig.apiKey().equals(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid API key\"}");
            return;
        }

        // Token is valid, continue with the request
        filterChain.doFilter(request, response);
    }
}
