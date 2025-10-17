package com.hooswhere.letsgogolfing.dto;

import java.util.List;

/**
 * Detailed tee time results containing actual bookable tee time slots.
 */
public record TeeTimeResultsDetailed(
        int startIndex,
        List<TeeTimeSlot> teeTimes,
        List<Object> featuredFacilities,
        List<Object> promotedCampaignCollection
) {
}
