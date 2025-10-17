package com.hooswhere.letsgogolfing.golfnow;

import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for working with HTTP clients and cookies.
 */
public class HttpClientUtils {

    /**
     * Converts a map of cookies into a Cookie header string.
     *
     * @param cookies Map of cookie names to cookie values
     * @return Cookie header string in the format "name1=value1; name2=value2"
     */
    public static String buildCookieHeader(Map<String, Object> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return "";
        }

        return cookies.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; "));
    }

    /**
     * Adds cookies from a map to Spring's HttpHeaders object.
     *
     * @param headers The HttpHeaders object to add cookies to
     * @param cookies Map of cookie names to cookie values
     */
    public static void addCookiesToHeaders(HttpHeaders headers, Map<String, Object> cookies) {
        if (cookies != null && !cookies.isEmpty()) {
            headers.set(HttpHeaders.COOKIE, buildCookieHeader(cookies));
        }
    }

    /**
     * Creates HttpHeaders with all required headers for GolfNow API calls.
     *
     * @param cookies Map of cookie names to cookie values (must include __RequestVerificationToken)
     * @return Configured HttpHeaders object
     */
    public static HttpHeaders createGolfNowHeaders(Map<String, Object> cookies) {
        HttpHeaders headers = new HttpHeaders();

        // Add cookies
        addCookiesToHeaders(headers, cookies);

        // Extract CSRF token from cookies
        Object csrfToken = cookies.get("__RequestVerificationToken");
        if (csrfToken != null) {
            headers.set("__requestverificationtoken", csrfToken.toString());
        }

        // Add required headers
        headers.set(HttpHeaders.ACCEPT, "application/json, text/javascript, */*; q=0.01");
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
        headers.set(HttpHeaders.ORIGIN, "https://www.golfnow.com");
        headers.set(HttpHeaders.REFERER, "https://www.golfnow.com/tee-times/courses-near-me");
        headers.set(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("x-requested-with", "XMLHttpRequest");

        return headers;
    }
}
