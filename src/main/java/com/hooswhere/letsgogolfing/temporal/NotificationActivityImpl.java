package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.TeeTimeResultDbDto;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.service.TeeTimeResultService;
import com.hooswhere.letsgogolfing.service.UserNotificationService;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.hooswhere.letsgogolfing.golfnow.GolfNowUtil.generateBookingUrl;

@Component
@ActivityImpl(taskQueues = "golfnow")
public class NotificationActivityImpl implements NotificationActivity {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationActivityImpl.class);

    private final UserNotificationService userNotificationService;
    private final TeeTimeResultService teeTimeResultService;

    public NotificationActivityImpl(UserNotificationService userNotificationService,
                                   TeeTimeResultService teeTimeResultService) {
        this.userNotificationService = userNotificationService;
        this.teeTimeResultService = teeTimeResultService;
    }

    @Override
    public void sendTeeTimeNotification(UUID userSearchPreferenceId, String email, List<TeeTimeSlot> teeTimes, int numPlayers) {
        LOG.info("=== NOTIFICATION ===");
        LOG.info("To: {}", email);
        LOG.info("Subject: {} New Tee Times Available!", teeTimes.size());
        LOG.info("Message:");
        LOG.info("We found {} tee time(s) matching your search criteria:", teeTimes.size());
        LOG.info("");

        for (int i = 0; i < teeTimes.size(); i++) {
            TeeTimeSlot slot = teeTimes.get(i);
            LOG.info("{}. {} at {}", i + 1, slot.facility().name(), slot.formattedTime());
            LOG.info("   Price: ${}", slot.displayRate());
            LOG.info("   Booking URL: {}", generateBookingUrl(slot, numPlayers));
            LOG.info("");
        }

        LOG.info("=== END NOTIFICATION ===");

        // TODO: Replace with actual email sending via AWS SES
        // EmailService.send(email, subject, body);

        // Record notifications in DB
        List<UUID> teeTimeResultIds = teeTimes.stream()
                .map(slot -> {
                    LocalDateTime teeTime = LocalDateTime.parse(slot.time());
                    // Find the tee time result ID from DB
                    return teeTimeResultService.getPreviousResults(
                            List.of(slot.facilityId()),
                            teeTime,
                            teeTime
                    ).stream()
                    .filter(result -> result.facilityId() == slot.facilityId() &&
                                     result.teeTime().equals(teeTime))
                    .findFirst()
                    .map(TeeTimeResultDbDto::id)
                    .orElse(null);
                })
                .filter(id -> id != null)
                .toList();

        if (!teeTimeResultIds.isEmpty()) {
            userNotificationService.recordNotifications(userSearchPreferenceId, teeTimeResultIds);
            LOG.info("Recorded {} notifications in DB for user preference {}", teeTimeResultIds.size(), userSearchPreferenceId);
        }
    }
}
