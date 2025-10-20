package com.hooswhere.letsgogolfing.golfnow;

import com.hooswhere.letsgogolfing.dto.TeeTimeSlot;

public final class GolfNowUtil {

    /**
     * Booking URL would look something like this:
     * /tee-times/facility/4817/tee-time/1867224724/checkout/players/4
     * @param slot
     * @return
     */
    public static String generateBookingUrl(TeeTimeSlot slot, int numPlayers) {
        return String.format("https://www.golfnow.com%s/checkout/players/%d",
                             slot.detailUrl(), numPlayers);
    }
}
