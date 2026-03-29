package com.hooswhere.letsgogolfing.dto;

import java.util.List;

public record Facility(
        int id,                        // Unique facility ID
        String name,                   // Course name
        Address address,
        String currencyCode,           // "USD"
        double latitude,
        double longitude,
        double distance,               // Distance in miles from search center
        int numberOfReviews,
        double averageRating,
        DateInfo minDate,              // was String, now object with date/formatted/formattedTimeMeridian
        @Deprecated String minDateFormatted,       // deprecated: use minDate.formatted()
        DateInfo maxDate,              // was String, now object with date/formatted/formattedTimeMeridian
        @Deprecated String maxDateFormatted,       // deprecated: use maxDate.formatted()
        PriceInfo minPrice,            // was double, now object with value/formattedValue2/etc
        @Deprecated String minPriceFormatted,      // deprecated: use minPrice.formattedValue2()
        @Deprecated String minPriceFormatted2,
        @Deprecated String minPriceSuperScriptFormattedValue,
        PriceInfo maxPrice,            // was double, now object with value/formattedValue2/etc
        @Deprecated String maxPriceFormatted,      // deprecated: use maxPrice.formattedValue2()
        @Deprecated String maxPriceFormatted2,
        @Deprecated String maxPriceSuperScriptFormattedValue,
        boolean isPriceRangeZero,
        boolean hasHotDeal,            // Special pricing available
        List<String> tags,             // e.g., ["GOLFENT", "NORAM", "SIM"]
        String thumbnailImagePath,     // Course image URL
        boolean isSmartPlay,
        boolean isSimulator,
        boolean hasMemberPricing,
        String formattedTimeMeridian,
        String subscriptFormattedTimeMeridian,
        boolean isTimeRangeZero,
        String formattedTimeMeridian_Min,
        String subscriptFormattedTimeMeridian_Min,
        boolean isFeatured,
        boolean isPremium,
        boolean isPnas,
        boolean isTrackman,
        boolean isPrivate,
        boolean isNewCourse,
        String formattedDistance       // "1 mi"
) {}

