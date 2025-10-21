package com.hooswhere.letsgogolfing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA entity for email_templates table.
 */
@Entity
@Table(name = "email_templates")
public class EmailTemplateEntity {
    /*
    CREATE TABLE IF NOT EXISTS email_templates (
    slug UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    html_body TEXT,
    text_body TEXT,
    is_active BOOLEAN DEFAULT true,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
     */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Template slug cannot be blank")
    @Size(max = 100, message = "Template slug cannot exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-_]+$", message = "Slug must contain only lowercase letters, numbers, hyphens, and underscores")
    private String slug;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @Column(name = "subject", nullable = false, length = 500)
    @NotBlank(message = "Subject cannot be blank")
    @Size(max = 500, message = "Subject cannot exceed 500 characters")
    private String subject;

    @Column(name = "html_body", columnDefinition = "TEXT")
    private String htmlBody;

    @Column(name = "text_body", columnDefinition = "TEXT")
    private String textBody;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "version", nullable = false)
    @Min(value = 1, message = "Version must be at least 1")
    private int version = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;

    public EmailTemplateEntity() {
    }

    public EmailTemplateEntity(String slug, String name, String subject, String htmlBody, String textBody) {
        this.slug = slug;
        this.name = name;
        this.subject = subject;
        this.htmlBody = htmlBody;
        this.textBody = textBody;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public String getTextBody() {
        return textBody;
    }

    public void setTextBody(String textBody) {
        this.textBody = textBody;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailTemplateEntity that = (EmailTemplateEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EmailTemplateEntity{" +
               "slug=" + id +
               ", slug='" + slug + '\'' +
               ", name='" + name + '\'' +
               ", subject='" + subject + '\'' +
               ", isActive=" + isActive +
               ", version=" + version +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
