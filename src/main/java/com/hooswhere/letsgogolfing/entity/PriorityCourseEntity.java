package com.hooswhere.letsgogolfing.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "priority_courses")
public class PriorityCourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_criteria_id", nullable = false)
    private SearchCriteriaEntity searchCriteria;

    @Column(name = "facility_id", nullable = false)
    private int facilityId;

    public PriorityCourseEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public SearchCriteriaEntity getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(SearchCriteriaEntity searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }
}
