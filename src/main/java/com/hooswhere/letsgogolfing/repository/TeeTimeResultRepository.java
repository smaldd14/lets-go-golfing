package com.hooswhere.letsgogolfing.repository;

import com.hooswhere.letsgogolfing.entity.TeeTimeResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeeTimeResultRepository extends JpaRepository<TeeTimeResultEntity, UUID> {

    Optional<TeeTimeResultEntity> findByFacilityIdAndTeeTime(int facilityId, LocalDateTime teeTime);

    List<TeeTimeResultEntity> findByFacilityIdAndTeeTimeBetween(
            int facilityId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("SELECT t FROM TeeTimeResultEntity t WHERE t.facilityId IN :facilityIds " +
           "AND t.teeTime BETWEEN :startTime AND :endTime")
    List<TeeTimeResultEntity> findByFacilityIdsAndTeeTimeBetween(
            @Param("facilityIds") List<Integer> facilityIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    void deleteByTeeTimeBefore(LocalDateTime cutoffTime);
}
