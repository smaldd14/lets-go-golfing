package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.dto.UserNotificationDbDto;
import com.hooswhere.letsgogolfing.entity.TeeTimeResultEntity;
import com.hooswhere.letsgogolfing.entity.UserNotificationEntity;
import com.hooswhere.letsgogolfing.entity.UserSearchPreferenceEntity;
import com.hooswhere.letsgogolfing.repository.TeeTimeResultRepository;
import com.hooswhere.letsgogolfing.repository.UserNotificationRepository;
import com.hooswhere.letsgogolfing.repository.UserSearchPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final UserSearchPreferenceRepository userSearchPreferenceRepository;
    private final TeeTimeResultRepository teeTimeResultRepository;

    public UserNotificationService(UserNotificationRepository userNotificationRepository,
                                   UserSearchPreferenceRepository userSearchPreferenceRepository,
                                   TeeTimeResultRepository teeTimeResultRepository) {
        this.userNotificationRepository = userNotificationRepository;
        this.userSearchPreferenceRepository = userSearchPreferenceRepository;
        this.teeTimeResultRepository = teeTimeResultRepository;
    }

    /**
     * Records that a user has been notified about specific tee time results.
     * Skips if the notification already exists.
     */
    @Transactional
    public List<UserNotificationDbDto> recordNotifications(UUID userSearchPreferenceId,
                                                           List<UUID> teeTimeResultIds) {
        UserSearchPreferenceEntity userPreference = userSearchPreferenceRepository
                .findById(userSearchPreferenceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User search preference not found: " + userSearchPreferenceId));

        return teeTimeResultIds.stream()
                .filter(teeTimeResultId -> !wasAlreadyNotified(userSearchPreferenceId, teeTimeResultId))
                .map(teeTimeResultId -> createNotification(userPreference, teeTimeResultId))
                .toList();
    }

    private boolean wasAlreadyNotified(UUID userSearchPreferenceId, UUID teeTimeResultId) {
        return userNotificationRepository.existsByUserSearchPreferenceIdAndTeeTimeResultId(
                userSearchPreferenceId, teeTimeResultId);
    }

    private UserNotificationDbDto createNotification(UserSearchPreferenceEntity userPreference,
                                                     UUID teeTimeResultId) {
        TeeTimeResultEntity teeTimeResult = teeTimeResultRepository
                .findById(teeTimeResultId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tee time result not found: " + teeTimeResultId));

        UserNotificationEntity entity = new UserNotificationEntity();
        entity.setUserSearchPreference(userPreference);
        entity.setTeeTimeResult(teeTimeResult);

        UserNotificationEntity saved = userNotificationRepository.save(entity);
        return toDto(saved);
    }

    /**
     * Gets all notifications for a user's search preference.
     */
    public List<UserNotificationDbDto> getNotificationsForUser(UUID userSearchPreferenceId) {
        return userNotificationRepository
                .findByUserSearchPreferenceId(userSearchPreferenceId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private UserNotificationDbDto toDto(UserNotificationEntity entity) {
        return new UserNotificationDbDto(
                entity.getId(),
                entity.getUserSearchPreference().getId(),
                entity.getTeeTimeResult().getId(),
                entity.getNotifiedAt()
        );
    }
}
