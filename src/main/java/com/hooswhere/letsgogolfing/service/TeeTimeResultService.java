package com.hooswhere.letsgogolfing.service;

import com.hooswhere.letsgogolfing.dto.TeeTimeResultDbDto;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import com.hooswhere.letsgogolfing.entity.TeeTimeResultEntity;
import com.hooswhere.letsgogolfing.repository.TeeTimeResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.hooswhere.letsgogolfing.golfnow.GolfNowUtil.generateBookingUrl;

@Service
public class TeeTimeResultService {

    private final TeeTimeResultRepository teeTimeResultRepository;

    public TeeTimeResultService(TeeTimeResultRepository teeTimeResultRepository) {
        this.teeTimeResultRepository = teeTimeResultRepository;
    }

    /**
     * Saves or updates tee time results.
     * If a result already exists (same facility + tee time), updates last_seen_at.
     * Otherwise, creates a new result.
     */
    @Transactional
    public List<TeeTimeResultDbDto> saveOrUpdateResults(List<TeeTimeSlot> teeTimeSlots) {
        return teeTimeSlots.stream()
                .map(this::saveOrUpdateSingle)
                .toList();
    }

    private TeeTimeResultDbDto saveOrUpdateSingle(TeeTimeSlot slot) {
        LocalDateTime teeTime = slot.time().toLocalDateTime();

        TeeTimeResultEntity entity = teeTimeResultRepository
                .findByFacilityIdAndTeeTime(slot.facilityId(), teeTime)
                .orElseGet(() -> createNew(slot, teeTime));

        // Update last_seen_at and price (price might change)
        entity.setLastSeenAt(LocalDateTime.now());
        entity.setPrice(BigDecimal.valueOf(slot.displayRate().value()));

        TeeTimeResultEntity saved = teeTimeResultRepository.save(entity);
        return toDto(saved);
    }

    private TeeTimeResultEntity createNew(TeeTimeSlot slot, LocalDateTime teeTime) {
        TeeTimeResultEntity entity = new TeeTimeResultEntity();
        entity.setFacilityId(slot.facilityId());
        entity.setFacilityName(slot.facility().name());
        entity.setTeeTime(teeTime);
        entity.setPrice(BigDecimal.valueOf(slot.displayRate().value()));
        entity.setBookingUrl(generateBookingUrl(slot, 1)); // Default to 1 player for URL
        return entity;
    }

    /**
     * Gets previous results for given facilities and time range.
     * Used to filter out tee times that have already been seen.
     */
    public List<TeeTimeResultDbDto> getPreviousResults(List<Integer> facilityIds,
                                                       LocalDateTime startTime,
                                                       LocalDateTime endTime) {
        return teeTimeResultRepository
                .findByFacilityIdsAndTeeTimeBetween(facilityIds, startTime, endTime)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Cleans up past tee times (tee time has already occurred).
     */
    @Transactional
    public void cleanupPastTeeTimes() {
        teeTimeResultRepository.deleteByTeeTimeBefore(LocalDateTime.now());
    }

    private TeeTimeResultDbDto toDto(TeeTimeResultEntity entity) {
        return new TeeTimeResultDbDto(
                entity.getId(),
                entity.getFacilityId(),
                entity.getFacilityName(),
                entity.getTeeTime(),
                entity.getPrice(),
                entity.getBookingUrl(),
                entity.getFirstSeenAt(),
                entity.getLastSeenAt()
        );
    }
}
