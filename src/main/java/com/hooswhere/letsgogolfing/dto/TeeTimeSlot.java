package com.hooswhere.letsgogolfing.dto;

import java.util.List;

/**
 * Represents a single bookable tee time slot with facility info and pricing.
 */
public record TeeTimeSlot(
        FacilityDetails facility,
        List<TeeTimeRate> teeTimeRates,
        String time,  // ISO 8601: "2025-10-18T07:07:00"
        String detailUrl,  // "/tee-times/facility/4047/tee-time/1855229670"
        String formattedTime,  // "7:07"
        String formattedTimeMeridian,  // "AM"
        int facilityId,
        long defaultTeeTimeRateId,
        double displayRate,
        String minRateFormatted,
        boolean isPriceRangeZero
) {
}
