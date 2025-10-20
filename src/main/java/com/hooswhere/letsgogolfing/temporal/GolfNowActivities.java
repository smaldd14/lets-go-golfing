package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.FacilitySummary;
import com.hooswhere.letsgogolfing.dto.SearchCriteria;
import com.hooswhere.letsgogolfing.dto.SearchCriteriaDbDto;
import com.hooswhere.letsgogolfing.dto.TTMonitorRequest;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.dto.UserPreferencesLegacy;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import io.temporal.activity.ActivityInterface;

import java.util.List;
import java.util.UUID;

@ActivityInterface
public interface GolfNowActivities {
    UserSearchPreferenceDto loadUserSearchPreference(UUID userSearchPreferenceId);

    List<TeeTimeSlot> searchTeeTimes(UserSearchPreferenceDto userPreferences);

    List<FacilitySummary> searchFacilities(UserPreferencesLegacy userPreferences);

    /**
     * Processes tee time results: queries DB for previous results, filters to new matches,
     * and saves all results to DB. Returns only the new matches.
     */
    List<TeeTimeSlot> filterPreviousMatches(List<TeeTimeSlot> allResults, SearchCriteriaDbDto searchCriteria);

    void saveNewMatches(List<TeeTimeSlot> allResults);
}
