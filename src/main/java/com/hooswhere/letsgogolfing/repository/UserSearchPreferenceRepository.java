package com.hooswhere.letsgogolfing.repository;

import com.hooswhere.letsgogolfing.entity.UserSearchPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSearchPreferenceRepository extends JpaRepository<UserSearchPreferenceEntity, UUID> {

    List<UserSearchPreferenceEntity> findByEmail(String email);

    List<UserSearchPreferenceEntity> findByActive(boolean active);

    Optional<UserSearchPreferenceEntity> findByEmailAndSearchCriteriaId(String email, UUID searchCriteriaId);

    Optional<UserSearchPreferenceEntity> findByScheduleId(String scheduleId);
}
