package com.hooswhere.letsgogolfing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for GolfNow tee time search API.
 * Maps to POST https://www.golfnow.com/api/tee-times/tee-time-results
 * Note: GolfNow API expects PascalCase field names
 */
public record TeeTimeSearchRequest(
        @JsonProperty("Radius") int radius,
        @JsonProperty("Latitude") double latitude,
        @JsonProperty("Longitude") double longitude,
        @JsonProperty("PageSize") int pageSize,
        @JsonProperty("PageNumber") int pageNumber,
        @JsonProperty("SearchType") int searchType,
        @JsonProperty("SortBy") String sortBy,
        @JsonProperty("SortDirection") int sortDirection,
        @JsonProperty("Date") String date,
        @JsonProperty("HotDealsOnly") String hotDealsOnly,
        @JsonProperty("PriceMin") int priceMin,
        @JsonProperty("PriceMax") int priceMax,
        @JsonProperty("Players") int players,
        @JsonProperty("TimePeriod") int timePeriod,
        @JsonProperty("Holes") int holes,
        @JsonProperty("FacilityType") int facilityType,
        @JsonProperty("RateType") String rateType,
        @JsonProperty("TimeMin") String timeMin,
        @JsonProperty("TimeMax") String timeMax,
        @JsonProperty("SortByRollup") String sortByRollup,
        @JsonProperty("View") String view,
        @JsonProperty("ExcludeFeaturedFacilities") boolean excludeFeaturedFacilities,
        @JsonProperty("TeeTimeCount") int teeTimeCount,
        @JsonProperty("PromotedCampaignsOnly") String promotedCampaignsOnly,
        @JsonProperty("CurrentClientDate") String currentClientDate
) {}
