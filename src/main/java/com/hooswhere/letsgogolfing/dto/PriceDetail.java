package com.hooswhere.letsgogolfing.dto;

/**
 * Detailed price information with formatting.
 */
public record PriceDetail(
        String currencyCode,
        double value,
        String formattedValue,
        String currencySymbol
) {
}
