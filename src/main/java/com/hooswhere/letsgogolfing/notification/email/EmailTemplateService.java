package com.hooswhere.letsgogolfing.notification.email;

import com.hooswhere.letsgogolfing.entity.EmailTemplateEntity;
import com.hooswhere.letsgogolfing.repository.EmailTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailTemplateService {
    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateService.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    private final EmailTemplateRepository templateRepository;

    public EmailTemplateService(EmailTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
        initializeDefaultTemplates();
    }

    public Optional<EmailTemplate> getTemplate(String templateSlug) {
        return templateRepository.findBySlugAndIsActiveTrue(templateSlug)
                .map(this::convertToModel);
    }

    public String renderTemplate(String templateSlug, EmailTemplateContext context) {
        EmailTemplateEntity entity = templateRepository.findBySlugAndIsActiveTrue(templateSlug)
                .orElse(null);
        if (entity == null) {
            logger.warn("Template not found: {}", templateSlug);
            return null;
        }

        return renderVariables(entity.getSubject(), context);
    }

    public EmailTemplate renderFullTemplate(String templateSlug, EmailTemplateContext context) {
        EmailTemplateEntity entity = templateRepository.findBySlugAndIsActiveTrue(templateSlug)
                .orElse(null);
        if (entity == null) {
            logger.warn("Template not found: {}", templateSlug);
            return null;
        }

        String renderedSubject = renderVariables(entity.getSubject(), context);
        String renderedHtmlBody = entity.getHtmlBody() != null ? renderVariables(entity.getHtmlBody(), context) : null;
        String renderedTextBody = entity.getTextBody() != null ? renderVariables(entity.getTextBody(), context) : null;

        return new EmailTemplate(entity.getSlug(), renderedSubject, renderedHtmlBody, renderedTextBody);
    }

    private String renderVariables(String content, EmailTemplateContext context) {
        if (content == null) return null;

        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = context.get(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    @Transactional
    public EmailTemplateEntity createTemplate(String slug, String name, String subject, String htmlBody, String textBody) {
        if (templateRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Template with slug already exists: " + slug);
        }

        EmailTemplateEntity template = new EmailTemplateEntity(slug, name, subject, htmlBody, textBody);
        EmailTemplateEntity saved = templateRepository.save(template);
        logger.info("Created email template: {}", slug);
        return saved;
    }

    private EmailTemplate convertToModel(EmailTemplateEntity entity) {
        return new EmailTemplate(
                entity.getSlug(),
                entity.getSubject(),
                entity.getHtmlBody(),
                entity.getTextBody()
        );
    }

    @Transactional
    public void initializeDefaultTemplates() {
        // Only create default templates if none exist
        if (templateRepository.count() == 0) {
            // Tee time notification template
            createTemplate(
                    "tee-time-notification",
                    "Tee Time Notification",
                    "🏌️ {{teeTimeCount}} New Tee Time{{pluralSuffix}} Available!",
                    """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5; }
                            .container { background: white; border-radius: 8px; padding: 30px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                            h1 { color: #2c5f2d; margin-bottom: 10px; font-size: 24px; }
                            .subtitle { color: #666; margin-bottom: 25px; font-size: 14px; }
                            .tee-time { background: #f8f9fa; border-left: 4px solid #28a745; padding: 20px; margin: 20px 0; border-radius: 4px; }
                            .tee-time h2 { color: #2c5f2d; margin: 0 0 15px 0; font-size: 18px; }
                            .detail { margin: 10px 0; color: #555; font-size: 15px; }
                            .detail strong { color: #333; display: inline-block; min-width: 80px; }
                            .price { font-size: 24px; color: #28a745; font-weight: bold; margin: 15px 0; }
                            .book-button { display: inline-block; background: #28a745; color: white !important; padding: 14px 28px; text-decoration: none; border-radius: 6px; margin-top: 15px; font-weight: bold; font-size: 16px; transition: background 0.3s; }
                            .book-button:hover { background: #218838; }
                            .footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; text-align: center; }
                            .footer a { color: #28a745; text-decoration: none; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>🏌️ New Tee Times Available!</h1>
                            <p class="subtitle">We found {{teeTimeCount}} new tee time{{pluralSuffix}} matching your search criteria</p>

                            {{teeTimesList}}

                            <div class="footer">
                                <p>This is an automated notification from Let's Go Golfing.</p>
                                <p>To manage your search preferences or unsubscribe, <a href="{{unsubscribeUrl}}">click here</a>.</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """,
                    """
                    NEW TEE TIMES AVAILABLE
                    ========================

                    We found {{teeTimeCount}} new tee time{{pluralSuffix}} matching your search criteria:

                    {{teeTimesListText}}

                    ---
                    This is an automated notification from Let's Go Golfing.
                    To manage your search preferences or unsubscribe, visit: {{unsubscribeUrl}}
                    """
            );

            logger.info("Initialized default email templates");
        } else {
            logger.info("Default templates already exist, skipping initialization");
        }
    }

    /**
     * List all email templates ordered by creation date.
     */
    public List<EmailTemplateEntity> listAllTemplates() {
        return templateRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get a template entity by slug.
     */
    public Optional<EmailTemplateEntity> getTemplateEntity(String slug) {
        return templateRepository.findBySlug(slug);
    }

    /**
     * Update an existing template and increment version.
     */
    @Transactional
    public EmailTemplateEntity updateTemplate(String slug, String name, String subject, String htmlBody, String textBody) {
        EmailTemplateEntity entity = templateRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Template not found: " + slug));
        entity.setName(name);
        entity.setSubject(subject);
        entity.setHtmlBody(htmlBody);
        entity.setTextBody(textBody);
        entity.setVersion(entity.getVersion() + 1);
        return templateRepository.save(entity);
    }

    /**
     * Delete a template by slug.
     */
    @Transactional
    public void deleteTemplate(String slug) {
        EmailTemplateEntity entity = templateRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Template not found: " + slug));
        templateRepository.delete(entity);
    }

    /**
     * Activate or deactivate a template.
     */
    @Transactional
    public EmailTemplateEntity setActive(String slug, boolean active) {
        EmailTemplateEntity entity = templateRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Template not found: " + slug));
        entity.setActive(active);
        return templateRepository.save(entity);
    }

    /**
     * Preview rendered template with provided variables.
     */
    public EmailTemplate previewTemplate(String slug, Map<String, Object> variables) {
        return renderFullTemplate(slug, EmailTemplateContext.builder().putAll(variables).build());
    }
}
