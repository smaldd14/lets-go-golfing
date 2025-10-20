package com.hooswhere.letsgogolfing.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPreferencesLegacy(@NotBlank String email,
                                    boolean paymentEnabled,
                                    SearchCriteria searchCriteria) {}
