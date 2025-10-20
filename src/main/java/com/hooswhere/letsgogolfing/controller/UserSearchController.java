package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.CreateUserSearchRequest;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import com.hooswhere.letsgogolfing.service.UserSearchPreferenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class UserSearchController implements UserSearchApi {

    private final UserSearchPreferenceService userSearchPreferenceService;

    public UserSearchController(UserSearchPreferenceService userSearchPreferenceService) {
        this.userSearchPreferenceService = userSearchPreferenceService;
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
        userSearchPreferenceService.deactivatePreference(id);
    }
}
