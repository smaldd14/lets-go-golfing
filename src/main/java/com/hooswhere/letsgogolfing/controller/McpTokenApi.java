package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.TokenIssueRequest;
import com.hooswhere.letsgogolfing.dto.TokenIssueResponse;
import com.hooswhere.letsgogolfing.dto.TokenResolveRequest;
import com.hooswhere.letsgogolfing.dto.TokenResolveResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@Tag(name = "MCP Tokens", description = "APIs for resolving per-user MCP bearer tokens")
@RequestMapping("/api/mcp/tokens")
public interface McpTokenApi {

    @Operation(
            summary = "Resolve an MCP token",
            description = "Resolves a per-user MCP token to its owner email and current subscription status. " +
                          "Returns 401 if the token is unknown or revoked. Called by the MCP worker."
    )
    @PostMapping("/resolve")
    TokenResolveResponse resolve(@RequestBody TokenResolveRequest request);

    @Operation(
            summary = "Issue a fresh MCP token",
            description = "Rotates and returns a new per-user MCP token (revoking any prior token). " +
                          "Requires an active subscription, otherwise returns 402. The raw token is " +
                          "returned only here. Called by the connect-page worker after verifying a paid " +
                          "Stripe checkout session."
    )
    @PostMapping("/issue")
    TokenIssueResponse issue(@RequestBody TokenIssueRequest request);
}
