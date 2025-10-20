package com.hooswhere.letsgogolfing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "search_criteria")
public class SearchCriteriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "radius_miles", nullable = false)
    private int radiusMiles;

    @Column(name = "search_date", nullable = false, length = 20)
    private String searchDate;

    @Column(name = "number_of_players", nullable = false)
    private int numberOfPlayers;

    @Column(name = "preferred_time_start", nullable = false)
    private int preferredTimeStart = 5;

    @Column(name = "preferred_time_end", nullable = false)
    private int preferredTimeEnd = 21;

    @Column(name = "max_price")
    private Integer maxPrice;

    @Column(name = "hot_deals_only", nullable = false)
    private boolean hotDealsOnly;

    @Column(nullable = false)
    private int holes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "searchCriteria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriorityCourseEntity> priorityCourses = new ArrayList<>();

    @OneToMany(mappedBy = "searchCriteria", cascade = CascadeType.ALL)
    private List<UserSearchPreferenceEntity> userSearchPreferences = new ArrayList<>();

    public SearchCriteriaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getRadiusMiles() {
        return radiusMiles;
    }

    public void setRadiusMiles(int radiusMiles) {
        this.radiusMiles = radiusMiles;
    }

    public String getSearchDate() {
        return searchDate;
    }

    public void setSearchDate(String searchDate) {
        this.searchDate = searchDate;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public int getPreferredTimeStart() {
        return preferredTimeStart;
    }

    public void setPreferredTimeStart(int preferredTimeStart) {
        this.preferredTimeStart = preferredTimeStart;
    }

    public int getPreferredTimeEnd() {
        return preferredTimeEnd;
    }

    public void setPreferredTimeEnd(int preferredTimeEnd) {
        this.preferredTimeEnd = preferredTimeEnd;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public boolean isHotDealsOnly() {
        return hotDealsOnly;
    }

    public void setHotDealsOnly(boolean hotDealsOnly) {
        this.hotDealsOnly = hotDealsOnly;
    }

    public int getHoles() {
        return holes;
    }

    public void setHoles(int holes) {
        this.holes = holes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<PriorityCourseEntity> getPriorityCourses() {
        return priorityCourses;
    }

    public void setPriorityCourses(List<PriorityCourseEntity> priorityCourses) {
        this.priorityCourses = priorityCourses;
    }

    public List<UserSearchPreferenceEntity> getUserSearchPreferences() {
        return userSearchPreferences;
    }

    public void setUserSearchPreferences(List<UserSearchPreferenceEntity> userSearchPreferences) {
        this.userSearchPreferences = userSearchPreferences;
    }
}
