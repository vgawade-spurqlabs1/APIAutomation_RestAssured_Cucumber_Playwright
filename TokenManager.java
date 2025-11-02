package org.Spurqlabs.Utils;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;

public class TokenManager {
    private static final ThreadLocal<String> tokenThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Long> expiryThreadLocal = new ThreadLocal<>();
    private static final String TOKEN_FILE = "token.json";
    private static final long TOKEN_VALIDITY_SECONDS = 30 * 60; // 30 minutes

    public static String getToken() {
        String token = tokenThreadLocal.get();
        Long expiry = expiryThreadLocal.get();
        if (token == null || expiry == null || Instant.now().getEpochSecond() >= expiry) {
            // Try to read from a file (for multi-JVM/CI)
            Map<String, Object> fileToken = readTokenFromFile();
            if (fileToken != null) {
                token = (String) fileToken.get("token");
                expiry = ((Number) fileToken.get("expiry")).longValue();
            }
            // If still null or expired, fetch new
            if (token == null || expiry == null || Instant.now().getEpochSecond() >= expiry) {
                Map<String, Object> newToken = generateAuthTokenViaBrowser();
                token = (String) newToken.get("token");
                expiry = (Long) newToken.get("expiry");
                writeTokenToFile(token, expiry);
            }
            tokenThreadLocal.set(token);
            expiryThreadLocal.set(expiry);
        }
        return token;
    }

    private static Map<String, Object> generateAuthTokenViaBrowser() {
        String bearerToken;
        long expiry = Instant.now().getEpochSecond() + TOKEN_VALIDITY_SECONDS;
        int maxRetries = 2;
        int attempt = 0;
        Exception lastException = null;
        while (attempt < maxRetries) {
            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
                BrowserContext context = browser.newContext();
                Page page = context.newPage();

                // Robust wait for login page to load
                page.navigate(FrameworkConfigReader.getFrameworkConfig("BaseUrl"), new Page.NavigateOptions().setTimeout(60000));
                page.waitForSelector("#email", new Page.WaitForSelectorOptions().setTimeout(20000));
                page.waitForSelector("#password", new Page.WaitForSelectorOptions().setTimeout(20000));
                page.waitForSelector("button[type='submit']", new Page.WaitForSelectorOptions().setTimeout(20000));

                // Fill a login form
                page.fill("#email", FrameworkConfigReader.getFrameworkConfig("DealCreatorEmail"));
                page.fill("#password", FrameworkConfigReader.getFrameworkConfig("DealCreatorPassword"));
                page.waitForSelector("button[type='submit']:not([disabled])", new Page.WaitForSelectorOptions().setTimeout(10000));
                page.click("button[type='submit']");

                // Wait for either dashboard element or flexible URL match
                boolean loggedIn;
                try {
                    page.waitForSelector(".dashboard, .main-content, .navbar, .sidebar", new Page.WaitForSelectorOptions().setTimeout(20000));
                    loggedIn = true;
                } catch (Exception e) {
                    // fallback to URL check
                    try {
                        page.waitForURL(url -> url.startsWith(FrameworkConfigReader.getFrameworkConfig("BaseUrl")), new Page.WaitForURLOptions().setTimeout(30000));
                        loggedIn = true;
                    } catch (Exception ex) {
                        // Both checks failed
                        loggedIn = false;
                    }
                }
                if (!loggedIn) {
                    throw new RuntimeException("Login did not complete successfully: dashboard element or expected URL not found");
                }

                // Extract cookies
                String oauthHMAC = null;
                String oauthExpires = null;
                String token = null;
                for (Cookie cookie : context.cookies()) {
                    switch (cookie.name) {
                        case "OauthHMAC":
                            oauthHMAC = cookie.name + "=" + cookie.value;
                            break;
                        case "OauthExpires":
                            oauthExpires = cookie.name + "=" + cookie.value;
                            if (cookie.expires != null && cookie.expires > 0) {
                                expiry = cookie.expires.longValue();
                            }
                            break;
                        case "BearerToken":
                            token = cookie.name + "=" + cookie.value;
                            break;
                    }
                }
                if (oauthHMAC != null && oauthExpires != null && token != null) {
                    bearerToken = oauthHMAC + ";" + oauthExpires + ";" + token + ";";
                } else {
                    throw new RuntimeException("❗ One or more cookies are missing: OauthHMAC, OauthExpires, BearerToken");
                }
                browser.close();
                Map<String, Object> map = new HashMap<>();
                map.put("token", bearerToken);
                map.put("expiry", expiry);
                return map;
            } catch (Exception e) {
                lastException = e;
                System.err.println("[TokenManager] Login attempt " + (attempt + 1) + " failed: " + e.getMessage());
                attempt++;
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("Failed to generate auth token after " + maxRetries + " attempts", lastException);
    }

    private static void writeTokenToFile(String token, long expiry) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("expiry", expiry);
            String json = new Gson().toJson(map);
            Files.write(Paths.get(TOKEN_FILE), json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Object> readTokenFromFile() {
        try {
            Path path = Paths.get(TOKEN_FILE);
            if (!Files.exists(path)) return null;
            String json = new String(Files.readAllBytes(path));
            return new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
        } catch (IOException e) {
            return null;
        }
    }
}
