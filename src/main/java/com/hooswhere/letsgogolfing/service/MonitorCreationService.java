package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.dto.SearchCriteria;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Shared "create a search preference and start its monitoring schedule" logic, used by both the
 * Stripe webhook (one-time checkout) and the subscription-gated /api/monitors endpoint.
 */
@Service
public class MonitorCreationService {

    private final UserSearchPreferenceService userSearchPreferenceService;
    private final TeeTimeScheduleStarter scheduleStarter;

    public MonitorCreationService(UserSearchPreferenceService userSearchPreferenceService,
                                  TeeTimeScheduleStarter scheduleStarter) {
        this.userSearchPreferenceService = userSearchPreferenceService;
        this.scheduleStarter = scheduleStarter;
    }

    @Transactional
    public UserSearchPreferenceDto createFromCriteria(String email, SearchCriteria criteria, Duration interval) {
        UserSearchPreferenceDto pref = userSearchPreferenceService.createPreference(email, criteria, false, true, interval);
        return startSchedule(pref);
    }

    @Transactional
    public UserSearchPreferenceDto createFromCriteriaId(String email, UUID searchCriteriaId, Duration interval) {
        UserSearchPreferenceDto pref = userSearchPreferenceService.createPreferenceFromCriteriaId(
                email, searchCriteriaId, false, true, interval);
        return startSchedule(pref);
    }

    private UserSearchPreferenceDto startSchedule(UserSearchPreferenceDto pref) {
        Optional<String> scheduleId = scheduleStarter.createTeeTimeSearchSchedule(pref);
        scheduleId.ifPresent(id -> userSearchPreferenceService.updateScheduleId(pref.id(), id));
        return userSearchPreferenceService.getPreference(pref.id());
    }
}
