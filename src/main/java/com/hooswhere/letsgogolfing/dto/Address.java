package com.hooswhere.letsgogolfing.dto;

public record Address(
        String line1,
        String line2,
        String city,
        String stateProvinceCode,      // "NJ"
        String postalCode,
        String country,                // "US"
        String stateProvince           // "New Jersey"
) {}