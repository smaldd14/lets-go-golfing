package com.hooswhere.letsgogolfing.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record DateInfo(
        String date,                          // ISO 8601: "2026-04-01T07:30:00+00:00"
        String formatted,                     // "7:30"
        String formattedTimeMeridian          // "AM" or "PM"
) {
    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
