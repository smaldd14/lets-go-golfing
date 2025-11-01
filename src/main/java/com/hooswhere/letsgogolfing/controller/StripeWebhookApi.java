package com.hooswhere.letsgogolfing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@RequestMapping("/api/stripe")
@Tag(name = "Stripe Webhooks", description = "APIs for handling Stripe payment webhooks")
public interface StripeWebhookApi {

    @Operation(
            summary = "Handle Stripe webhook events",
            description = "Receives and processes webhook events from Stripe, including checkout.session.completed. " +
                         "Creates user search preferences and starts monitoring schedules after successful payment."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Webhook processed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid signature or malformed webhook data"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error (Stripe will retry)"
            )
    })
    @PostMapping("/webhook")
    ResponseEntity<Void> handleWebhook(
            @Parameter(
                    description = "Raw webhook payload from Stripe",
                    required = true
            )
            @RequestBody String payload,

            @Parameter(
                    description = "Stripe signature header for webhook verification",
                    required = true
            )
            @RequestHeader("Stripe-Signature") String stripeSignature
    );
}
