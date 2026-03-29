package com.hooswhere.letsgogolfing.golfnow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hooswhere.letsgogolfing.dto.AuthTokens;
import com.hooswhere.letsgogolfing.dto.DateInfo;
import com.hooswhere.letsgogolfing.dto.*;
import com.hooswhere.letsgogolfing.dto.Facility;
import com.hooswhere.letsgogolfing.dto.FacilityTeeTimeRequest;
import com.hooswhere.letsgogolfing.dto.FacilityTeeTimeResponse;
import com.hooswhere.letsgogolfing.dto.SearchCriteria;
import com.hooswhere.letsgogolfing.dto.TeeTimeRate;
import com.hooswhere.letsgogolfing.dto.TeeTimeResults;
import com.hooswhere.letsgogolfing.dto.TeeTimeSearchRequest;
import com.hooswhere.letsgogolfing.dto.TeeTimeSearchResponse;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GolfNowService {
    private static final Logger LOG = LoggerFactory.getLogger(GolfNowService.class);
    private final GolfNowConfigProps configProps;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public GolfNowService(GolfNowConfigProps configProps, ObjectMapper objectMapper,
                     RestTemplate restTemplate) {
        this.configProps = configProps;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    public AuthTokens login() {
        return null; // TODO implement login using authService
    }

    /**
     * Fetches tee times for specific facilities based on given IDs and search criteria.
     * @param cookies cookies
     * @param facilityIds list of facility IDs to fetch tee times for
     * @param criteria search criteria
     * @return list of matching tee time slots
     */
    public List<TeeTimeSlot> fetchTeeTimesForFacilities(Map<String, Object> cookies, List<Integer> facilityIds,
                                                        SearchCriteriaDbDto criteria) {
        List<TeeTimeSlot> allMatchingTeeTimeSlots = new ArrayList<>();

        for (int id : facilityIds) {
            try {
                LOG.debug("Fetching tee times for facility ID: {}", id);

                FacilityTeeTimeResponse facilityResponse =
                        fetchFacilityTeeTimes(cookies, id, criteria);

                if (facilityResponse.ttResults() != null &&
                    facilityResponse.ttResults().teeTimes() != null) {

                    // Step 4: Filter tee times by preferences (18 holes, time range)
                    List<TeeTimeSlot> filteredSlots =
                            filterTeeTimesByPreferences(facilityResponse.ttResults().teeTimes(), criteria);

                    allMatchingTeeTimeSlots.addAll(filteredSlots);

                    LOG.info("Found {} matching tee times at {}", filteredSlots.size(), id);
                }
            } catch (Exception e) {
                LOG.error("Error fetching tee times for facility id {}", id, e);
            }
        }
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
        String minTime = criteria.preferredTimeStart() != null
                ? convertHourToApiValue(criteria.preferredTimeStart())
                : "10";  // 5am default
        String maxTime = criteria.preferredTimeEnd() != null
                ? convertHourToApiValue(criteria.preferredTimeEnd())
                : "42";  // 9pm+ default

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
                minTime,
                maxTime,
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
                    DateInfo minDate = facility.minDate();
                    DateInfo maxDate = facility.maxDate();

                    if (minDate == null || minDate.date() == null || maxDate == null || maxDate.date() == null) {
                        return false;
                    }

                    try {
                        LocalDateTime facilityStartTime = minDate.toLocalDateTime();
                        LocalDateTime facilityEndTime = maxDate.toLocalDateTime();

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
            SearchCriteriaDbDto criteria) {

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

    private FacilityTeeTimeRequest buildFacilityTeeTimeRequest(int facilityId, SearchCriteriaDbDto criteria) {
        String minTime = convertHourToApiValue(criteria.preferredTimeStart());  // 5am default
        String maxTime = convertHourToApiValue(criteria.preferredTimeEnd());  // 9pm+ default

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
                minTime,
                maxTime,
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
            SearchCriteriaDbDto criteria) {

        LocalTime preferredStart = LocalTime.of(criteria.preferredTimeStart(), 0);
        LocalTime preferredEnd = LocalTime.of(criteria.preferredTimeEnd(), 0);
        int bufferMinutes = 15;

        LocalTime bufferedStart = preferredStart.minusMinutes(bufferMinutes);
        LocalTime bufferedEnd = preferredEnd.plusMinutes(bufferMinutes);

        return teeTimeSlots.stream()
                .filter(slot -> {
                    // Parse tee time
                    try {
                        LocalDateTime teeTime = slot.time().toLocalDateTime();
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
                                .anyMatch(rate -> rate.isEightteen() || rate.holeCount() == 18);

                        return has18HoleOption;

                    } catch (Exception e) {
                        // If parsing fails, exclude this slot
                        return false;
                    }
                })
                .toList();
    }

    /**
     * Converts hour (0-23) to GolfNow API time value.
     * Formula: (hour - 5) * 2 + 10
     * Examples: 5am=10, 6am=12, 8pm=40, 9pm=42
     * Range: 5am (10) to 9pm+ (42)
     * Returns "10" (5am) for hours < 5, "42" (9pm+) for hours > 21
     */
    private String convertHourToApiValue(int hour) {
        if (hour < 5) {
            return "10";  // 5am
        }
        if (hour > 21) {
            return "42";  // 9pm+
        }
        return String.valueOf((hour - 5) * 2 + 10);
    }
}
