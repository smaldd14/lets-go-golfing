package com.hooswhere.letsgogolfing.dto;

import java.util.List;

public record SearchCriteria(
        double latitude,
        double longitude,
        int radiusMiles,
        String searchDate,             // Format: "Oct 11 2025"
        int numberOfPlayers,           // 0 = any, or specific number (1-4)
        Integer preferredTimeStart,    // Optional: earliest acceptable time (e.g., 10 = 10:00 AM)
        Integer preferredTimeEnd,      // Optional: latest acceptable time (e.g., 18 = 6:00 PM)
        List<Integer> priorityCourseIds, // List of facility IDs to prioritize
        Integer maxPrice,              // Optional: maximum price in dollars
        boolean hotDealsOnly,
        int holes,                     // 1=9 holes, 2=18 holes, 3=both
        int checkIntervalMinutes       // How often to poll (e.g., 15)
) {}
