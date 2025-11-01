package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.service.StripeWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StripeWebhookController implements StripeWebhookApi {
    private static final Logger LOG = LoggerFactory.getLogger(StripeWebhookController.class);
    private static final String CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";

    private final StripeWebhookService webhookService;

    public StripeWebhookController(StripeWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @Override
    public ResponseEntity<Void> handleWebhook(String payload, String stripeSignature) {
        try {
            // Verify signature and parse event
            Event event = webhookService.verifyAndParseWebhook(payload, stripeSignature);

            LOG.info("Received Stripe webhook event: {}", event.getType());

            // Handle checkout.session.completed event
            if (CHECKOUT_SESSION_COMPLETED.equals(event.getType())) {
                webhookService.processCheckoutCompleted(event);
                LOG.info("Successfully processed checkout completion");
            } else {
                LOG.info("Ignoring webhook event type: {}", event.getType());
            }

            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            LOG.error("Invalid Stripe signature", e);
            return ResponseEntity.badRequest().build();

        } catch (IllegalArgumentException e) {
            LOG.error("Invalid webhook data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            LOG.error("Failed to process webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
