package com.hooswhere.letsgogolfing.dto;

/**
 * Detailed facility information for a specific tee time.
 */
public record FacilityDetails(
        int facilityId,
        String name,
        Address address,
        double latitude,
        double longitude,
        double averageRating,
        int reviewCount,
        String imagePathURL,
        String phoneNumber,
        double timeZoneOffset
) {
}
