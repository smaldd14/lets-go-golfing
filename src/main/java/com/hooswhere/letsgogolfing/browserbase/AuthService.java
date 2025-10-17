package com.hooswhere.letsgogolfing.browserbase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hooswhere.letsgogolfing.dto.AuthTokens;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class AuthService {
    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);
    private final BrowserBaseClient client;
    private final BBConfigProps configProps;

    private final ObjectMapper objectMapper;

    private static final Playwright playwright = Playwright.create();
    public AuthService(BrowserBaseClient client, BBConfigProps configProps, ObjectMapper objectMapper) {
        this.client = client;
        this.configProps = configProps;
        this.objectMapper = objectMapper;
    }

    /*
    1. Create BrowserBase session
    2. Connect via Playwright
    3. Navigate to GolfNow login page
    4. Perform login flow (steps outlined above)
    5. Extract cookies and CSRF token
    6. Close browser session
    7. Return `AuthTokens` with expiration metadata
     */
    public AuthTokens authenticate() throws Exception {
        BrowserBaseSession session = client.createSession();
        try (Browser browser = playwright.chromium().connectOverCDP(session.connectUrl())) {
            return performLogin(browser, configProps.email(), configProps.password());
        } catch(Exception e) {
            LOG.error("Error during authentication", e);
            throw e;
        } finally {
            LOG.info("Cleaning up BrowserBase session");
            if (session != null) {
                client.stopSession(session.id());
            }
        }
    }

    private Map<String, Object> readCachedCookies() {
        try {
            String json = Files.readString(Path.of("cookiemap-unauth.json"));
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            return map;
        } catch (IOException e) {
            LOG.error("Error reading cached cookies", e);
            return new HashMap<>();
        }
    }

    public Map<String, Object> getCookies() throws Exception {

        Map<String, Object> cachedCookies = readCachedCookies();
        // TODO: check expiration of cached cookies
        if (cachedCookies != null && !cachedCookies.isEmpty()) {
            LOG.info("Using cached cookies");
            return cachedCookies;
        }

        BrowserBaseSession session = client.createSession();
        try (Browser browser = playwright.chromium().connectOverCDP(session.connectUrl())) {
            Page page = browser.newPage();
            // Navigate to GolfNow homepage
            LOG.info("Navigating to GolfNow homepage");
            page.navigate("https://www.golfnow.com");
            Page.GetByRoleOptions searchBoxOptions = new Page.GetByRoleOptions()
                    .setName("Enter Course, City, or Postal");
            page.getByRole(AriaRole.TEXTBOX,
                           searchBoxOptions).click();

            page.getByRole(AriaRole.TEXTBOX,
                           searchBoxOptions).fill("hoboken");

            page.getByRole(AriaRole.TEXTBOX,
                           searchBoxOptions).press("Enter");
            return extractCookies(page);
        } catch(Exception e) {
            LOG.error("Error during getCookies", e);
            throw e;
        } finally {
            LOG.info("Cleaning up BrowserBase session");
            if (session != null) {
                client.stopSession(session.id());
            }
        }
    }

    /**
     * Perform the actual login flow and extract tokens
     */
    private AuthTokens performLogin(Browser browser, String email, String password) {
        Page page = browser.newPage();
        try {
            // Navigate to GolfNow homepage
            LOG.info("Navigating to GolfNow homepage");
            page.navigate("https://www.golfnow.com");

            // 2. Handle dynamic popup iframe (optional)
            try {
                FrameLocator popupFrame = page.frameLocator("iframe[name^='ju_iframe_']");
                popupFrame.locator("html").click(new Locator.ClickOptions().setTimeout(2000));
            } catch (TimeoutError e) {
                // Popup didn't appear, continue
                LOG.info("No popup iframe detected, continuing...");
            }

            // 3. Click Log In button
            page.getByText("Log In").click();

            // 4. Switch to OAuth iframe
            FrameLocator oauthFrame = page.locator("#golfid-oauth-frame").contentFrame();

            // 5. Fill email
            oauthFrame.getByRole(AriaRole.TEXTBOX,
                 new FrameLocator.GetByRoleOptions()
                    .setName("Email Address"))
                    .fill(email);

            // 6. Click Continue
            oauthFrame.getByRole(AriaRole.BUTTON,
                 new FrameLocator.GetByRoleOptions()
                    .setName("Continue"))
                    .click();

            // 7. Fill password
            oauthFrame.getByRole(AriaRole.TEXTBOX,
                new FrameLocator.GetByRoleOptions()
                    .setName("Password*"))
                    .fill(password);

            // 8. Submit login
            oauthFrame.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions()
                            .setName("Login"))
                    .click();

            // 9. Wait for redirect
            page.waitForURL("https://www.golfnow.com/", new Page.WaitForURLOptions()
                    .setTimeout(30000));

//                    .fill(password);
//            await page.getByRole('textbox', { name: 'Enter Course, City, or Postal' }).click();
//            await page.getByRole('textbox', { name: 'Enter Course, City, or Postal' }).fill('hoboken');
//            await page.getByRole('textbox', { name: 'Enter Course, City, or Po

            // tODO: probably shouldn't return authTokens from an activity, or encrypt
            return extractAuthTokens(page);

        } finally {
            page.close();
        }
    }

    private Map<String, Object> extractCookies(Page page) {
        // Get all cookies
        List<Cookie> cookies = page.context().cookies();

        Map<String, Object> cookieMap = new HashMap<>();
        for (Cookie cookie : cookies) {
            cookieMap.put(cookie.name, cookie.value);
        }

        try {
            LOG.info("cookieMap: {}", cookieMap);
            //TODO: save cookies so that can open page with context later
            Files.write(Path.of("/Users/devinsmaldore/code/developer/java-projects/letsgogolfing/cookiemap-unauth.json"),
                        objectMapper.writeValueAsString(cookieMap).getBytes());
        } catch (Exception e) {
            LOG.error("Error serializing cookies to JSON", e);
        }

        return cookieMap;
    }

    /**
     * Extract all authentication tokens from the browser page
     */
    private AuthTokens extractAuthTokens(Page page) {
        // Get all cookies
        List<Cookie> cookies = page.context().cookies();
        try {
            LOG.info("cookies: {}", cookies);
            //TODO: save cookies so that can open page with context later
            Files.write(Path.of("/Users/devinsmaldore/code/developer/java-projects/letsgogolfing/cookies-unuauth.json"),
                        objectMapper.writeValueAsString(cookies).getBytes());
        } catch (Exception e) {
            LOG.error("Error serializing cookies to JSON", e);
        }

        Map<String, String> cookieMap = new HashMap<>();
        for (Cookie cookie : cookies) {
            cookieMap.put(cookie.name, cookie.value);
        }

        LOG.info("cookiemap: {}", cookieMap);

        // Extract CSRF token from page (if present in hidden input)
        String csrfToken = page.evaluate("() => {" +
                                         "const input = document.querySelector('input[name=\"__RequestVerificationToken\"]');" +
                                         "return input ? input.value : null;" +
                                         "}").toString();

        // If not in page, check cookie
        if (csrfToken == null) {
            csrfToken = cookieMap.get("__RequestVerificationToken");
        }

        LOG.info("csrfToken: {}", csrfToken);

        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofHours(2)); // Conservative 2-hour expiration

        return new AuthTokens(
                cookieMap.get("user-token"),
                csrfToken,
                cookieMap.get("ASP.NET_SessionId"),
                cookieMap.get("_CMSAUTH"),
                cookieMap.get("cf_clearance"),
                cookieMap.get("__cf_bm"),
                cookieMap.get("__cflb"),
                cookieMap.get("__cfruid"),
                cookieMap.get("_cfuvid"),
                cookieMap.get("gnus_userId"),
                cookieMap.get("OptanonConsent"),
                cookieMap.get("OptanonAlertBoxClosed"),
                cookieMap.get("OneTrustWPCCPAGoogleOptOut"),
                cookieMap.get("OTGPPConsent"),
                cookieMap.get("usprivacy"),
                now,
                expiresAt
        );
    }
}
