package com.hooswhere.letsgogolfing.dto;

import java.time.Duration;

public record CreateUserSearchRequest(
        String email,
        SearchCriteria searchCriteria,
        boolean paymentEnabled,
        boolean notifyEnabled,
        Duration scheduleInterval
) {
}
