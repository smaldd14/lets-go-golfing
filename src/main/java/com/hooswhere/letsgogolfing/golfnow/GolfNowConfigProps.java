package com.hooswhere.letsgogolfing.golfnow;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "golfnow")
public record GolfNowConfigProps(@NotBlank String email,
                                 @NotBlank String password,
                                 @NotBlank String baseUrl,
                                 @NotNull @Valid Endpoints endpoints) {

    public record Endpoints(@NotBlank String teeTimeResults) {
        // Add more endpoints here as needed, e.g.:
        // @NotBlank String facilityDetails,
        // @NotBlank String booking,
        // etc.
    }
}
