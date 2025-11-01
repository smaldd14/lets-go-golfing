package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.dto.AuthTokens;
import com.hooswhere.letsgogolfing.dto.AuthenticationRequest;
import com.hooswhere.letsgogolfing.dto.FacilitySummary;
import com.hooswhere.letsgogolfing.dto.TTMonitorRequest;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.dto.UserPreferencesLegacy;
import com.hooswhere.letsgogolfing.temporal.FacilitySearchWorkflow;
import com.hooswhere.letsgogolfing.temporal.TeeTimeSearchWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GolfNowStarter {

    private final WorkflowClient client;

    public GolfNowStarter(WorkflowClient client) {
        this.client = client;
    }

    public List<FacilitySummary> startFacilitySearchWf(UserPreferencesLegacy userPreferences) {
        String wfId = UUID.randomUUID().toString();
        var stub = client.newWorkflowStub(FacilitySearchWorkflow.class,
                                          WorkflowOptions.newBuilder()
                                                  .setWorkflowId(wfId)
                                                  .setTaskQueue("golfnow")
                                                  .build());
        // synchronous call to the workflow method
        return stub.searchFacilities(userPreferences);
    }

    public List<TeeTimeSlot> startTTSearchWf(TTMonitorRequest ttMonitorRequest) {
        String wfId = UUID.randomUUID().toString();
        var stub = client.newWorkflowStub(TeeTimeSearchWorkflow.class,
                                          WorkflowOptions.newBuilder()
                                                  .setWorkflowId(wfId)
                                                  .setTaskQueue("golfnow")
                                                  .build());
        // synchronous call to the workflow method
        return stub.searchTeeTimes(ttMonitorRequest);
    }
}
