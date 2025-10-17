package com.hooswhere.letsgogolfing.dto;

import java.util.List;

/**
 * Contains the actual tee time search results including facilities and campaigns.
 */
public record TeeTimeResults(
        PromotedCampaigns promotedCampaigns,
        List<Integer> emptyFacilities,
        List<Facility> facilities
) {
}
