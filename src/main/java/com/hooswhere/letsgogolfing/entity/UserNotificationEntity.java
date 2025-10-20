package com.hooswhere.letsgogolfing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_notifications")
public class UserNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_search_preference_id", nullable = false)
    private UserSearchPreferenceEntity userSearchPreference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tee_time_result_id", nullable = false)
    private TeeTimeResultEntity teeTimeResult;

    @Column(name = "notified_at", nullable = false, updatable = false)
    private LocalDateTime notifiedAt = LocalDateTime.now();

    public UserNotificationEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserSearchPreferenceEntity getUserSearchPreference() {
        return userSearchPreference;
    }

    public void setUserSearchPreference(UserSearchPreferenceEntity userSearchPreference) {
        this.userSearchPreference = userSearchPreference;
    }

    public TeeTimeResultEntity getTeeTimeResult() {
        return teeTimeResult;
    }

    public void setTeeTimeResult(TeeTimeResultEntity teeTimeResult) {
        this.teeTimeResult = teeTimeResult;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
}
