package com.hooswhere.letsgogolfing.dto;

import java.util.List;

/**
 * Represents a single bookable tee time slot with facility info and pricing.
 */
public record TeeTimeSlot(
        FacilityDetails facility,
        List<TeeTimeRate> teeTimeRates,
        DateInfo time,  // was String, now object with date/formatted/formattedTimeMeridian
        String detailUrl,  // "/tee-times/facility/4817/tee-time/1867224724"
        @Deprecated String formattedTime,  // deprecated: use time.formatted()
        @Deprecated String formattedTimeMeridian,  // deprecated: use time.formattedTimeMeridian()
        int facilityId,
        long defaultTeeTimeRateId,
        PriceInfo displayRate,          // was double, now object with value/formattedValue2/etc
        @Deprecated String minRateFormatted,  // deprecated: use displayRate.formattedValue2()
        boolean isPriceRangeZero
) {
}
