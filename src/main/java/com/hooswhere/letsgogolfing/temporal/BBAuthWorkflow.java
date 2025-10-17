package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.AuthTokens;
import com.hooswhere.letsgogolfing.dto.AuthenticationRequest;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface BBAuthWorkflow {
    @WorkflowMethod
    public AuthTokens authenticate(AuthenticationRequest request);
}
