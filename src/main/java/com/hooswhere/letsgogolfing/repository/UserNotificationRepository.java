package com.hooswhere.letsgogolfing.repository;

import com.hooswhere.letsgogolfing.entity.UserNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotificationEntity, UUID> {

    List<UserNotificationEntity> findByUserSearchPreferenceId(UUID userSearchPreferenceId);

    Optional<UserNotificationEntity> findByUserSearchPreferenceIdAndTeeTimeResultId(
            UUID userSearchPreferenceId,
            UUID teeTimeResultId
    );

    boolean existsByUserSearchPreferenceIdAndTeeTimeResultId(
            UUID userSearchPreferenceId,
            UUID teeTimeResultId
    );
}
