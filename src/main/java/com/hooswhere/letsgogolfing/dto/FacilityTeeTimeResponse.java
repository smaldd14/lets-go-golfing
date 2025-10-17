package com.hooswhere.letsgogolfing.dto;

import java.util.List;

/**
 * Response DTO for facility-specific tee time search.
 */
public record FacilityTeeTimeResponse(
        TeeTimeResultsDetailed ttResults,
        int total,
        String date,
        boolean limitReached,
        Object ttException
) {
}
