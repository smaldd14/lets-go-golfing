package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.dto.UserPreferences;
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
    public List<TeeTimeSlot> monitorTeeTimes(UserPreferences userPreferences) {
        logger.info("Starting tee time monitoring for user: {}", userPreferences.email());

        // Search for tee times
        List<TeeTimeSlot> results = golfNowActivities.searchTeeTimes(userPreferences);
        logger.info("Found {} tee time slots", results.size());

        // TODO: Get previous results from DB and filter for new matches only
        // For now, we'll notify about all results
        // Future implementation:
        // 1. Query DB for previous tee times matching this search criteria
        // 2. Filter results to only include new/unseen tee times
        // 3. Save new results to DB
        // 4. Track notification in user_notifications table

        // Send notification if we have results
        if (!results.isEmpty()) {
            logger.info("Sending notification for {} tee times to {}", results.size(), userPreferences.email());
            notificationActivity.sendTeeTimeNotification(userPreferences.email(), results, userPreferences.searchCriteria().numberOfPlayers());
        } else {
            logger.info("No tee times found, skipping notification");
        }

        return results;
    }
}
