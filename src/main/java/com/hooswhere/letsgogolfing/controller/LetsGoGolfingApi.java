package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.FacilitySummary;
import com.hooswhere.letsgogolfing.dto.SearchCriteria;
import com.hooswhere.letsgogolfing.dto.TTMonitorRequest;
import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Validated
@Tag(name = "Golf Tee Times", description = "APIs for searching golf facilities and available tee times")
public interface LetsGoGolfingApi {

    @Operation(
            summary = "Search for golf facilities",
            description = "Returns a list of golf facilities matching the search criteria. " +
                         "Users can browse these results and select which facilities to prioritize for tee time searches. " +
                         "This endpoint makes only one API call to GolfNow and does not fetch specific tee times."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of facilities",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FacilitySummary.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search criteria provided"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error or GolfNow API error"
            )
    })
    @PostMapping("/api/facilities/search")
    List<FacilitySummary> searchFacilities(
            @Parameter(
                    description = "Search criteria for finding golf facilities. " +
                                 "Includes location (lat/lon), radius, date, and preferences.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SearchCriteria.class))
            )
            @RequestBody SearchCriteria searchCriteria
    ) throws Exception;

    @Operation(
            summary = "Search for available tee times",
            description = "Fetches specific bookable tee times for priority golf facilities. " +
                         "IMPORTANT: Specify priorityCourseIds (3-5 facilities) in the search criteria to limit API calls. " +
                         "Returns 18-hole tee times within the specified time preferences. " +
                         "Each tee time includes facility details, time slot, and pricing information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved available tee times",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TeeTimeSlot.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user preferences or missing priority course IDs"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error or GolfNow API error"
            )
    })
    @PostMapping("/api/tee-times/search")
    List<TeeTimeSlot> searchTeeTimes(
            @Parameter(
                    description = "User preferences including email and search criteria. " +
                                 "Must include priorityCourseIds (list of 3-5 facility IDs) to avoid excessive API calls.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TTMonitorRequest.class))
            )
            @RequestBody TTMonitorRequest request
    ) throws Exception;
}
