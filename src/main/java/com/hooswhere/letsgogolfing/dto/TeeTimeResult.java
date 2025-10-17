package com.hooswhere.letsgogolfing.dto;

import java.util.List;

public record TeeTimeResult(
        PromotedCampaigns promotedCampaigns,
        List<Integer> emptyFacilities,
        List<Facility> facilities
) {}