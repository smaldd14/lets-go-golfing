package com.hooswhere.letsgogolfing.browserbase;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "bbconfig")
public record BBConfigProps(@NotBlank String apiKey,
                            @NotBlank String projectId,
                            @NotBlank String baseUrl,
                            @NotBlank String email,
                            @NotBlank String password) {
}
