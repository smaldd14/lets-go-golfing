package com.hooswhere.letsgogolfing.repository;

import com.hooswhere.letsgogolfing.entity.EmailTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EmailTemplateEntity CRUD operations.
 */
public interface EmailTemplateRepository extends JpaRepository<EmailTemplateEntity, UUID> {

    /**
     * Find all active templates ordered by creation date.
     */
    List<EmailTemplateEntity> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * Find all templates (active and inactive) ordered by creation date.
     */
    List<EmailTemplateEntity> findAllByOrderByCreatedAtDesc();

    /**
     * Find template by slug.
     */
    Optional<EmailTemplateEntity> findBySlug(String slug);

    /**
     * Find active template by slug.
     */
    Optional<EmailTemplateEntity> findBySlugAndIsActiveTrue(String slug);

    /**
     * Find template by name (case-insensitive).
     */
    Optional<EmailTemplateEntity> findByNameIgnoreCase(String name);

    /**
     * Find active template by ID.
     */
    Optional<EmailTemplateEntity> findByIdAndIsActiveTrue(UUID id);

    /**
     * Check if template exists by slug.
     */
    boolean existsBySlug(String slug);

    /**
     * Find templates by name pattern (case-insensitive).
     */
    @Query("SELECT t FROM EmailTemplateEntity t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :namePattern, '%')) ORDER BY t.createdAt DESC")
    List<EmailTemplateEntity> findByNameContainingIgnoreCase(@Param("namePattern") String namePattern);

    /**
     * Find templates by active status.
     */
    List<EmailTemplateEntity> findByIsActive(boolean isActive);

    /**
     * Count active templates.
     */
    long countByIsActiveTrue();

    /**
     * Find all templates with their latest version.
     */
    @Query("SELECT t FROM EmailTemplateEntity t WHERE t.version = (SELECT MAX(t2.version) FROM EmailTemplateEntity t2 WHERE t2.slug = t.slug)")
    List<EmailTemplateEntity> findLatestVersionTemplates();
}

