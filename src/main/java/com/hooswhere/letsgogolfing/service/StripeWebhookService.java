package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import com.hooswhere.letsgogolfing.repository.SearchCriteriaRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
public class StripeWebhookService {
    private static final Logger LOG = LoggerFactory.getLogger(StripeWebhookService.class);

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final UserSearchPreferenceService userSearchPreferenceService;
    private final SearchCriteriaRepository searchCriteriaRepository;
    private final TeeTimeScheduleStarter scheduleStarter;

    public StripeWebhookService(UserSearchPreferenceService userSearchPreferenceService,
                                SearchCriteriaRepository searchCriteriaRepository,
                                TeeTimeScheduleStarter scheduleStarter) {
        this.userSearchPreferenceService = userSearchPreferenceService;
        this.searchCriteriaRepository = searchCriteriaRepository;
        this.scheduleStarter = scheduleStarter;
    }

    /**
     * Verifies Stripe webhook signature and returns the parsed event.
     * Throws SignatureVerificationException if signature is invalid.
     */
    public Event verifyAndParseWebhook(String payload, String signatureHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, signatureHeader, webhookSecret);
    }

    /**
     * Processes checkout.session.completed event.
     * Creates user search preference and starts monitoring schedule.
     */
    @Transactional
    public void processCheckoutCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow(
                () -> new IllegalArgumentException("Could not deserialize checkout session from webhook")
        );

        // Extract data from session
        String email = session.getCustomerDetails().getEmail();
        String searchCriteriaIdStr = session.getMetadata().get("searchCriteriaId");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email not found in checkout session");
        }

        if (searchCriteriaIdStr == null || searchCriteriaIdStr.isBlank()) {
            throw new IllegalArgumentException("searchCriteriaId not found in session metadata");
        }

        UUID searchCriteriaId = UUID.fromString(searchCriteriaIdStr);

        LOG.info("Processing checkout completion for email: {}, searchCriteriaId: {}", email, searchCriteriaId);

        // Verify search criteria exists
        var searchCriteria = searchCriteriaRepository.findById(searchCriteriaId)
                .orElseThrow(() -> new IllegalArgumentException("Search criteria not found: " + searchCriteriaId));

        // Check if preference already exists (idempotency)
        var existingPrefs = userSearchPreferenceService.getActivePreferences(email);
        boolean alreadyExists = existingPrefs.stream()
                .anyMatch(pref -> pref.searchCriteria().id().equals(searchCriteriaId));

        if (alreadyExists) {
            LOG.info("User search preference already exists for email: {} and searchCriteriaId: {}",
                    email, searchCriteriaId);
            return;
        }

        // Create user search preference
        UserSearchPreferenceDto userPref = userSearchPreferenceService.createPreferenceFromCriteriaId(
                email,
                searchCriteriaId,
                false,  // payment_enabled = false (determines if workflow auto-pays on GolfNow)
                true,   // notify_enabled = true
                Duration.parse("PT5M")  // schedule_interval = 5 minutes
        );

        LOG.info("Created user search preference with ID: {}", userPref.id());

        // Start Temporal schedule
        var scheduleResponse = scheduleStarter.createTeeTimeSearchSchedule(userPref);

        if (scheduleResponse.isPresent()) {
            String scheduleId = scheduleResponse.get();
            userSearchPreferenceService.updateScheduleId(userPref.id(), scheduleId);
            LOG.info("Successfully started schedule {} for user {}", scheduleId, email);
        } else {
            LOG.error("Failed to create schedule for user {}", email);
            throw new RuntimeException("Failed to create monitoring schedule");
        }
    }
}
