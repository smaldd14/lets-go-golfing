package com.hooswhere.letsgogolfing.notification.email;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import java.util.Map;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final SesV2Client sesV2Client;
    private final AwsSesProps awsSesProps;

    public EmailService(SesV2Client sesV2Client, AwsSesProps sesConfig) {
        this.sesV2Client = sesV2Client;
        this.awsSesProps = sesConfig;
    }

    public boolean sendEmail(String toEmail, String subject, String htmlBody, String textBody) {
        return sendEmail(toEmail, null, subject, htmlBody, textBody, Map.of());
    }

    public boolean sendEmail(String toEmail, String toName, String subject, String htmlBody, String textBody, Map<String, String> tags) {
        try {
            // Build the destination
            Destination.Builder destinationBuilder = Destination.builder()
                    .toAddresses(formatEmailAddress(toEmail, toName));

            // Build the email content
            Body.Builder bodyBuilder = Body.builder();

            if (htmlBody != null && !htmlBody.isEmpty()) {
                bodyBuilder.html(Content.builder()
                                         .charset("UTF-8")
                                         .data(htmlBody)
                                         .build());
            }

            if (textBody != null && !textBody.isEmpty()) {
                bodyBuilder.text(Content.builder()
                                         .charset("UTF-8")
                                         .data(textBody)
                                         .build());
            }

            // Build the message
            Message message = Message.builder()
                    .subject(Content.builder()
                                     .charset("UTF-8")
                                     .data(subject)
                                     .build())
                    .body(bodyBuilder.build())
                    .build();

            // Build the email content
            EmailContent emailContent = EmailContent.builder()
                    .simple(message)
                    .build();

            // Build the send request
            SendEmailRequest.Builder requestBuilder = SendEmailRequest.builder()
                    .fromEmailAddress(formatEmailAddress(awsSesProps.fromEmail(), awsSesProps.fromName()))
                    .destination(destinationBuilder.build())
                    .content(emailContent);

            // Add tags if provided
            if (!tags.isEmpty()) {
                requestBuilder.emailTags(
                        tags.entrySet().stream()
                                .map(entry -> MessageTag.builder()
                                        .name(entry.getKey())
                                        .value(entry.getValue())
                                        .build())
                                .toList()
                );
            }

            // Send the email
            SendEmailResponse response = sesV2Client.sendEmail(requestBuilder.build());

            logger.info("Email sent successfully to: {} with message ID: {}", toEmail, response.messageId());
            return true;

        } catch (Exception e) {
            logger.error("Failed to send email to: {}", toEmail, e);
            return false;
        }
    }

    public EmailSendResult sendEmailWithResult(String toEmail, String toName, String subject, String htmlBody, String textBody, Map<String, String> tags) {
        try {
            boolean success = sendEmail(toEmail, toName, subject, htmlBody, textBody, tags);
            if (success) {
                return EmailSendResult.success();
            } else {
                return EmailSendResult.failure("Unknown error occurred");
            }
        } catch (Exception e) {
            return EmailSendResult.failure(e.getMessage());
        }
    }

    private String formatEmailAddress(String email, String name) {
        if (name != null && !name.isEmpty()) {
            return String.format("%s <%s>", name, email);
        }
        return email;
    }

    public static class EmailSendResult {
        private final boolean success;
        private final String errorMessage;
        private final String messageId;

        private EmailSendResult(boolean success, String errorMessage, String messageId) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.messageId = messageId;
        }

        public static EmailSendResult success() {
            return new EmailSendResult(true, null, null);
        }

        public static EmailSendResult success(String messageId) {
            return new EmailSendResult(true, null, messageId);
        }

        public static EmailSendResult failure(String errorMessage) {
            return new EmailSendResult(false, errorMessage, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getMessageId() {
            return messageId;
        }
    }
}
