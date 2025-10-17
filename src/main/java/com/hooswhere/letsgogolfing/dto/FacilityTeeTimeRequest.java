package com.hooswhere.letsgogolfing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for facility-specific tee time search.
 * Maps to POST https://www.golfnow.com/api/tee-times/tee-time-results
 * Note: SearchType must be 1 for facility-specific search (vs 4 for general search)
 */
public record FacilityTeeTimeRequest(
        @JsonProperty("Radius") int radius,
        @JsonProperty("Latitude") double latitude,
        @JsonProperty("Longitude") double longitude,
        @JsonProperty("PageSize") int pageSize,
        @JsonProperty("PageNumber") int pageNumber,
        @JsonProperty("SearchType") int searchType,  // Must be 1
        @JsonProperty("SortBy") String sortBy,  // "Date"
        @JsonProperty("SortDirection") int sortDirection,
        @JsonProperty("Date") String date,
        @JsonProperty("HotDealsOnly") String hotDealsOnly,
        @JsonProperty("BestDealsOnly") boolean bestDealsOnly,
        @JsonProperty("PriceMin") String priceMin,
        @JsonProperty("PriceMax") String priceMax,
        @JsonProperty("Players") String players,  // String not int
        @JsonProperty("Holes") String holes,  // String not int
        @JsonProperty("FacilityType") String facilityType,
        @JsonProperty("RateType") String rateType,
        @JsonProperty("TimeMin") String timeMin,
        @JsonProperty("TimeMax") String timeMax,
        @JsonProperty("FacilityId") int facilityId,  // Required for facility search
        @JsonProperty("SortByRollup") String sortByRollup,  // "Date.MinDate"
        @JsonProperty("View") String view,  // "Grouping"
        @JsonProperty("ExcludeFeaturedFacilities") boolean excludeFeaturedFacilities,  // true
        @JsonProperty("TeeTimeCount") int teeTimeCount,
        @JsonProperty("PromotedCampaignsOnly") String promotedCampaignsOnly,
        @JsonProperty("Q") String q,  // Optional search query
        @JsonProperty("QC") String qc,  // Optional query context
        @JsonProperty("CurrentClientDate") String currentClientDate
) {
}
