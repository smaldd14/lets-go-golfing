package com.hooswhere.letsgogolfing.controller;

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
}
