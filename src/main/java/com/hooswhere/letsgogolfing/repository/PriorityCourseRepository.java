package com.hooswhere.letsgogolfing.repository;

import com.hooswhere.letsgogolfing.entity.PriorityCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PriorityCourseRepository extends JpaRepository<PriorityCourseEntity, UUID> {

    List<PriorityCourseEntity> findBySearchCriteriaId(UUID searchCriteriaId);

    void deleteBySearchCriteriaId(UUID searchCriteriaId);
}
