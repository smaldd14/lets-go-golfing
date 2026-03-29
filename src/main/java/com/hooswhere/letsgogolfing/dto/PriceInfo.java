package com.hooswhere.letsgogolfing.dto;

public record PriceInfo(
        String currencyCode,                  // "USD"
        double value,                         // 27.99
        String formattedValue2,               // "$27.99"
        String roundedFormattedValue2,        // "$27.99"
        String roundedSuperScriptFormattedValue
) {}
