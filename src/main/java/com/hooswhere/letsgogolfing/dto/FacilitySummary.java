package com.hooswhere.letsgogolfing.dto;

/**
 * Simplified facility information for user selection.
 * Contains only the essential details needed for users to choose which facilities to prioritize.
 */
public record FacilitySummary(
        int id,                        // Facility ID - needed for prioritization
        String name,                   // Course name
        String imageUrl,               // Course image
        String address,                // Formatted address for display
        double distance,               // Distance in miles from search center
        String formattedDistance,      // "1.2 mi"
        double averageRating,          // Course rating (e.g., 4.5)
        int numberOfReviews,           // Review count
        String availableTimeRange,     // "7:00 AM - 7:00 PM"
        String priceRange,             // "$28 - $75"
        boolean hasHotDeal             // Special pricing available
) {
    /**
     * Creates a FacilitySummary from a full Facility object.
     */
    public static FacilitySummary fromFacility(Facility facility) {
        // Format address
        String addressStr = facility.address() != null
                ? String.format("%s, %s, %s %s",
                    facility.address().city(),
                    facility.address().stateProvinceCode(),
                    facility.address().postalCode(),
                    facility.address().country())
                : "Address not available";

        // Format time range
        String minTime = facility.minDate() != null ? facility.minDate().formatted() + " " + facility.minDate().formattedTimeMeridian() : "N/A";
        String maxTime = facility.maxDate() != null ? facility.maxDate().formatted() + " " + facility.maxDate().formattedTimeMeridian() : "N/A";
        String timeRange = minTime + " - " + maxTime;

        // Format price range
        String minPriceStr = facility.minPrice() != null ? facility.minPrice().formattedValue2() : "$0";
        String maxPriceStr = facility.maxPrice() != null ? facility.maxPrice().formattedValue2() : "$0";
        String priceRange = minPriceStr + " - " + maxPriceStr;

        return new FacilitySummary(
                facility.id(),
                facility.name(),
                facility.thumbnailImagePath(),
                addressStr,
                facility.distance(),
                facility.formattedDistance() != null ? facility.formattedDistance() : String.format("%.1f mi", facility.distance()),
                facility.averageRating(),
                facility.numberOfReviews(),
                timeRange,
                priceRange,
                facility.hasHotDeal()
        );
    }
}
