package com.hooswhere.letsgogolfing.repository;

import com.hooswhere.letsgogolfing.entity.McpTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface McpTokenRepository extends JpaRepository<McpTokenEntity, UUID> {

    Optional<McpTokenEntity> findByTokenHash(String tokenHash);

    List<McpTokenEntity> findByEmailAndRevokedFalse(String email);
}
