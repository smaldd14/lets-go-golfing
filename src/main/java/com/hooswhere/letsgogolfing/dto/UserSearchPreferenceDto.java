package com.hooswhere.letsgogolfing.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserSearchPreferenceDto(
        UUID id,
        String email,
        SearchCriteriaDbDto searchCriteria,
        boolean paymentEnabled,
        boolean notifyEnabled,
        String scheduleId,
        Duration scheduleInterval,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
