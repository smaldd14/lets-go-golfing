package com.hooswhere.letsgogolfing.dto;

/**
 * Response DTO for GolfNow tee time search API.
 * Maps the response from POST https://www.golfnow.com/api/tee-times/tee-time-results
 */
public record TeeTimeSearchResponse(TeeTimeResults ttResults) {
}
