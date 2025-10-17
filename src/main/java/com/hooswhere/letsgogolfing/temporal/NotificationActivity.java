package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import io.temporal.activity.ActivityInterface;

import java.util.List;

@ActivityInterface
public interface NotificationActivity {
    void sendTeeTimeNotification(String email, List<TeeTimeSlot> teeTimes, int numPlayers);
}
