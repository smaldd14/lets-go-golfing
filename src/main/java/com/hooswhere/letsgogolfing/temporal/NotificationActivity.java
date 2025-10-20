package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import io.temporal.activity.ActivityInterface;

import java.util.List;
import java.util.UUID;

@ActivityInterface
public interface NotificationActivity {
    void sendTeeTimeNotification(UUID userSearchPreferenceId, String email, List<TeeTimeSlot> teeTimes, int numPlayers);
}
