package com.hooswhere.letsgogolfing.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SearchCriteriaDbDto(
        UUID id,
        double latitude,
        double longitude,
        int radiusMiles,
        String searchDate,
        int numberOfPlayers,
        int preferredTimeStart,
        int preferredTimeEnd,
        Integer maxPrice,
        boolean hotDealsOnly,
        int holes,
        LocalDateTime createdAt,
        List<Integer> priorityCourseIds
) {
}
