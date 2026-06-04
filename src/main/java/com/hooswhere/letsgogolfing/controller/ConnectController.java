package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.ResendConnectRequest;
import com.hooswhere.letsgogolfing.dto.ResendConnectResponse;
import com.hooswhere.letsgogolfing.service.ConnectEmailService;
import com.hooswhere.letsgogolfing.service.SubscriptionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConnectController implements ConnectApi {

    private static final String GENERIC_MESSAGE =
            "If you have an active subscription, we've emailed your setup link.";

    private final SubscriptionService subscriptionService;
    private final ConnectEmailService connectEmailService;

    public ConnectController(SubscriptionService subscriptionService, ConnectEmailService connectEmailService) {
        this.subscriptionService = subscriptionService;
        this.connectEmailService = connectEmailService;
    }

    @Override
    public ResendConnectResponse resend(ResendConnectRequest request) {
        String email = request.email();
        if (email != null && !email.isBlank()) {
            subscriptionService.activeCheckoutSessionId(email)
                    .ifPresent(sessionId -> connectEmailService.sendConnectEmail(email, sessionId));
        }
        return new ResendConnectResponse(GENERIC_MESSAGE);
    }
}
