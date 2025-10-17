package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.FacilitySummary;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.dto.UserPreferences;
import io.temporal.activity.ActivityInterface;

import java.util.List;

@ActivityInterface
public interface GolfNowActivities {
    List<TeeTimeSlot> searchTeeTimes(UserPreferences userPreferences);

    List<FacilitySummary> searchFacilities(UserPreferences userPreferences);
}
