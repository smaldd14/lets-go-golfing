package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.*;
import com.hooswhere.letsgogolfing.golfnow.GolfNowConfigProps;
import com.hooswhere.letsgogolfing.service.GolfNowStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LetsGoGolfingController implements LetsGoGolfingApi {
    private static final Logger LOG = LoggerFactory.getLogger(LetsGoGolfingController.class);
    private final GolfNowStarter starter;
    private final GolfNowConfigProps configProps;

    public LetsGoGolfingController(GolfNowConfigProps configProps,
                                   GolfNowStarter starter) {
        this.starter = starter;
        this.configProps = configProps;
    }

    @Override
    public List<FacilitySummary> searchFacilities(SearchCriteria searchCriteria) {
        LOG.info("Searching facilities for criteria: lat={}, lon={}, radius={}, date={}",
                searchCriteria.latitude(), searchCriteria.longitude(),
                searchCriteria.radiusMiles(), searchCriteria.searchDate());
        // TODO: fetch user's email from auth context
        UserPreferencesLegacy userPreferences = new UserPreferencesLegacy(configProps.email(),
                                                                          false, searchCriteria);
        return starter.startFacilitySearchWf(userPreferences);
    }

    @Override
    public List<TeeTimeSlot> searchTeeTimes(TTMonitorRequest ttMonitorRequest) {
        LOG.info("Searching tee times for priority courses");
        // TODO: starter wants users search pref ID. so that means that we need to get it from headers,
        //  also this requires user to have saved a search criteria prior
        return starter.startTTSearchWf(ttMonitorRequest);
    }
}
