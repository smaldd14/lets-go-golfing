package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.browserbase.AuthService;
import com.hooswhere.letsgogolfing.dto.FacilitySummary;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.dto.UserPreferences;
import com.hooswhere.letsgogolfing.golfnow.GolfNowService;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ActivityImpl(taskQueues = "golfnow")
public class GolfNowActivitiesImpl implements GolfNowActivities {
    private static final Logger LOG = LoggerFactory.getLogger(GolfNowActivitiesImpl.class);

    private final GolfNowService golfNowService;
    private final AuthService authService;

    public GolfNowActivitiesImpl(GolfNowService golfNowService, AuthService authService) {
        this.golfNowService = golfNowService;
        this.authService = authService;
    }

    @Override
    public List<TeeTimeSlot> searchTeeTimes(UserPreferences userPreferences) {
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
    public List<FacilitySummary> searchFacilities(UserPreferences userPreferences) {

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
}
