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
        String minDate,                // ISO 8601: "2025-10-11T07:00:00"
        String minDateFormatted,       // "7:00"
        String maxDate,                // ISO 8601: "2025-10-11T19:00:00"
        String maxDateFormatted,       // "7:00"
        double minPrice,               // Minimum price (e.g., 21.99)
        String minPriceFormatted,      // "$21.99"
        String minPriceFormatted2,
        String minPriceSuperScriptFormattedValue,
        double maxPrice,               // Maximum price (e.g., 30.0)
        String maxPriceFormatted,      // "$30.00"
        String maxPriceFormatted2,
        String maxPriceSuperScriptFormattedValue,
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

