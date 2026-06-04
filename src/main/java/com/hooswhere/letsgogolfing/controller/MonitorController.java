package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.CreateMonitorRequest;
import com.hooswhere.letsgogolfing.dto.SearchCriteria;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import com.hooswhere.letsgogolfing.service.MonitorCreationService;
import com.hooswhere.letsgogolfing.service.SubscriptionService;
import com.hooswhere.letsgogolfing.service.TeeTimeScheduleStarter;
import com.hooswhere.letsgogolfing.service.UserSearchPreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

@RestController
public class MonitorController implements MonitorApi {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorController.class);
    private static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(5);

    private final SubscriptionService subscriptionService;
    private final UserSearchPreferenceService userSearchPreferenceService;
    private final MonitorCreationService monitorCreationService;
    private final TeeTimeScheduleStarter scheduleStarter;

    public MonitorController(SubscriptionService subscriptionService,
                             UserSearchPreferenceService userSearchPreferenceService,
                             MonitorCreationService monitorCreationService,
                             TeeTimeScheduleStarter scheduleStarter) {
        this.subscriptionService = subscriptionService;
        this.userSearchPreferenceService = userSearchPreferenceService;
        this.monitorCreationService = monitorCreationService;
        this.scheduleStarter = scheduleStarter;
    }

    @Override
    @Transactional
    public ResponseEntity<UserSearchPreferenceDto> createMonitor(CreateMonitorRequest request, boolean replace) {
        String email = request.email();
        if (email == null || email.isBlank()) {
            throw new LggException(HttpStatus.BAD_REQUEST, "email is required");
        }
        if (request.searchCriteria() == null) {
            throw new LggException(HttpStatus.BAD_REQUEST, "searchCriteria is required");
        }

        if (!subscriptionService.hasActiveSubscription(email)) {
            throw new LggException(HttpStatus.PAYMENT_REQUIRED, "no_active_subscription");
        }

        // One active monitor per user. Replace tears down the old one first.
        List<UserSearchPreferenceDto> existing = userSearchPreferenceService.getActivePreferences(email);
        if (!existing.isEmpty()) {
            if (!replace) {
                throw new LggException(HttpStatus.CONFLICT, "monitor_exists");
            }
            replaceExisting(email, existing);
        }

        UserSearchPreferenceDto pref = monitorCreationService.createFromCriteria(
                email, request.searchCriteria(), intervalFor(request.searchCriteria()));

        return ResponseEntity.status(HttpStatus.CREATED).body(pref);
    }

    private void replaceExisting(String email, List<UserSearchPreferenceDto> existing) {
        // The schedule is keyed by email, so deleting once is enough. It may already be gone
        // (e.g. expired), so a NOT_FOUND here is fine.
        try {
            scheduleStarter.deleteTeeTimeSearchSchedule(email);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw e;
            }
            LOG.info("No existing schedule to delete for {} during replace", email);
        }
        existing.forEach(pref -> userSearchPreferenceService.deactivatePreference(pref.id()));
    }

    private Duration intervalFor(SearchCriteria criteria) {
        return criteria.checkIntervalMinutes() > 0
                ? Duration.ofMinutes(criteria.checkIntervalMinutes())
                : DEFAULT_INTERVAL;
    }
}
