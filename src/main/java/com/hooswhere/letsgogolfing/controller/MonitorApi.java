package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.CreateMonitorRequest;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Validated
@Tag(name = "Monitors", description = "Subscription-gated server-side creation of tee time monitors (used by the MCP server)")
@RequestMapping("/api/monitors")
public interface MonitorApi {

    @Operation(
            summary = "Create a tee time monitor",
            description = "Creates a search preference and starts its monitoring schedule for the given user. " +
                          "Requires an active subscription (402 otherwise). Enforces one active monitor per user " +
                          "(409 with error 'monitor_exists' unless replace=true, which replaces the existing one)."
    )
    @PostMapping
    ResponseEntity<UserSearchPreferenceDto> createMonitor(
            @RequestBody CreateMonitorRequest request,
            @Parameter(description = "Replace the user's existing active monitor instead of failing", example = "false")
            @RequestParam(defaultValue = "false") boolean replace
    );
}
