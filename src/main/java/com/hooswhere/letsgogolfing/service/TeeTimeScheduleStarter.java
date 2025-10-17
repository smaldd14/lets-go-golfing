package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.controller.LggException;
import com.hooswhere.letsgogolfing.dto.ScheduleRequest;
import com.hooswhere.letsgogolfing.dto.UserPreferences;
import com.hooswhere.letsgogolfing.temporal.TeeTimeMonitorWorkflow;
import io.temporal.client.WorkflowClient;
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

import java.util.List;
import java.util.Optional;

@Service
public class TeeTimeScheduleStarter {
    private static final Logger LOG = LoggerFactory.getLogger(TeeTimeScheduleStarter.class);
    private final ScheduleClient scheduleClient;

    public TeeTimeScheduleStarter(ScheduleClient scheduleClient) {
        this.scheduleClient = scheduleClient;
    }

    public ResponseEntity<String> createTeeTimeSearchSchedule(ScheduleRequest scheduleRequest) {
        // Use email as schedule ID to prevent duplicates
        String scheduleId = generateScheduleId(scheduleRequest.email());

        UserPreferences userPreferences = new UserPreferences(
                scheduleRequest.email(),
                scheduleRequest.paymentEnabled(),
                scheduleRequest.searchCriteria()
        );

        Schedule schedule = Schedule.newBuilder()
                .setAction(
                        ScheduleActionStartWorkflow.newBuilder()
                                .setWorkflowType(TeeTimeMonitorWorkflow.class)
                                .setArguments(userPreferences)
                                .setOptions(
                                        WorkflowOptions.newBuilder()
                                                .setWorkflowId("ttmonitor-" + scheduleRequest.email())
                                                .setTaskQueue("golfnow")
                                                .build())
                                .build())
                .setSpec(
                        ScheduleSpec.newBuilder()
                            .setIntervals(List.of(
                                    new ScheduleIntervalSpec(scheduleRequest.interval())
                            ))
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
            LOG.info("Schedule already exists for {}", scheduleRequest.email());
            LOG.trace("", e);
            throw new LggException(HttpStatus.CONFLICT, "Schedule already exists for user: " + scheduleRequest.email(), e);
        }


        return ResponseEntity.of(Optional.of(scheduleId));
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
}
