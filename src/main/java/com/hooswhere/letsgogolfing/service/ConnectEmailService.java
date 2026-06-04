package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.notification.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Sends the post-subscription "set up your access" email. The email links to the frontend
 * /connect page (carrying the Stripe checkout session id) rather than embedding a raw token:
 * the connect page is the single source of truth and issues the token on visit.
 */
@Service
public class ConnectEmailService {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectEmailService.class);

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final EmailService emailService;

    public ConnectEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendConnectEmail(String email, String checkoutSessionId) {
        String connectUrl = frontendUrl + "/connect?session_id="
                + URLEncoder.encode(checkoutSessionId, StandardCharsets.UTF_8);
        String subject = "Set up your tee-time monitor access";
        String textBody = """
                You're all set! Your subscription is active.

                Finish connecting your AI assistant here:
                %s

                This page shows your personal access token and copy-paste setup steps for
                Claude Desktop, Claude Code, and the Claude mobile/web app. You can also manage or
                cancel your subscription from there.

                Keep this link private - it's tied to your account.
                """.formatted(connectUrl);

        boolean sent = emailService.sendEmail(email, subject, null, textBody);
        if (sent) {
            LOG.info("Connect email sent to {}", email);
        } else {
            LOG.warn("Failed to send connect email to {}", email);
        }
    }
}
