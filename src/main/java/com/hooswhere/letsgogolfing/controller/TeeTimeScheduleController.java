package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.ScheduleRequest;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import com.hooswhere.letsgogolfing.service.TeeTimeScheduleStarter;
import com.hooswhere.letsgogolfing.service.UserSearchPreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
public class TeeTimeScheduleController implements TeeTimeScheduleApi {
    private static final Logger LOG = LoggerFactory.getLogger(TeeTimeScheduleController.class);
    private final TeeTimeScheduleStarter scheduleStarter;
    private final UserSearchPreferenceService userPrefsService;

    public TeeTimeScheduleController(TeeTimeScheduleStarter scheduleStarter,
                                     UserSearchPreferenceService userPrefsService) {
        this.scheduleStarter = scheduleStarter;
        this.userPrefsService = userPrefsService;
    }

    @Override
    public ResponseEntity<String> createSchedule(ScheduleRequest scheduleRequest) {
        LOG.info("Creating tee time search schedule for user: {}z",
                scheduleRequest.email());

        List<UserSearchPreferenceDto> activePrefs = userPrefsService.getActivePreferences(scheduleRequest.email());
        // TODO: kick off all schedules here?
        UserSearchPreferenceDto userSearchPreferenceDto = activePrefs.stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No active search preferences found for user: " + scheduleRequest.email()));
        Optional<String> scheduleId = scheduleStarter.createTeeTimeSearchSchedule(userSearchPreferenceDto);
        return ResponseEntity.of(scheduleId);
    }

    @Override
    public ResponseEntity<Void> deleteSchedule(String email) throws Exception {
        LOG.info("Deleting tee time search schedule for user: {}", email);
        return scheduleStarter.deleteTeeTimeSearchSchedule(email);
    }
}
