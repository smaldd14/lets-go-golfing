package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.dto.UserPreferences;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface TeeTimeSearchWorkflow {
    @WorkflowMethod
    List<TeeTimeSlot> searchTeeTimes(UserPreferences userPreferences);
}
