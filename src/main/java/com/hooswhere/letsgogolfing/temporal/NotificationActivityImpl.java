package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.TeeTimeResultDbDto;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.notification.email.EmailService;
import com.hooswhere.letsgogolfing.notification.email.EmailTemplate;
import com.hooswhere.letsgogolfing.notification.email.EmailTemplateContext;
import com.hooswhere.letsgogolfing.notification.email.EmailTemplateService;
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
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    public NotificationActivityImpl(UserNotificationService userNotificationService,
                                   TeeTimeResultService teeTimeResultService,
                                   EmailService emailService,
                                   EmailTemplateService emailTemplateService) {
        this.userNotificationService = userNotificationService;
        this.teeTimeResultService = teeTimeResultService;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
    }

    @Override
    public void sendTeeTimeNotification(UUID userSearchPreferenceId, String email, List<TeeTimeSlot> teeTimes, int numPlayers) {
        LOG.info("Sending tee time notification to {} for {} tee times", email, teeTimes.size());

        // Build tee times HTML list
        StringBuilder teeTimesHtml = new StringBuilder();
        StringBuilder teeTimesText = new StringBuilder();

        for (int i = 0; i < teeTimes.size(); i++) {
            TeeTimeSlot slot = teeTimes.get(i);
            String bookingUrl = generateBookingUrl(slot, numPlayers);
            LocalDateTime slotDate = slot.time().toLocalDateTime();
            // HTML version
            teeTimesHtml.append(String.format("""
                <div class="tee-time">
                    <h2>%s</h2>
                    <div class="detail"><strong>Time:</strong> %s</div>
                    <div class="detail"><strong>Date:</strong> %s</div>
                    <div class="detail"><strong>Players:</strong> %d</div>
                    <div class="price">$%.2f</div>
                    <a href="%s" class="book-button">Book Now →</a>
                </div>
                """,
                escapeHtml(slot.facility().name()),
                slot.time().formatted() + slot.time().formattedTimeMeridian(),
                slotDate.toLocalDate(),
                numPlayers,
                slot.displayRate().value(),
                bookingUrl
            ));

            // Text version
            teeTimesText.append(String.format("""
                %d. %s
                   Time: %s
                   Date: %s
                   Players: %d
                   Price: $%.2f
                   Book: %s

                """,
                i + 1,
                slot.facility().name(),
                slot.formattedTime(),
                slot.time().date(),
                numPlayers,
                slot.displayRate().value(),
                bookingUrl
            ));
        }

        // Build template context
        EmailTemplateContext context = EmailTemplateContext.builder()
                .put("teeTimeCount", teeTimes.size())
                .put("pluralSuffix", teeTimes.size() > 1 ? "s" : "")
                .put("teeTimesList", teeTimesHtml.toString())
                .put("teeTimesListText", teeTimesText.toString())
                .put("unsubscribeUrl", "https://letsgogolfing.com/unsubscribe") // TODO: actual unsubscribe URL
                .build();

        // Render email template
        EmailTemplate renderedTemplate = emailTemplateService.renderFullTemplate("tee-time-notification", context);

        if (renderedTemplate == null) {
            LOG.error("Failed to render email template for tee time notification");
            return;
        }

        // Send email via AWS SES
        boolean sent = emailService.sendEmail(
                email,
                null,
                renderedTemplate.subject(),
                renderedTemplate.htmlBody(),
                renderedTemplate.textBody(),
                java.util.Map.of(
                        "notification_type", "tee_time",
                        "tee_time_count", String.valueOf(teeTimes.size())
                )
        );

        if (sent) {
            LOG.info("Successfully sent email notification to {}", email);
        } else {
            LOG.error("Failed to send email notification to {}", email);
        }

        // Record notifications in DB
        List<UUID> teeTimeResultIds = teeTimes.stream()
                .map(slot -> {
                    LocalDateTime teeTime = slot.time().toLocalDateTime();
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

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
