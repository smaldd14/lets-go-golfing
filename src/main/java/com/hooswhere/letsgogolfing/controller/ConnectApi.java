package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.ResendConnectRequest;
import com.hooswhere.letsgogolfing.dto.ResendConnectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@Tag(name = "Connect", description = "APIs supporting the connect/onboarding page")
@RequestMapping("/api/connect")
public interface ConnectApi {

    @Operation(
            summary = "Resend the connect setup link",
            description = "Re-emails the /connect setup link to the address on file if it has an active " +
                          "subscription. Always returns a generic 200 regardless of whether an account " +
                          "exists, to avoid email enumeration."
    )
    @PostMapping("/resend")
    ResendConnectResponse resend(@RequestBody ResendConnectRequest request);
}
