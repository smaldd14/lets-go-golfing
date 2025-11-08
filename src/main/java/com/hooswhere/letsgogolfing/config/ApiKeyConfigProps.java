package com.hooswhere.letsgogolfing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nowgolf")
public record ApiKeyConfigProps(String apiKey) {
}
