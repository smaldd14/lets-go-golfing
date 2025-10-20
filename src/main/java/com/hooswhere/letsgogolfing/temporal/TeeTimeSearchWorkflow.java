package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.TTMonitorRequest;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.dto.UserPreferencesLegacy;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface TeeTimeSearchWorkflow {
    @WorkflowMethod
    List<TeeTimeSlot> searchTeeTimes(TTMonitorRequest ttMonitorRequest);
}
