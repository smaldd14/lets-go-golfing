package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.TTMonitorRequest;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.dto.UserPreferencesLegacy;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;

@WorkflowImpl(taskQueues = "golfnow")
public class TeeTimeMonitorWorkflowImpl implements TeeTimeMonitorWorkflow {

    private final Logger logger;


    private final GolfNowActivities golfNowActivities = Workflow.newActivityStub(GolfNowActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(30))
                    // TODO: imiplement heartbeating thruout activities
                    .setHeartbeatTimeout(Duration.ofMinutes(5))
                    .setTaskQueue("golfnow")
                    .setRetryOptions(
                            RetryOptions.newBuilder()
                                    .setMaximumAttempts(3)
                                    .setBackoffCoefficient(2.0)
                                    .setInitialInterval(Duration.ofSeconds(10))
                                    .build()
                    )
                    .build());

    private final NotificationActivity notificationActivity = Workflow.newActivityStub(NotificationActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setTaskQueue("golfnow")
                    .setRetryOptions(
                            RetryOptions.newBuilder()
                                    .setInitialInterval(Duration.ofSeconds(5))
                                    .build()
                    )
                    .build());

    public TeeTimeMonitorWorkflowImpl() {
        this.logger = Workflow.getLogger(this.getClass());
    }

    @Override
    public List<TeeTimeSlot> monitorTeeTimes(TTMonitorRequest ttMonitorRequest) {
        logger.info("Starting tee time monitoring for user: {}", ttMonitorRequest.userSearchPreferenceId());

        // 1. get prefs
        UserSearchPreferenceDto userPrefs = golfNowActivities.loadUserSearchPreference(ttMonitorRequest.userSearchPreferenceId());
        // 2. Search for tee times from GolfNow API
        List<TeeTimeSlot> allResults = golfNowActivities.searchTeeTimes(userPrefs);
        logger.info("Found {} tee time slots from GolfNow", allResults.size());

        if (allResults.isEmpty()) {
            logger.info("No tee times found, skipping processing");
            return allResults;
        }

        // 3. Process results: get previous from DB, filter new matches, save to DB
        List<TeeTimeSlot> newMatches = golfNowActivities.filterPreviousMatches(allResults, userPrefs.searchCriteria());

        golfNowActivities.saveNewMatches(allResults);
        logger.info("Filtered to {} new tee time matches", newMatches.size());

        // 4. Send notification if we have new matches
        if (!newMatches.isEmpty()) {
            logger.info("Sending notification for {} new tee times to {}", newMatches.size(), userPrefs.email());
            notificationActivity.sendTeeTimeNotification(userPrefs.id(),
                                                         userPrefs.email(),
                                                         newMatches,
                                                         userPrefs.searchCriteria().numberOfPlayers());
        } else {
            logger.info("No new tee times found, skipping notification");
        }

        return newMatches;
    }
}
