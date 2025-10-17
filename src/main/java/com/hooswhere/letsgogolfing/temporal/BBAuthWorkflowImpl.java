package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.AuthTokens;
import com.hooswhere.letsgogolfing.dto.AuthenticationRequest;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@WorkflowImpl(taskQueues = "golfnow")
public class BBAuthWorkflowImpl implements BBAuthWorkflow {

    private final BBAuthActivity authActivity = Workflow.newActivityStub(BBAuthActivity.class,
                                                     ActivityOptions.newBuilder()
//                                                             .setHeartbeatTimeout(Duration.ofSeconds(10))
                                                             .setStartToCloseTimeout(Duration.ofMinutes(5))
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
    public AuthTokens authenticate(AuthenticationRequest request) {

        try {
            return authActivity.authenticate();
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed", e);
        }
    }
}
