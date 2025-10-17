package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ActivityImpl(taskQueues = "golfnow")
public class NotificationActivityImpl implements NotificationActivity {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationActivityImpl.class);

    @Override
    public void sendTeeTimeNotification(String email, List<TeeTimeSlot> teeTimes, int numPlayers) {
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
    }

    /**
     * Booking URL would look something like this:
     * /tee-times/facility/4817/tee-time/1867224724/checkout/players/4
     * @param slot
     * @return
     */
    private String generateBookingUrl(TeeTimeSlot slot, int numPlayers) {
        return String.format("https://www.golfnow.com%s/checkout/players/%d",
                slot.detailUrl(), numPlayers);
    }
}
