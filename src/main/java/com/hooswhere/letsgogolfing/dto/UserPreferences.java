package com.hooswhere.letsgogolfing.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPreferences(@NotBlank String email,
                              boolean paymentEnabled,
                              SearchCriteria searchCriteria) {}
