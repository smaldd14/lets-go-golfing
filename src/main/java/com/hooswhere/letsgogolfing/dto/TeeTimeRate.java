package com.hooswhere.letsgogolfing.dto;

/**
 * Pricing details for a specific tee time rate option (e.g., 9 holes, 18 holes with cart).
 */
public record TeeTimeRate(
        int holeCount,
        long teeTimeRateId,
        String rateName,  // "9 Holes", "18 Holes w/ Cart"
        String transportation,  // "Walking" or cart info
        boolean isCartIncluded,
        boolean isHotDeal,
        boolean isNine,
        boolean isEightteen,
        SinglePlayerPrice singlePlayerPrice,
        String detailUrl
) {
}
