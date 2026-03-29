package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.FacilitySummary;
import com.hooswhere.letsgogolfing.dto.SearchCriteriaDbDto;
import com.hooswhere.letsgogolfing.dto.TeeTimeResultDbDto;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.dto.UserPreferencesLegacy;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import com.hooswhere.letsgogolfing.golfnow.GolfNowService;
import com.hooswhere.letsgogolfing.service.TeeTimeResultService;
import com.hooswhere.letsgogolfing.service.UserSearchPreferenceService;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@ActivityImpl(taskQueues = "golfnow")
public class GolfNowActivitiesImpl implements GolfNowActivities {
    private static final Logger LOG = LoggerFactory.getLogger(GolfNowActivitiesImpl.class);

    private final GolfNowService golfNowService;
    private final TeeTimeResultService teeTimeResultService;
    private final UserSearchPreferenceService userPrefsService;

    public GolfNowActivitiesImpl(GolfNowService golfNowService,
                                 TeeTimeResultService teeTimeResultService,
                                 UserSearchPreferenceService userPrefsService) {
        this.golfNowService = golfNowService;
        this.teeTimeResultService = teeTimeResultService;
        this.userPrefsService = userPrefsService;
    }

    @Override
    public UserSearchPreferenceDto loadUserSearchPreference(UUID userSearchPreferenceId) {
        try {
            UserSearchPreferenceDto userSearchPreferenceDto = userPrefsService.getPreference(userSearchPreferenceId);
            return userSearchPreferenceDto;
        } catch (NoSuchElementException e) {
            LOG.error("User search preference not found: {}", userSearchPreferenceId, e);
            throw ApplicationFailure.newFailure("User search preference not found", "NotFoundError");
        }
    }

    @Override
    public List<TeeTimeSlot> searchTeeTimes(UserSearchPreferenceDto userPreferences) {
        LOG.info("searching for tee times at facilities {} for user {}", userPreferences.searchCriteria().priorityCourseIds(), userPreferences.email());
        LOG.info("date: {}, time start {}, end {}, players: {}, lat/lon/radius: {}/{}/{}",
                 userPreferences.searchCriteria().searchDate(),
                 userPreferences.searchCriteria().preferredTimeStart(),
                 userPreferences.searchCriteria().preferredTimeEnd(),
                 userPreferences.searchCriteria().numberOfPlayers(),
                 userPreferences.searchCriteria().latitude(),
                 userPreferences.searchCriteria().longitude(),
                 userPreferences.searchCriteria().radiusMiles());

        try {
//            Map<String, Object> cookies = authService.getCookies();
            List<TeeTimeSlot> results = golfNowService.fetchTeeTimesForFacilities(Map.of(),
                                                                                  userPreferences.searchCriteria().priorityCourseIds(),
                                                                                  userPreferences.searchCriteria());
            return results;
        } catch (Exception e) {
            // TODO: should catch rate limit errors, bot scraping errors, and not retry in those cases.
            LOG.error("Error searching for tee times", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FacilitySummary> searchFacilities(UserPreferencesLegacy userPreferences) {

        try {
//            Map<String, Object> cookies = authService.getCookies();
            List<FacilitySummary> facilities = golfNowService.searchFacilities(
                    Map.of(),
                    userPreferences.searchCriteria());
            return facilities;
        } catch (Exception e) {
        // TODO: should catch rate limit errors, bot scraping errors, and not retry in those cases.
        LOG.error("Error searching for tee times", e);
        throw new RuntimeException(e);
        }
    }

    @Override
    public List<TeeTimeSlot> filterPreviousMatches(List<TeeTimeSlot> allResults, SearchCriteriaDbDto searchCriteria) {
        // 1. Get previous results from DB to filter out already-seen tee times
        LocalDateTime startTime = parseSearchDate(searchCriteria.searchDate())
                .atTime(searchCriteria.preferredTimeStart(), 0);
        LocalDateTime endTime = parseSearchDate(searchCriteria.searchDate())
                .atTime(searchCriteria.preferredTimeEnd(), 59);

        List<TeeTimeResultDbDto> previousResults = teeTimeResultService.getPreviousResults(
                searchCriteria.priorityCourseIds(),
                startTime,
                endTime
        );

        LOG.info("Found {} previous results in DB", previousResults.size());

        // 2. Filter to only new matches (tee times not previously seen)
        List<TeeTimeSlot> newMatches = filterNewMatches(allResults, previousResults);
        return newMatches;
    }

    @Override
    public void saveNewMatches(List<TeeTimeSlot> allResults) {
        // 3. Save all results to DB (new + update existing last_seen_at)
        teeTimeResultService.saveOrUpdateResults(allResults);
        LOG.info("Saved/updated {} tee time results to DB", allResults.size());
    }

    private List<TeeTimeSlot> filterNewMatches(List<TeeTimeSlot> newSlots,
                                               List<TeeTimeResultDbDto> previousResults) {
        // Create a set of (facilityId, teeTime truncated to minutes) for quick lookup
        Set<String> previousKeys = previousResults.stream()
                .map(prev -> {
                    LocalDateTime truncated = prev.teeTime().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
                    return prev.facilityId() + ":" + truncated;
                })
                .collect(Collectors.toSet());

        return newSlots.stream()
                .filter(slot -> {
                    LocalDateTime teeTime = slot.time().toLocalDateTime();
                    LocalDateTime truncated = teeTime.truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
                    String key = slot.facilityId() + ":" + truncated;
                    return !previousKeys.contains(key);
                })
                .toList();
    }

    private LocalDate parseSearchDate(String searchDate) {
        // Parse format like "Oct 11 2025" to LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);
        return LocalDate.parse(searchDate, formatter);
    }
}
