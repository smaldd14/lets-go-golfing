package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.entity.McpTokenEntity;
import com.hooswhere.letsgogolfing.repository.McpTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class McpTokenService {

    private static final String TOKEN_PREFIX = "mcp_";
    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final McpTokenRepository mcpTokenRepository;

    public McpTokenService(McpTokenRepository mcpTokenRepository) {
        this.mcpTokenRepository = mcpTokenRepository;
    }

    /**
     * Generates a new high-entropy token for the user, storing only its hash.
     * Existing tokens for the email are revoked so each issue replaces the last.
     * Returns the raw token, which is only available here and never persisted.
     */
    @Transactional
    public String issueToken(String email) {
        mcpTokenRepository.findByEmailAndRevokedFalse(email).forEach(token -> {
            token.setRevoked(true);
            mcpTokenRepository.save(token);
        });

        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);
        String rawToken = TOKEN_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        McpTokenEntity entity = new McpTokenEntity();
        entity.setEmail(email);
        entity.setTokenHash(hash(rawToken));
        entity.setRevoked(false);
        mcpTokenRepository.save(entity);

        return rawToken;
    }

    /**
     * Issues a token only if the user has no active token yet. Used by the webhook so that
     * redelivered checkout events don't churn the user's token. Returns the raw token when a
     * new one was created, empty otherwise.
     */
    @Transactional
    public Optional<String> issueTokenIfAbsent(String email) {
        if (!mcpTokenRepository.findByEmailAndRevokedFalse(email).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(issueToken(email));
    }

    /**
     * Resolves a raw token to its owner email, touching last_used_at.
     * Returns empty if the token is unknown or revoked.
     */
    @Transactional
    public Optional<String> resolveEmail(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return mcpTokenRepository.findByTokenHash(hash(rawToken))
                .filter(token -> !token.isRevoked())
                .map(token -> {
                    token.setLastUsedAt(LocalDateTime.now());
                    mcpTokenRepository.save(token);
                    return token.getEmail();
                });
    }

    @Transactional
    public void revokeForEmail(String email) {
        mcpTokenRepository.findByEmailAndRevokedFalse(email).forEach(token -> {
            token.setRevoked(true);
            mcpTokenRepository.save(token);
        });
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
