package com.example.demo.Security;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads and writes the single auth token cookie.
 *
 * <p>HttpOnly keeps the token out of reach of page scripts; SameSite=Lax stops it riding
 * along on cross-site POSTs while still surviving ordinary top-level navigation.
 */
@Component
public class TokenCookieService {

    private final String cookieName;
    private final Duration maxAge;

    public TokenCookieService(
            @Value("${app.security.token.cookie-name:note_token}") String cookieName,
            @Value("${app.security.token.ttl:PT8H}") Duration ttl) {
        this.cookieName = cookieName;
        this.maxAge = ttl;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void addCookie(HttpServletResponse response, String rawToken) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, rawToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String read(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
