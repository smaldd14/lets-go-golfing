package com.hooswhere.letsgogolfing.dto;

import java.util.List;

public record PromotedCampaigns(
        int total,
        String timeMin,
        String timeMax,
        List<Integer> campaignIds
) {}