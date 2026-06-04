package com.hooswhere.letsgogolfing.dto;

import java.time.Instant;

public record SubscriptionStatusDto(boolean active, String status, Instant currentPeriodEnd) {
}
