package com.hooswhere.letsgogolfing.browserbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Client for interacting with BrowserBase REST API
 */
@Component
public class BrowserBaseClient {
    private final BBConfigProps configProps;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BrowserBaseClient(BBConfigProps configProps,
            ObjectMapper objectMapper) {
        this.configProps = configProps;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Create a new BrowserBase session
     * @return BrowserBaseSession with connectUrl for Playwright
     */
    public BrowserBaseSession createSession() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
            "projectId", configProps.projectId(),
            "keepAlive", true,
            "browserSettings", Map.of(
                "solveCaptchas", true, // defaults to true
                "blockAds", true,
                "timeout", 90 // session timeout in seconds
            )
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(configProps.baseUrl() + "/sessions"))
                .header("Content-Type", "application/json")
                .header("X-BB-API-Key", configProps.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new RuntimeException("Failed to create BrowserBase session: " + response.body());
        }

        return objectMapper.readValue(response.body(), BrowserBaseSession.class);
    }

    /**
     * Get session details by ID
     */
    public BrowserBaseSession getSession(String sessionId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(configProps.baseUrl() + "/sessions/" + sessionId))
                .header("X-BB-API-Key", configProps.apiKey())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get BrowserBase session: " + response.body());
        }

        return objectMapper.readValue(response.body(), BrowserBaseSession.class);
    }

    /**
     * Stop/complete a session
     */
    public void stopSession(String sessionId) throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
            "status", "REQUEST_RELEASE"
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(configProps.baseUrl() + "/sessions/" + sessionId))
                .header("Content-Type", "application/json")
                .header("X-BB-API-Key", configProps.apiKey())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to stop BrowserBase session: " + response.body());
        }
    }
}
