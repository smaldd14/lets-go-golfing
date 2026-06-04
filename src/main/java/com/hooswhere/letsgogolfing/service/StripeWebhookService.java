package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import com.hooswhere.letsgogolfing.entity.PriorityCourseEntity;
import com.hooswhere.letsgogolfing.entity.SearchCriteriaEntity;
import com.hooswhere.letsgogolfing.notification.email.EmailService;
import com.hooswhere.letsgogolfing.notification.email.EmailTemplate;
import com.hooswhere.letsgogolfing.notification.email.EmailTemplateContext;
import com.hooswhere.letsgogolfing.notification.email.EmailTemplateService;
import com.hooswhere.letsgogolfing.repository.SearchCriteriaRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StripeWebhookService {
    private static final Logger LOG = LoggerFactory.getLogger(StripeWebhookService.class);

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final UserSearchPreferenceService userSearchPreferenceService;
    private final SearchCriteriaRepository searchCriteriaRepository;
    private final MonitorCreationService monitorCreationService;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final SubscriptionService subscriptionService;
    private final ConnectEmailService connectEmailService;

    public StripeWebhookService(UserSearchPreferenceService userSearchPreferenceService,
                                SearchCriteriaRepository searchCriteriaRepository,
                                MonitorCreationService monitorCreationService,
                                EmailService emailService,
                                EmailTemplateService emailTemplateService,
                                SubscriptionService subscriptionService,
                                ConnectEmailService connectEmailService) {
        this.userSearchPreferenceService = userSearchPreferenceService;
        this.searchCriteriaRepository = searchCriteriaRepository;
        this.monitorCreationService = monitorCreationService;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
        this.subscriptionService = subscriptionService;
        this.connectEmailService = connectEmailService;
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

        // Subscription checkouts (MCP access) and one-time checkouts (web UI) both arrive here.
        if ("subscription".equals(session.getMode())) {
            processSubscriptionCheckout(session);
            return;
        }

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

        // Create the preference and start its monitoring schedule (shared with /api/monitors)
        UserSearchPreferenceDto userPref = monitorCreationService.createFromCriteriaId(
                email, searchCriteriaId, Duration.parse("PT5M"));

        LOG.info("Started schedule {} for user {}", userPref.scheduleId(), email);
        sendPaymentConfirmationEmail(email, searchCriteria);
    }

    /**
     * Handles a completed subscription checkout: records the subscription as active (storing the
     * checkout session id) and emails the user a link to the /connect page, where they issue their
     * MCP token and finish setup.
     */
    private void processSubscriptionCheckout(Session session) {
        String email = session.getCustomerDetails() != null
                ? session.getCustomerDetails().getEmail()
                : session.getCustomerEmail();
        String customerId = session.getCustomer();
        String subscriptionId = session.getSubscription();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email not found in subscription checkout session");
        }
        if (subscriptionId == null || subscriptionId.isBlank()) {
            throw new IllegalArgumentException("Subscription id not found in checkout session");
        }

        LOG.info("Processing subscription checkout for {} (subscription {})", email, subscriptionId);

        subscriptionService.upsertFromStripe(subscriptionId, customerId, session.getId(), email, "active", null, false);

        connectEmailService.sendConnectEmail(email, session.getId());
    }

    /**
     * Handles customer.subscription.created/updated/deleted. Looks up the row by subscription id
     * (no email on these events) and updates status / period end / cancellation.
     */
    @Transactional
    public void processSubscriptionEvent(Event event) {
        Subscription sub = (Subscription) event.getDataObjectDeserializer().getObject().orElseThrow(
                () -> new IllegalArgumentException("Could not deserialize subscription from webhook")
        );

        String status = "customer.subscription.deleted".equals(event.getType()) ? "canceled" : sub.getStatus();
        boolean cancelAtPeriodEnd = Boolean.TRUE.equals(sub.getCancelAtPeriodEnd());

        LOG.info("Processing {} for subscription {} (status {})", event.getType(), sub.getId(), status);

        subscriptionService.upsertFromStripe(
                sub.getId(),
                sub.getCustomer(),
                null,                 // no checkout session on subscription events; existing row keeps it
                null,                 // email not present on subscription events; existing row keeps it
                status,
                currentPeriodEnd(sub),
                cancelAtPeriodEnd);
    }

    private LocalDateTime currentPeriodEnd(Subscription sub) {
        if (sub.getItems() == null) {
            return null;
        }
        List<SubscriptionItem> items = sub.getItems().getData();
        if (items == null || items.isEmpty()) {
            return null;
        }
        Long epochSeconds = items.get(0).getCurrentPeriodEnd();
        return epochSeconds != null
                ? LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC)
                : null;
    }

    private void sendPaymentConfirmationEmail(String email, SearchCriteriaEntity criteria) {
        try {
            String timeWindow = formatHour(criteria.getPreferredTimeStart()) + " - " + formatHour(criteria.getPreferredTimeEnd());
            String holesDisplay = switch (criteria.getHoles()) {
                case 1 -> "9 holes";
                case 2 -> "18 holes";
                case 3 -> "9 or 18 holes";
                default -> criteria.getHoles() + " holes";
            };
            String maxPriceDisplay = criteria.getMaxPrice() != null ? "$" + criteria.getMaxPrice() : "No limit";
            String priorityCoursesDisplay = criteria.getPriorityCourses().isEmpty()
                    ? "None specified"
                    : criteria.getPriorityCourses().stream()
                            .map(pc -> String.valueOf(pc.getFacilityId()))
                            .collect(Collectors.joining(", "));

            EmailTemplateContext context = EmailTemplateContext.builder()
                    .put("searchDate", criteria.getSearchDate())
                    .put("numberOfPlayers", criteria.getNumberOfPlayers())
                    .put("timeWindow", timeWindow)
                    .put("holes", holesDisplay)
                    .put("radiusMiles", criteria.getRadiusMiles())
                    .put("maxPrice", maxPriceDisplay)
                    .put("priorityCourses", priorityCoursesDisplay)
                    .put("email", email)
                    .build();

            EmailTemplate rendered = emailTemplateService.renderFullTemplate("payment-confirmation", context);
            if (rendered == null) {
                LOG.warn("payment-confirmation template not found, skipping confirmation email for {}", email);
                return;
            }

            boolean sent = emailService.sendEmail(email, null, rendered.subject(), rendered.htmlBody(), rendered.textBody(),
                    Map.of("notification_type", "payment_confirmation"));

            if (sent) {
                LOG.info("Payment confirmation email sent to {}", email);
            } else {
                LOG.warn("Failed to send payment confirmation email to {}", email);
            }
        } catch (Exception e) {
            LOG.error("Error sending payment confirmation email to {}", email, e);
        }
    }

    private static String formatHour(int hour) {
        if (hour == 0 || hour == 24) return "12:00 AM";
        if (hour == 12) return "12:00 PM";
        return hour < 12 ? hour + ":00 AM" : (hour - 12) + ":00 PM";
    }
}
