package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.controller.LggException;
import com.hooswhere.letsgogolfing.dto.ScheduleRequest;
import com.hooswhere.letsgogolfing.dto.TTMonitorRequest;
import com.hooswhere.letsgogolfing.dto.UserPreferencesLegacy;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import com.hooswhere.letsgogolfing.temporal.TeeTimeMonitorWorkflow;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.Schedule;
import io.temporal.client.schedules.ScheduleActionStartWorkflow;
import io.temporal.client.schedules.ScheduleAlreadyRunningException;
import io.temporal.client.schedules.ScheduleClient;
import io.temporal.client.schedules.ScheduleException;
import io.temporal.client.schedules.ScheduleHandle;
import io.temporal.client.schedules.ScheduleIntervalSpec;
import io.temporal.client.schedules.ScheduleOptions;
import io.temporal.client.schedules.ScheduleSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class TeeTimeScheduleStarter {
    private static final Logger LOG = LoggerFactory.getLogger(TeeTimeScheduleStarter.class);
    private static final ZoneId EST_ZONE = ZoneId.of("America/New_York");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);
    private static final int GRACE_PERIOD_MINUTES = 15;

    private final ScheduleClient scheduleClient;

    public TeeTimeScheduleStarter(ScheduleClient scheduleClient) {
        this.scheduleClient = scheduleClient;
    }

    public Optional<String> createTeeTimeSearchSchedule(UserSearchPreferenceDto userPrefs) {
        // Use email as schedule ID to prevent duplicates
        String scheduleId = generateScheduleId(userPrefs.email());

        // Calculate schedule end time and validate it's not in the past
        Instant endTime = calculateScheduleEndTime(userPrefs.searchCriteria());
        if (endTime.isBefore(Instant.now())) {
            throw new LggException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot create schedule: the tee time window has already passed"
            );
        }

        Schedule schedule = Schedule.newBuilder()
                .setAction(
                        ScheduleActionStartWorkflow.newBuilder()
                                .setWorkflowType(TeeTimeMonitorWorkflow.class)
                                .setArguments(new TTMonitorRequest(userPrefs.id()))
                                .setOptions(
                                        WorkflowOptions.newBuilder()
                                                .setWorkflowId("ttmonitor-" + userPrefs.email())
                                                .setTaskQueue("golfnow")
                                                .build())
                                .build())
                .setSpec(
                        ScheduleSpec.newBuilder()
                            .setIntervals(List.of(
                                    new ScheduleIntervalSpec(userPrefs.scheduleInterval())
                            ))
                            .setEndAt(endTime)
                            .build()
                )
                .build();
        try {
            ScheduleHandle handle = scheduleClient.createSchedule(
                    scheduleId,
                    schedule,
                    ScheduleOptions.newBuilder()
                            .setTriggerImmediately(true) // run it right away to start
                            .build()
            );
        } catch (ScheduleAlreadyRunningException e) {
            LOG.info("Schedule already exists for {}", userPrefs.email());
            LOG.trace("", e);
            throw new LggException(HttpStatus.CONFLICT, "Schedule already exists for user: " + userPrefs.email(), e);
        }


        return Optional.of(scheduleId);
    }

    private boolean scheduleExists(String scheduleId) {
        return scheduleClient.listSchedules()
                .anyMatch(handle -> handle.getScheduleId().equals(scheduleId));

    }

    public ResponseEntity<Void> deleteTeeTimeSearchSchedule(String email) {
        String scheduleId = generateScheduleId(email);

        if (!scheduleExists(scheduleId)) {
            LOG.info("No schedule found for user: {}", email);
            throw new LggException(HttpStatus.NOT_FOUND, "Schedule not found for user: " + email);
        }

        try {
            ScheduleHandle handle = scheduleClient.getHandle(scheduleId);
            handle.delete();
            LOG.info("Successfully deleted schedule for user: {}", email);
            return ResponseEntity.noContent().build();
        } catch (ScheduleException e) {
            LOG.error("Failed to delete schedule for user: {}", email, e);
            throw new LggException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete schedule for user: " + email, e);
        }
    }

    private String generateScheduleId(String email) {
        // Use email as base for schedule ID to ensure one schedule per user
        return "tee-time-search-" + email;
    }

    private Instant calculateScheduleEndTime(SearchCriteriaDbDto searchCriteria) {
        // Parse the search date (format: "Oct 11 2025")
        LocalDateTime dateTime = LocalDateTime.parse(searchCriteria.searchDate(), DATE_FORMATTER)
                .atStartOfDay()
                .withHour(searchCriteria.preferredTimeEnd())
                .plusMinutes(GRACE_PERIOD_MINUTES);

        return dateTime.atZone(EST_ZONE).toInstant();
    }
}
