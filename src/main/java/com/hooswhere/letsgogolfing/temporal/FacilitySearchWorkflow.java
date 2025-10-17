package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.FacilitySummary;
import com.hooswhere.letsgogolfing.dto.UserPreferences;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface FacilitySearchWorkflow {
    @WorkflowMethod
    List<FacilitySummary> searchFacilities(UserPreferences userPreferences);
}
