package com.hooswhere.letsgogolfing.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserNotificationDbDto(
        UUID id,
        UUID userSearchPreferenceId,
        UUID teeTimeResultId,
        LocalDateTime notifiedAt
) {
}
