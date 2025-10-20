package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.ScheduleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@RequestMapping("/api/tee-times/schedules")
@Tag(name = "Tee Time Schedules", description = "APIs for managing recurring tee time search schedules")
public interface TeeTimeScheduleApi {

    @Operation(
            summary = "Create a recurring tee time search schedule",
            description = "Creates a Temporal schedule that periodically searches for available tee times. " +
                         "The schedule ID is derived from the user's email to prevent duplicate schedules. " +
                         "Only one active schedule per user is allowed."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Schedule created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid schedule request parameters"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Schedule already exists for this user"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping
    ResponseEntity<String> createSchedule(
            @Parameter(
                    description = "Schedule configuration including email, interval, and search criteria",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ScheduleRequest.class))
            )
            @RequestBody ScheduleRequest scheduleRequest
    ) throws Exception;

    @Operation(
            summary = "Delete a tee time search schedule",
            description = "Deletes a Temporal schedule for the specified user email. " +
                         "The schedule will no longer execute and all associated data will be removed."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Schedule deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Schedule not found for this user"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @DeleteMapping("/{email}")
    ResponseEntity<Void> deleteSchedule(
            @Parameter(
                    description = "User email associated with the schedule to delete",
                    required = true
            )
            @PathVariable String email
    ) throws Exception;
}
