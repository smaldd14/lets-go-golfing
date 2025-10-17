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
        String timeRange = String.format("%s - %s",
                facility.minDateFormatted() != null ? facility.minDateFormatted() : "N/A",
                facility.maxDateFormatted() != null ? facility.maxDateFormatted() : "N/A");

        // Format price range
        String priceRange = String.format("%s - %s",
                facility.minPriceFormatted() != null ? facility.minPriceFormatted() : "$0",
                facility.maxPriceFormatted() != null ? facility.maxPriceFormatted() : "$0");

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
