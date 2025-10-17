package com.hooswhere.letsgogolfing.golfnow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hooswhere.letsgogolfing.browserbase.AuthService;
import com.hooswhere.letsgogolfing.browserbase.BrowserBaseClient;
import com.hooswhere.letsgogolfing.dto.AuthTokens;
import com.hooswhere.letsgogolfing.dto.*;
import com.hooswhere.letsgogolfing.dto.Facility;
import com.hooswhere.letsgogolfing.dto.FacilityTeeTimeRequest;
import com.hooswhere.letsgogolfing.dto.FacilityTeeTimeResponse;
import com.hooswhere.letsgogolfing.dto.SearchCriteria;
import com.hooswhere.letsgogolfing.dto.TeeTime;
import com.hooswhere.letsgogolfing.dto.TeeTimeRate;
import com.hooswhere.letsgogolfing.dto.TeeTimeResult;
import com.hooswhere.letsgogolfing.dto.TeeTimeResults;
import com.hooswhere.letsgogolfing.dto.TeeTimeSearchRequest;
import com.hooswhere.letsgogolfing.dto.TeeTimeSearchResponse;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.dto.UserPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GolfNowService {
    private static final Logger LOG = LoggerFactory.getLogger(GolfNowService.class);
    private final AuthService authService;
    private final GolfNowConfigProps configProps;
    private final ObjectMapper objectMapper;
    private final BrowserBaseClient client;
    private final RestTemplate restTemplate;

    public GolfNowService(AuthService authService, GolfNowConfigProps configProps, ObjectMapper objectMapper,
                     BrowserBaseClient client, RestTemplate restTemplate) {
        this.authService = authService;
        this.configProps = configProps;
        this.objectMapper = objectMapper;
        this.client = client;
        this.restTemplate = restTemplate;
    }

    public AuthTokens login() {
        return null; // TODO implement login using authService
    }


    public List<TeeTimeSlot> fetchTeeTimes(Map<String, Object> cookies, UserPreferences userPreferences) {
        SearchCriteria criteria = userPreferences.searchCriteria();

        // Step 1: Fetch facilities within radius (general search)
        TeeTimeSearchRequest request = buildTeeTimeSearchRequest(criteria);
        String url = configProps.baseUrl() + configProps.endpoints().teeTimeResults();

        TeeTimeResults generalResults;
        try {
            // It seems like cookies are unnecessary for fetching tee times and facilities, so not passing in for now
//            HttpHeaders headers = HttpClientUtils.createGolfNowHeaders(cookies);
            HttpEntity<TeeTimeSearchRequest> entity = new HttpEntity<>(request);

            ResponseEntity<TeeTimeSearchResponse> response =
                restTemplate.postForEntity(url, entity, TeeTimeSearchResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                generalResults = response.getBody().ttResults();
            } else {
                throw new RuntimeException("Failed to fetch tee times: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching tee times from GolfNow API", e);
        }

        // Step 2: Filter facilities by time preferences
        List<Facility> filteredFacilities =
                filterFacilitiesByTimePreferences(generalResults.facilities(), criteria);

        // Step 2.5: Further filter to only priority course IDs to limit API calls
        // This prevents spamming the GolfNow API with too many requests
        List<Facility> facilitiesToFetch = filteredFacilities;
        if (criteria.priorityCourseIds() != null && !criteria.priorityCourseIds().isEmpty()) {
            facilitiesToFetch = filteredFacilities.stream()
                    .filter(facility -> criteria.priorityCourseIds().contains(facility.id()))
                    .toList();

            LOG.info("Filtered to {} priority facilities out of {} total facilities",
                    facilitiesToFetch.size(), filteredFacilities.size());
        } else {
            LOG.warn("No priority course IDs specified - this will make API calls for {} facilities. " +
                     "Consider limiting to 3-5 priority courses to avoid spamming the API.",
                    facilitiesToFetch.size());
            facilitiesToFetch = filteredFacilities.stream()
                    .limit(1)  // Limit to first facilities if no priorities specified
                    .collect(Collectors.toList());
        }

        // Step 3: For each priority facility, fetch specific tee times
        // TODO: Consider implementing rate limiting or proxying these calls in the future
        List<TeeTimeSlot> allMatchingTeeTimeSlots = new ArrayList<>();

        // TODO: when comfortable, enable looping again
        for (Facility facility : facilitiesToFetch) {
            try {
                LOG.debug("Fetching tee times for facility: {} (ID: {})", facility.name(), facility.id());

                FacilityTeeTimeResponse facilityResponse =
                        fetchFacilityTeeTimes(cookies, facility.id(), criteria);

                if (facilityResponse != null &&
                    facilityResponse.ttResults() != null &&
                    facilityResponse.ttResults().teeTimes() != null) {

                    // Step 4: Filter tee times by preferences (18 holes, time range)
                    List<TeeTimeSlot> filteredSlots =
                            filterTeeTimesByPreferences(facilityResponse.ttResults().teeTimes(), criteria);

                    allMatchingTeeTimeSlots.addAll(filteredSlots);

                    LOG.info("Found {} matching tee times at {}", filteredSlots.size(), facility.name());
                }
            } catch (Exception e) {
                LOG.error("Error fetching tee times for facility {} ({}): {}",
                        facility.name(), facility.id(), e.getMessage());
            }
        }

        LOG.info("Total matching tee times found: {} across {} facilities",
                allMatchingTeeTimeSlots.size(), facilitiesToFetch.size());

        // TODO: Update return type to include the actual tee time slots (allMatchingTeeTimeSlots)
        // For now, return the filtered facilities
        return allMatchingTeeTimeSlots;
    }

    /**
     * Searches for facilities matching the given criteria without fetching specific tee times.
     * This is useful for letting users browse and select which facilities to prioritize.
     * Does NOT make per-facility API calls - only returns the general search results.
     */
    public List<FacilitySummary> searchFacilities(Map<String, Object> cookies, SearchCriteria criteria) {
        // Make general search to get facilities
        TeeTimeSearchRequest request = buildTeeTimeSearchRequest(criteria);
        String url = configProps.baseUrl() + configProps.endpoints().teeTimeResults();

        TeeTimeResults generalResults;
        try {
            // It seems like cookies are unnecessary for fetching tee times and facilities, so not passing in for now
//            HttpHeaders headers = HttpClientUtils.createGolfNowHeaders(cookies);
            HttpEntity<TeeTimeSearchRequest> entity = new HttpEntity<>(request);

            ResponseEntity<TeeTimeSearchResponse> response =
                    restTemplate.postForEntity(url, entity, TeeTimeSearchResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                generalResults = response.getBody().ttResults();
            } else {
                throw new RuntimeException("Failed to search facilities: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error searching facilities from GolfNow API", e);
        }

        // Filter by time preferences
        List<Facility> filteredFacilities =
                filterFacilitiesByTimePreferences(generalResults.facilities(), criteria);

        // Convert to simplified summary objects
        List<FacilitySummary> summaries = filteredFacilities.stream()
                .map(FacilitySummary::fromFacility)
                .toList();

        LOG.info("Found {} facilities matching search criteria", summaries.size());

        return summaries;
    }

    private TeeTimeSearchRequest buildTeeTimeSearchRequest(SearchCriteria criteria) {
        return new TeeTimeSearchRequest(
                criteria.radiusMiles(),
                criteria.latitude(),
                criteria.longitude(),
                30,  // PageSize
                0,   // PageNumber
                0,   // SearchType (4 = standard search) 0 was in latest call?
                "Facilities.Distance",  // SortBy
                0,   // SortDirection (0 = ascending)
                criteria.searchDate(),
                criteria.hotDealsOnly() ? "true" : "false",
                0,   // PriceMin in cents
                criteria.maxPrice() != null ? criteria.maxPrice() * 100 : 10000,  // PriceMax in cents
                criteria.numberOfPlayers(),
                3,   // TimePeriod (3 = all day)
                criteria.holes(),
                0,   // FacilityType (0 = all)
                "all",  // RateType
                "10", // browser does this
                "42", // browser does this
//                criteria.preferredTimeStart() != null ? criteria.preferredTimeStart().toString() : "6",
//                criteria.preferredTimeEnd() != null ? criteria.preferredTimeEnd().toString() : "19",
                "Facilities.Distance",  // SortByRollup
                "Course",  // View
                false,  // ExcludeFeaturedFacilities
                20,  // TeeTimeCount
                "false",  // PromotedCampaignsOnly
                java.time.Instant.now().toString()  // CurrentClientDate
        );
    }

    /**
     * Filters facilities based on whether their available times fall within user preferences.
     * Uses a 15-minute buffer for time matching.
     * TODO: This logic can be enhanced later to include other filters (price, hot deals, etc.)
     */
    private List<Facility> filterFacilitiesByTimePreferences(
            List<Facility> facilities,
            SearchCriteria criteria) {

        if (criteria.preferredTimeStart() == null && criteria.preferredTimeEnd() == null) {
            return facilities;  // No time preferences, return all
        }

        LocalTime preferredStart = criteria.preferredTimeStart() != null
                ? LocalTime.of(criteria.preferredTimeStart(), 0)
                : LocalTime.MIN;
        LocalTime preferredEnd = criteria.preferredTimeEnd() != null
                ? LocalTime.of(criteria.preferredTimeEnd(), 0)
                : LocalTime.MAX;
        int bufferMinutes = 15;

        return facilities.stream()
                .filter(facility -> {
                    String minDate = facility.minDate();  // ISO 8601: "2025-10-18T07:00:00"
                    String maxDate = facility.maxDate();

                    if (minDate == null || maxDate == null) {
                        return false;
                    }

                    try {
                        LocalDateTime facilityStartTime = LocalDateTime.parse(minDate);
                        LocalDateTime facilityEndTime = LocalDateTime.parse(maxDate);

                        LocalTime facilityStart = facilityStartTime.toLocalTime();
                        LocalTime facilityEnd = facilityEndTime.toLocalTime();

                        // Apply buffer
                        LocalTime bufferedPreferredStart = preferredStart.minusMinutes(bufferMinutes);
                        LocalTime bufferedPreferredEnd = preferredEnd.plusMinutes(bufferMinutes);

                        // Check if facility's time range overlaps with user's preference
                        return !facilityEnd.isBefore(bufferedPreferredStart) &&
                               !facilityStart.isAfter(bufferedPreferredEnd);
                    } catch (Exception e) {
                        // If parsing fails, include the facility
                        return true;
                    }
                })
                .toList();
    }

    /**
     * Fetches specific tee times for a given facility.
     */
    private FacilityTeeTimeResponse fetchFacilityTeeTimes(
            Map<String, Object> cookies,
            int facilityId,
            SearchCriteria criteria) {

        FacilityTeeTimeRequest request = buildFacilityTeeTimeRequest(facilityId, criteria);
        String url = configProps.baseUrl() + configProps.endpoints().teeTimeResults();

        try {
            HttpHeaders headers = HttpClientUtils.createGolfNowHeaders(cookies);
            HttpEntity<FacilityTeeTimeRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<FacilityTeeTimeResponse> response =
                    restTemplate.postForEntity(url, entity, FacilityTeeTimeResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to fetch facility tee times: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching facility tee times from GolfNow API", e);
        }
    }

    private FacilityTeeTimeRequest buildFacilityTeeTimeRequest(int facilityId, SearchCriteria criteria) {
        return new FacilityTeeTimeRequest(
                criteria.radiusMiles(),
                criteria.latitude(),
                criteria.longitude(),
                1000,  // PageSize - large to get all tee times
                0,   // PageNumber
                1,   // SearchType (1 = facility-specific search)
                "Date",  // SortBy
                0,   // SortDirection (0 = ascending)
                criteria.searchDate(),
                criteria.hotDealsOnly() ? "true" : "false",
                false,  // BestDealsOnly
                "0",  // PriceMin (string)
                criteria.maxPrice() != null ? String.valueOf(criteria.maxPrice() * 100) : "10000",  // PriceMax (string)
                String.valueOf(criteria.numberOfPlayers()),  // Players (string)
                String.valueOf(criteria.holes()),  // Holes (string)
                "0",  // FacilityType (string)
                "all",  // RateType
                "10", // browser does this
                "42", // browser does this
//                criteria.preferredTimeStart() != null ? criteria.preferredTimeStart().toString() : "6",
//                criteria.preferredTimeEnd() != null ? criteria.preferredTimeEnd().toString() : "19",
                facilityId,  // FacilityId - required
                "Date.MinDate",  // SortByRollup
                "Grouping",  // View
                true,  // ExcludeFeaturedFacilities
                20,  // TeeTimeCount
                "false",  // PromotedCampaignsOnly
                "Hoboken, New Jersey, US",  // Q - optional
                "GeoLocation",  // QC - optional
                java.time.Instant.now().toString()  // CurrentClientDate
        );
    }

    /**
     * Filters tee time slots based on user preferences.
     * Currently filters for:
     * - 18 hole options only (TODO: make this configurable)
     * - Time preferences with 15-minute buffer
     */
    private List<TeeTimeSlot> filterTeeTimesByPreferences(
            List<TeeTimeSlot> teeTimeSlots,
            SearchCriteria criteria) {

        LocalTime preferredStart = criteria.preferredTimeStart() != null
                ? LocalTime.of(criteria.preferredTimeStart(), 0)
                : LocalTime.MIN;
        LocalTime preferredEnd = criteria.preferredTimeEnd() != null
                ? LocalTime.of(criteria.preferredTimeEnd(), 0)
                : LocalTime.MAX;
        int bufferMinutes = 15;

        LocalTime bufferedStart = preferredStart.minusMinutes(bufferMinutes);
        LocalTime bufferedEnd = preferredEnd.plusMinutes(bufferMinutes);

        return teeTimeSlots.stream()
                .filter(slot -> {
                    // Parse tee time
                    try {
                        LocalDateTime teeTime = LocalDateTime.parse(slot.time());
                        LocalTime slotTime = teeTime.toLocalTime();

                        // Check time is within preference range (with buffer)
                        boolean withinTimeRange = !slotTime.isBefore(bufferedStart) &&
                                                 !slotTime.isAfter(bufferedEnd);

                        if (!withinTimeRange) {
                            return false;
                        }

                        // Filter for 18-hole options only (for now)
                        // TODO: Make hole count configurable via SearchCriteria
                        boolean has18HoleOption = slot.teeTimeRates().stream()
                                .anyMatch(TeeTimeRate::isEightteen);

                        return has18HoleOption;

                    } catch (Exception e) {
                        // If parsing fails, exclude this slot
                        return false;
                    }
                })
                .toList();
    }
}
