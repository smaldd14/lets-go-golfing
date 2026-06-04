package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.SubscriptionStatusDto;
import com.hooswhere.letsgogolfing.service.SubscriptionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionController implements SubscriptionApi {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public SubscriptionStatusDto getStatus(String email) {
        return subscriptionService.getStatus(email);
    }
}
