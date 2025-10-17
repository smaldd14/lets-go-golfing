package com.hooswhere.letsgogolfing.browserbase;

/**
 * Represents a BrowserBase session response
 */
public record BrowserBaseSession(
    String id,
    String connectUrl,
    String seleniumRemoteUrl,
    String status,
    String createdAt
) {}
