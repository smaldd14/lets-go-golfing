package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.ScheduleRequest;
import com.hooswhere.letsgogolfing.service.TeeTimeScheduleStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TeeTimeScheduleController implements TeeTimeScheduleApi {
    private static final Logger LOG = LoggerFactory.getLogger(TeeTimeScheduleController.class);
    private final TeeTimeScheduleStarter scheduleStarter;

    public TeeTimeScheduleController(TeeTimeScheduleStarter scheduleStarter) {
        this.scheduleStarter = scheduleStarter;
    }

    @Override
    public ResponseEntity<String> createSchedule(ScheduleRequest scheduleRequest) {
        LOG.info("Creating tee time search schedule for user: {}, interval: {}",
                scheduleRequest.email(), scheduleRequest.interval());

        return scheduleStarter.createTeeTimeSearchSchedule(scheduleRequest);
    }

    @Override
    public ResponseEntity<Void> deleteSchedule(String email) throws Exception {
        LOG.info("Deleting tee time search schedule for user: {}", email);
        return scheduleStarter.deleteTeeTimeSearchSchedule(email);
    }
}
