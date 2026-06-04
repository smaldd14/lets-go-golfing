package com.hooswhere.letsgogolfing.repository;

import com.hooswhere.letsgogolfing.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {

    Optional<SubscriptionEntity> findByStripeSubscriptionId(String stripeSubscriptionId);

    List<SubscriptionEntity> findByEmailOrderByCreatedAtDesc(String email);
}
