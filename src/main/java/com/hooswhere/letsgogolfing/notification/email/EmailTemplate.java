package com.hooswhere.letsgogolfing.notification.email;

public record EmailTemplate(
        String templateId,
        String subject,
        String htmlBody,
        String textBody
) {
    public static EmailTemplate create(String templateId, String subject, String htmlBody, String textBody) {
        return new EmailTemplate(templateId, subject, htmlBody, textBody);
    }

    public static EmailTemplate htmlOnly(String templateId, String subject, String htmlBody) {
        return new EmailTemplate(templateId, subject, htmlBody, null);
    }

    public static EmailTemplate textOnly(String templateId, String subject, String textBody) {
        return new EmailTemplate(templateId, subject, null, textBody);
    }

    public boolean hasHtmlBody() {
        return htmlBody != null && !htmlBody.isEmpty();
    }

    public boolean hasTextBody() {
        return textBody != null && !textBody.isEmpty();
    }
}
