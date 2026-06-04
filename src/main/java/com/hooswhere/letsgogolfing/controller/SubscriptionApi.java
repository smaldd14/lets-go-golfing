package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.SubscriptionStatusDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Validated
@Tag(name = "Subscriptions", description = "APIs for checking recurring subscription status")
@RequestMapping("/api/subscription")
public interface SubscriptionApi {

    @Operation(
            summary = "Get subscription status for a user",
            description = "Returns whether the user has an active recurring subscription, the raw status, " +
                          "and the current period end."
    )
    @GetMapping("/status")
    SubscriptionStatusDto getStatus(
            @Parameter(description = "User email address", required = true, example = "user@example.com")
            @RequestParam String email
    );
}
