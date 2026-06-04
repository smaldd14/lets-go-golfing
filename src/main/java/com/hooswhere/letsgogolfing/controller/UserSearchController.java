package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.CreateUserSearchRequest;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import com.hooswhere.letsgogolfing.service.TeeTimeScheduleStarter;
import com.hooswhere.letsgogolfing.service.UserSearchPreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
public class UserSearchController implements UserSearchApi {

    private static final Logger LOG = LoggerFactory.getLogger(UserSearchController.class);

    private final UserSearchPreferenceService userSearchPreferenceService;
    private final TeeTimeScheduleStarter scheduleStarter;

    public UserSearchController(UserSearchPreferenceService userSearchPreferenceService,
                                TeeTimeScheduleStarter scheduleStarter) {
        this.userSearchPreferenceService = userSearchPreferenceService;
        this.scheduleStarter = scheduleStarter;
    }

    @Override
    public UserSearchPreferenceDto createSearch(CreateUserSearchRequest request) {
        try {
            return userSearchPreferenceService.createPreference(
                    request.email(),
                    request.searchCriteria(),
                    request.paymentEnabled(),
                    request.notifyEnabled(),
                    request.scheduleInterval()
            );
        } catch (Exception e) {
            throw new LggException(HttpStatus.BAD_REQUEST ,"Failed to create user search preference", e);
        }

    }

    @Override
    public List<UserSearchPreferenceDto> getUserSearches(String email) {
        return userSearchPreferenceService.getActivePreferences(email);
    }

    @Override
    public UserSearchPreferenceDto getSearch(UUID id) {
        return userSearchPreferenceService.getPreference(id);
    }

    @Override
    public void deleteSearch(UUID id) {
        // Stop the Temporal schedule (keyed by email) before deactivating the preference.
        UserSearchPreferenceDto pref = userSearchPreferenceService.getPreference(id);
        try {
            scheduleStarter.deleteTeeTimeSearchSchedule(pref.email());
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw e;
            }
            LOG.info("No schedule to delete for {} when cancelling monitor {}", pref.email(), id);
        }
        userSearchPreferenceService.deactivatePreference(id);
    }
}
