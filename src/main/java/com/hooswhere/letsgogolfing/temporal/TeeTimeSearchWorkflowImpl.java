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
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

@WorkflowImpl(taskQueues = "golfnow")
public class TeeTimeSearchWorkflowImpl implements TeeTimeSearchWorkflow {
    private static final Logger LOG = LoggerFactory.getLogger(TeeTimeSearchWorkflowImpl.class);

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
    public List<TeeTimeSlot> searchTeeTimes(TTMonitorRequest ttRequest) {
        UserSearchPreferenceDto userPrefs = golfNowActivities.loadUserSearchPreference(ttRequest.userSearchPreferenceId());
        return golfNowActivities.searchTeeTimes(userPrefs);
    }
}
