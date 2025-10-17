package com.hooswhere.letsgogolfing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;

public record ScheduleRequest(
        @NotBlank String email,
        @NotNull Duration interval,
        @NotNull SearchCriteria searchCriteria,
        boolean paymentEnabled
) {}
