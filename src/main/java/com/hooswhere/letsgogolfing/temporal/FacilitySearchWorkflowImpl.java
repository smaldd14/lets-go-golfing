package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.FacilitySummary;
import com.hooswhere.letsgogolfing.dto.UserPreferencesLegacy;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.List;

@WorkflowImpl(taskQueues = "golfnow")
public class FacilitySearchWorkflowImpl implements FacilitySearchWorkflow {

    private final GolfNowActivities golfNowActivities = Workflow.newActivityStub(GolfNowActivities.class,
         ActivityOptions.newBuilder()
    //                                                             .setHeartbeatTimeout(Duration.ofSeconds(10))
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
    @Override
    public List<FacilitySummary> searchFacilities(UserPreferencesLegacy userPreferences) {
        return golfNowActivities.searchFacilities(userPreferences);
    }
}
