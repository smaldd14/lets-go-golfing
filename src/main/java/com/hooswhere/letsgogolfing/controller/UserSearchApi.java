package com.hooswhere.letsgogolfing.controller;

import com.hooswhere.letsgogolfing.dto.CreateUserSearchRequest;
import com.hooswhere.letsgogolfing.dto.UserSearchPreferenceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@Tag(name = "User Search Preferences", description = "APIs for managing user tee time search preferences and schedules")
@RequestMapping("/api/user/searches")
public interface UserSearchApi {

    @Operation(
            summary = "Create a new search preference",
            description = "Creates a new user search preference with specified criteria. " +
                         "This will find or create the search criteria (deduplicating if it already exists), " +
                         "then create a user preference record. " +
                         "The preference can later be used to start a Temporal schedule for automated monitoring."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully created search preference",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserSearchPreferenceDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping
    UserSearchPreferenceDto createSearch(
            @Parameter(
                    description = "Search preference details including email, search criteria, and schedule settings",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateUserSearchRequest.class))
            )
            @RequestBody CreateUserSearchRequest request
    );

    @Operation(
            summary = "Get all active searches for a user",
            description = "Returns all active search preferences for the specified user email. " +
                         "Includes full search criteria details and schedule information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user searches",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserSearchPreferenceDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email parameter"
            )
    })
    @GetMapping
    List<UserSearchPreferenceDto> getUserSearches(
            @Parameter(
                    description = "User email address",
                    required = true,
                    example = "user@example.com"
            )
            @RequestParam String email
    );

    @Operation(
            summary = "Get a specific search preference",
            description = "Returns detailed information about a specific search preference by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved search preference",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserSearchPreferenceDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Search preference not found"
            )
    })
    @GetMapping("/{id}")
    UserSearchPreferenceDto getSearch(
            @Parameter(
                    description = "Search preference UUID",
                    required = true
            )
            @PathVariable UUID id
    );

    @Operation(
            summary = "Delete a search preference",
            description = "Deactivates a search preference (marks as inactive). " +
                         "This should also stop any associated Temporal schedules. " +
                         "Does not delete the record from the database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully deactivated search preference"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Search preference not found"
            )
    })
    @DeleteMapping("/{id}")
    void deleteSearch(
            @Parameter(
                    description = "Search preference UUID",
                    required = true
            )
            @PathVariable UUID id
    );
}
