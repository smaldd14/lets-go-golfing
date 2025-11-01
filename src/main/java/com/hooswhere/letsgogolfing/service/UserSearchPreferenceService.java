package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.dto.SearchCriteria;
import com.hooswhere.letsgogolfing.dto.SearchCriteriaDbDto;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import com.hooswhere.letsgogolfing.entity.PriorityCourseEntity;
import com.hooswhere.letsgogolfing.entity.SearchCriteriaEntity;
import com.hooswhere.letsgogolfing.entity.UserSearchPreferenceEntity;
import com.hooswhere.letsgogolfing.repository.SearchCriteriaRepository;
import com.hooswhere.letsgogolfing.repository.UserSearchPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UserSearchPreferenceService {

    private final UserSearchPreferenceRepository userSearchPreferenceRepository;
    private final SearchCriteriaRepository searchCriteriaRepository;
    private final SearchCriteriaService searchCriteriaService;

    public UserSearchPreferenceService(UserSearchPreferenceRepository userSearchPreferenceRepository,
                                      SearchCriteriaRepository searchCriteriaRepository,
                                      SearchCriteriaService searchCriteriaService) {
        this.userSearchPreferenceRepository = userSearchPreferenceRepository;
        this.searchCriteriaRepository = searchCriteriaRepository;
        this.searchCriteriaService = searchCriteriaService;
    }

    /**
     * Creates a new user search preference.
     * Finds or creates the search criteria, then creates the preference.
     */
    @Transactional
    public UserSearchPreferenceDto createPreference(String email,
                                                    SearchCriteria searchCriteria,
                                                    boolean paymentEnabled,
                                                    boolean notifyEnabled,
                                                    Duration scheduleInterval) {
        // Find or create search criteria
        SearchCriteriaDbDto criteriaDto = searchCriteriaService.findOrCreate(searchCriteria);

        // Get the entity
        SearchCriteriaEntity criteriaEntity = searchCriteriaRepository.findById(criteriaDto.id())
                .orElseThrow(() -> new IllegalStateException("Search criteria not found after creation"));

        // Create user preference
        UserSearchPreferenceEntity preference = new UserSearchPreferenceEntity();
        preference.setEmail(email);
        preference.setSearchCriteria(criteriaEntity);
        preference.setPaymentEnabled(paymentEnabled);
        preference.setNotifyEnabled(notifyEnabled);
        preference.setScheduleInterval(scheduleInterval.toString());  // Store as ISO 8601 string in DB
        preference.setActive(true);

        UserSearchPreferenceEntity saved = userSearchPreferenceRepository.save(preference);
        return toDto(saved);
    }

    /**
     * Creates a new user search preference from an existing search criteria ID.
     * Used by Stripe webhook after payment.
     */
    @Transactional
    public UserSearchPreferenceDto createPreferenceFromCriteriaId(String email,
                                                                   UUID searchCriteriaId,
                                                                   boolean paymentEnabled,
                                                                   boolean notifyEnabled,
                                                                   Duration scheduleInterval) {
        // Get the existing search criteria entity
        SearchCriteriaEntity criteriaEntity = searchCriteriaRepository.findById(searchCriteriaId)
                .orElseThrow(() -> new IllegalArgumentException("Search criteria not found: " + searchCriteriaId));

        // Create user preference
        UserSearchPreferenceEntity preference = new UserSearchPreferenceEntity();
        preference.setEmail(email);
        preference.setSearchCriteria(criteriaEntity);
        preference.setPaymentEnabled(paymentEnabled);
        preference.setNotifyEnabled(notifyEnabled);
        preference.setScheduleInterval(scheduleInterval.toString());
        preference.setActive(true);

        UserSearchPreferenceEntity saved = userSearchPreferenceRepository.save(preference);
        return toDto(saved);
    }

    /**
     * Gets all active search preferences for a user.
     */
    @Transactional(readOnly = true)
    public List<UserSearchPreferenceDto> getActivePreferences(String email) {
        return userSearchPreferenceRepository.findByEmail(email).stream()
                .filter(UserSearchPreferenceEntity::isActive)
                .map(this::toDto)
                .toList();
    }

    /**
     * Deactivates a user search preference.
     */
    @Transactional
    public void deactivatePreference(UUID preferenceId) {
        UserSearchPreferenceEntity preference = userSearchPreferenceRepository.findById(preferenceId)
                .orElseThrow(() -> new IllegalArgumentException("Preference not found: " + preferenceId));

        preference.setActive(false);
        userSearchPreferenceRepository.save(preference);
    }

    /**
     * Updates the schedule ID for a preference (called after creating Temporal schedule).
     */
    @Transactional
    public void updateScheduleId(UUID preferenceId, String scheduleId) {
        UserSearchPreferenceEntity preference = userSearchPreferenceRepository.findById(preferenceId)
                .orElseThrow(() -> new IllegalArgumentException("Preference not found: " + preferenceId));

        preference.setScheduleId(scheduleId);
        userSearchPreferenceRepository.save(preference);
    }

    /**
     * Gets a preference by ID.
     */
    @Transactional(readOnly = true)
    public UserSearchPreferenceDto getPreference(UUID preferenceId) {
        UserSearchPreferenceEntity entity = userSearchPreferenceRepository.findById(preferenceId)
                .orElseThrow(() -> new NoSuchElementException("Preference not found: " + preferenceId));
        return toDto(entity);
    }

    private UserSearchPreferenceDto toDto(UserSearchPreferenceEntity entity) {
        // Get the full search criteria with priority courses
        List<Integer> priorityCourseIds = entity.getSearchCriteria().getPriorityCourses().stream()
                .map(PriorityCourseEntity::getFacilityId)
                .toList();

        SearchCriteriaDbDto criteriaDto = new SearchCriteriaDbDto(
                entity.getSearchCriteria().getId(),
                entity.getSearchCriteria().getLatitude(),
                entity.getSearchCriteria().getLongitude(),
                entity.getSearchCriteria().getRadiusMiles(),
                entity.getSearchCriteria().getSearchDate(),
                entity.getSearchCriteria().getNumberOfPlayers(),
                entity.getSearchCriteria().getPreferredTimeStart(),
                entity.getSearchCriteria().getPreferredTimeEnd(),
                entity.getSearchCriteria().getMaxPrice(),
                entity.getSearchCriteria().isHotDealsOnly(),
                entity.getSearchCriteria().getHoles(),
                entity.getSearchCriteria().getCreatedAt(),
                priorityCourseIds
        );

        // Parse schedule interval from ISO 8601 string to Duration
        Duration scheduleInterval = entity.getScheduleInterval() != null
                ? Duration.parse(entity.getScheduleInterval())
                : null;

        return new UserSearchPreferenceDto(
                entity.getId(),
                entity.getEmail(),
                criteriaDto,
                entity.isPaymentEnabled(),
                entity.isNotifyEnabled(),
                entity.getScheduleId(),
                scheduleInterval,
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
