package com.hooswhere.letsgogolfing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TeeTimeResultDbDto(
        UUID id,
        int facilityId,
        String facilityName,
        LocalDateTime teeTime,
        BigDecimal price,
        String bookingUrl,
        LocalDateTime firstSeenAt,
        LocalDateTime lastSeenAt
) {
}
