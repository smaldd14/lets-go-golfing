package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.dto.SubscriptionStatusDto;
import com.hooswhere.letsgogolfing.entity.SubscriptionEntity;
import com.hooswhere.letsgogolfing.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionService.class);
    private static final Set<String> ACTIVE_STATUSES = Set.of("active", "trialing");

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public SubscriptionStatusDto getStatus(String email) {
        return mostRecent(email)
                .map(sub -> new SubscriptionStatusDto(
                        isActive(sub),
                        sub.getStatus(),
                        sub.getCurrentPeriodEnd() != null ? sub.getCurrentPeriodEnd().toInstant(ZoneOffset.UTC) : null))
                .orElse(new SubscriptionStatusDto(false, "none", null));
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(String email) {
        return mostRecent(email).map(this::isActive).orElse(false);
    }

    /**
     * Creates or updates a subscription row keyed by Stripe subscription ID.
     * When email is null (e.g. subscription.updated/deleted events that lack it), the
     * existing email is preserved.
     */
    @Transactional
    public void upsertFromStripe(String stripeSubscriptionId,
                                 String stripeCustomerId,
                                 String email,
                                 String status,
                                 LocalDateTime currentPeriodEnd,
                                 boolean cancelAtPeriodEnd) {
        Optional<SubscriptionEntity> existing = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);
        if (existing.isEmpty() && email == null) {
            // A subscription.updated/deleted event arrived before the checkout that creates the row.
            // We can't persist a row without an email (NOT NULL); the checkout event will create it.
            LOG.warn("Skipping subscription upsert for {} - no existing row and no email", stripeSubscriptionId);
            return;
        }

        SubscriptionEntity sub = existing.orElseGet(SubscriptionEntity::new);

        sub.setStripeSubscriptionId(stripeSubscriptionId);
        if (stripeCustomerId != null) {
            sub.setStripeCustomerId(stripeCustomerId);
        }
        if (email != null) {
            sub.setEmail(email);
        }
        sub.setStatus(status);
        if (currentPeriodEnd != null) {
            sub.setCurrentPeriodEnd(currentPeriodEnd);
        }
        sub.setCancelAtPeriodEnd(cancelAtPeriodEnd);

        subscriptionRepository.save(sub);
    }

    private Optional<SubscriptionEntity> mostRecent(String email) {
        return subscriptionRepository.findByEmailOrderByCreatedAtDesc(email).stream().findFirst();
    }

    private boolean isActive(SubscriptionEntity sub) {
        if (!ACTIVE_STATUSES.contains(sub.getStatus())) {
            return false;
        }
        return sub.getCurrentPeriodEnd() == null || sub.getCurrentPeriodEnd().isAfter(LocalDateTime.now());
    }
}
