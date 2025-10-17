package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.browserbase.AuthService;
import com.hooswhere.letsgogolfing.browserbase.BBConfigProps;
import com.hooswhere.letsgogolfing.browserbase.BrowserBaseClient;
import com.hooswhere.letsgogolfing.browserbase.BrowserBaseSession;
import com.hooswhere.letsgogolfing.dto.AuthTokens;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import io.temporal.activity.Activity;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Temporal activity that performs GolfNow authentication using BrowserBase + Playwright
 */

@Component
@ActivityImpl(taskQueues = "golfnow")
public class BBAuthActivityImpl implements BBAuthActivity {
    private final AuthService authService;

    public BBAuthActivityImpl(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticate to GolfNow and extract auth tokens
     * @return AuthTokens containing all cookies and CSRF token
     */
    public AuthTokens authenticate() throws Exception {
        return authService.authenticate();
    }
}
