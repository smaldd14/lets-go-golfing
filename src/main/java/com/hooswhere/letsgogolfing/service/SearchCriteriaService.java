package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.dto.SearchCriteria;
import com.hooswhere.letsgogolfing.dto.SearchCriteriaDbDto;
import com.hooswhere.letsgogolfing.entity.PriorityCourseEntity;
import com.hooswhere.letsgogolfing.entity.SearchCriteriaEntity;
import com.hooswhere.letsgogolfing.repository.PriorityCourseRepository;
import com.hooswhere.letsgogolfing.repository.SearchCriteriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SearchCriteriaService {

    private final SearchCriteriaRepository searchCriteriaRepository;
    private final PriorityCourseRepository priorityCourseRepository;

    public SearchCriteriaService(SearchCriteriaRepository searchCriteriaRepository,
                                 PriorityCourseRepository priorityCourseRepository) {
        this.searchCriteriaRepository = searchCriteriaRepository;
        this.priorityCourseRepository = priorityCourseRepository;
    }

    /**
     * Finds existing search criteria or creates a new one.
     * Uses the unique constraint fields to prevent duplicates.
     */
    @Transactional
    public SearchCriteriaDbDto findOrCreate(SearchCriteria criteria) {
        SearchCriteriaEntity entity = searchCriteriaRepository
                .findByLatitudeAndLongitudeAndRadiusMilesAndSearchDateAndNumberOfPlayersAndPreferredTimeStartAndPreferredTimeEndAndHotDealsOnlyAndHoles(
                        criteria.latitude(),
                        criteria.longitude(),
                        criteria.radiusMiles(),
                        criteria.searchDate(),
                        criteria.numberOfPlayers(),
                        criteria.preferredTimeStart() != null ? criteria.preferredTimeStart() : 5,
                        criteria.preferredTimeEnd() != null ? criteria.preferredTimeEnd() : 21,
                        criteria.hotDealsOnly(),
                        criteria.holes()
                )
                .orElseGet(() -> createNew(criteria));

        return toDto(entity);
    }

    private SearchCriteriaEntity createNew(SearchCriteria criteria) {
        SearchCriteriaEntity entity = new SearchCriteriaEntity();
        entity.setLatitude(criteria.latitude());
        entity.setLongitude(criteria.longitude());
        entity.setRadiusMiles(criteria.radiusMiles());
        entity.setSearchDate(criteria.searchDate());
        entity.setNumberOfPlayers(criteria.numberOfPlayers());
        entity.setPreferredTimeStart(criteria.preferredTimeStart() != null ? criteria.preferredTimeStart() : 5);
        entity.setPreferredTimeEnd(criteria.preferredTimeEnd() != null ? criteria.preferredTimeEnd() : 21);
        entity.setMaxPrice(criteria.maxPrice());
        entity.setHotDealsOnly(criteria.hotDealsOnly());
        entity.setHoles(criteria.holes());

        SearchCriteriaEntity saved = searchCriteriaRepository.save(entity);

        // Save priority courses if provided
        if (criteria.priorityCourseIds() != null && !criteria.priorityCourseIds().isEmpty()) {
            for (Integer facilityId : criteria.priorityCourseIds()) {
                PriorityCourseEntity priorityCourse = new PriorityCourseEntity();
                priorityCourse.setSearchCriteria(saved);
                priorityCourse.setFacilityId(facilityId);
                priorityCourseRepository.save(priorityCourse);
            }
        }

        return saved;
    }

    /**
     * Updates priority courses for a given search criteria.
     * Replaces existing priority courses with the new list.
     */
    @Transactional
    public void updatePriorityCourses(UUID searchCriteriaId, List<Integer> facilityIds) {
        // Delete existing priority courses
        priorityCourseRepository.deleteBySearchCriteriaId(searchCriteriaId);

        // Add new priority courses
        SearchCriteriaEntity criteria = searchCriteriaRepository.findById(searchCriteriaId)
                .orElseThrow(() -> new IllegalArgumentException("Search criteria not found: " + searchCriteriaId));

        for (Integer facilityId : facilityIds) {
            PriorityCourseEntity priorityCourse = new PriorityCourseEntity();
            priorityCourse.setSearchCriteria(criteria);
            priorityCourse.setFacilityId(facilityId);
            priorityCourseRepository.save(priorityCourse);
        }
    }

    /**
     * Gets priority course IDs for a given search criteria.
     */
    public List<Integer> getPriorityCourseIds(UUID searchCriteriaId) {
        return priorityCourseRepository.findBySearchCriteriaId(searchCriteriaId)
                .stream()
                .map(PriorityCourseEntity::getFacilityId)
                .toList();
    }

    private SearchCriteriaDbDto toDto(SearchCriteriaEntity entity) {
        List<Integer> priorityCourseIds = priorityCourseRepository
                .findBySearchCriteriaId(entity.getId())
                .stream()
                .map(PriorityCourseEntity::getFacilityId)
                .toList();

        return new SearchCriteriaDbDto(
                entity.getId(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getRadiusMiles(),
                entity.getSearchDate(),
                entity.getNumberOfPlayers(),
                entity.getPreferredTimeStart(),
                entity.getPreferredTimeEnd(),
                entity.getMaxPrice(),
                entity.isHotDealsOnly(),
                entity.getHoles(),
                entity.getCreatedAt(),
                priorityCourseIds
        );
    }
}
