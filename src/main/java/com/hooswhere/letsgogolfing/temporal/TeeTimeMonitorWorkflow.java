package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.TTMonitorRequest;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;
import java.util.UUID;

@WorkflowInterface
public interface TeeTimeMonitorWorkflow {
    @WorkflowMethod
    List<TeeTimeSlot> monitorTeeTimes(TTMonitorRequest request);
}
