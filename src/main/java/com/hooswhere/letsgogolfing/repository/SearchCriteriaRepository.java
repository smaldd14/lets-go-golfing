package com.hooswhere.letsgogolfing.repository;

import com.hooswhere.letsgogolfing.entity.SearchCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SearchCriteriaRepository extends JpaRepository<SearchCriteriaEntity, UUID> {

    Optional<SearchCriteriaEntity> findByLatitudeAndLongitudeAndRadiusMilesAndSearchDateAndNumberOfPlayersAndPreferredTimeStartAndPreferredTimeEndAndHotDealsOnlyAndHoles(
            double latitude,
            double longitude,
            int radiusMiles,
            String searchDate,
            int numberOfPlayers,
            int preferredTimeStart,
            int preferredTimeEnd,
            boolean hotDealsOnly,
            int holes
    );
}
