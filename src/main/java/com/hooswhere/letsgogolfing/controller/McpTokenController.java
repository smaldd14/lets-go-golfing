package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.TokenIssueRequest;
import com.hooswhere.letsgogolfing.dto.TokenIssueResponse;
import com.hooswhere.letsgogolfing.dto.TokenResolveRequest;
import com.hooswhere.letsgogolfing.dto.TokenResolveResponse;
import com.hooswhere.letsgogolfing.service.McpTokenService;
import com.hooswhere.letsgogolfing.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class McpTokenController implements McpTokenApi {

    private final McpTokenService mcpTokenService;
    private final SubscriptionService subscriptionService;

    public McpTokenController(McpTokenService mcpTokenService, SubscriptionService subscriptionService) {
        this.mcpTokenService = mcpTokenService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public TokenResolveResponse resolve(TokenResolveRequest request) {
        String email = mcpTokenService.resolveEmail(request.token())
                .orElseThrow(() -> new LggException(HttpStatus.UNAUTHORIZED, "Invalid or revoked MCP token"));

        return new TokenResolveResponse(email, subscriptionService.hasActiveSubscription(email));
    }

    @Override
    public TokenIssueResponse issue(TokenIssueRequest request) {
        String email = request.email();
        if (email == null || email.isBlank()) {
            throw new LggException(HttpStatus.BAD_REQUEST, "email is required");
        }
        if (!subscriptionService.hasActiveSubscription(email)) {
            throw new LggException(HttpStatus.PAYMENT_REQUIRED, "No active subscription for " + email);
        }

        return new TokenIssueResponse(mcpTokenService.issueToken(email));
    }
}
