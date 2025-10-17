package com.hooswhere.letsgogolfing.dto;

import java.time.Duration;
import java.time.Instant;

public record AuthTokens(
        String userToken,                    // Most critical - user session JWT
        String requestVerificationToken,     // CSRF token (header + cookie must match)
        String aspNetSessionId,              // ASP.NET session
        String cmsAuth,                      // CMS authentication
        String cfClearance,                  // Cloudflare clearance
        String cfBm,                         // Cloudflare bot management
        String cfLb,                         // Cloudflare load balancer
        String cfRuid,                       // Cloudflare request UID
        String cfUvid,                       // Cloudflare visitor ID
        String gnusUserId,                   // GolfNow user GUID
        String optanonConsent,               // GDPR consent
        String optanonAlertBoxClosed,        // GDPR alert timestamp
        String oneTrustWPCCPAGoogleOptOut,   // Privacy setting
        String otgppConsent,                 // GPP consent
        String usprivacy,                    // US privacy string
        Instant acquiredAt,                  // When tokens were obtained
        Instant expiresAt                    // Estimated expiration (e.g., +2 hours)
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isExpiringSoon(Duration threshold) {
        return Instant.now().plus(threshold).isAfter(expiresAt);
    }

    /**
     * Build a Cookie header string from all tokens for HTTP requests
     */
    public String toCookieString() {
        return String.format(
            "user-token=%s; __RequestVerificationToken=%s; ASP.NET_SessionId=%s; " +
            "_CMSAUTH=%s; cf_clearance=%s; __cf_bm=%s; __cflb=%s; __cfruid=%s; " +
            "_cfuvid=%s; gnus_userId=%s; OptanonConsent=%s; " +
            "OptanonAlertBoxClosed=%s; OneTrustWPCCPAGoogleOptOut=%s; " +
            "OTGPPConsent=%s; usprivacy=%s",
            nullSafe(userToken),
            nullSafe(requestVerificationToken),
            nullSafe(aspNetSessionId),
            nullSafe(cmsAuth),
            nullSafe(cfClearance),
            nullSafe(cfBm),
            nullSafe(cfLb),
            nullSafe(cfRuid),
            nullSafe(cfUvid),
            nullSafe(gnusUserId),
            nullSafe(optanonConsent),
            nullSafe(optanonAlertBoxClosed),
            nullSafe(oneTrustWPCCPAGoogleOptOut),
            nullSafe(otgppConsent),
            nullSafe(usprivacy)
        );
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
