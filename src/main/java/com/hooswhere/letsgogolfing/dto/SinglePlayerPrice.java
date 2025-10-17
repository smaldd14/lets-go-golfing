package com.hooswhere.letsgogolfing.dto;

/**
 * Comprehensive pricing breakdown for a single player.
 */
public record SinglePlayerPrice(
        PriceDetail greensFees,
        FeeMessagingDisplayRates feeMessagingDisplayRates
) {

    public record FeeMessagingDisplayRates(
            PriceDetail greensFees,
            PriceDetail totalPrice,
            PriceDetail transactionFeesTotal
    ) {
    }
}
