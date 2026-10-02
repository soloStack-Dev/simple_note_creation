package com.example.demo.Security;

import org.springframework.http.ResponseCookie.ResponseCookieBuilder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DeferredCsrfToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Consumer;

/**
 * A {@link CsrfTokenRepository} that stores the token in a cookie and never clears it.
 *
 * <p>Spring Security's default repository keeps the token in the HttpSession. This app is
 * {@link org.springframework.security.config.http.SessionCreationPolicy#STATELESS}, so the session
 * is never persisted - every response carries a fresh JSESSIONID - and the token minted on page
 * load lives in a session that is gone by the time the browser posts.
 *
 * <p>The stock {@link CookieCsrfTokenRepository} fixes that, but has the opposite problem: it
 * honours {@code saveToken(null, ...)} by expiring the cookie, and Spring Security calls that
 * whenever it believes the authenticated identity changed. Here
 * {@link TokenAuthenticationFilter} rebuilds the authentication on every request from the token
 * cookie, so that happens on every request. The response to any mutation came back with
 * {@code XSRF-TOKEN=; Expires=1970}, and the next mutation 403'd - a note could be written once
 * and never again.
 *
 * <p>Rotating the token on an identity change is the right rule for a session-bound app, where the
 * session is what ties the token to a principal. This app is stateless: the identity lives in an
 * HttpOnly cookie the browser never exposes to script, and the CSRF token in a second, readable
 * cookie proves only that the request came from a page this app rendered. There is nothing for a
 * rotation to protect, and rotating it only breaks the open tab holding a stale copy.
 *
 * <p>A forged token still fails. The cookie is the only place a token ever comes from, so a value
 * the attacker has to guess is a value the server rejects; clearing the cookie buys nothing they
 * do not already have to defeat.
 */
public class PersistentCookieCsrfTokenRepository implements CsrfTokenRepository {

    private final CookieCsrfTokenRepository delegate = CookieCsrfTokenRepository.withHttpOnlyFalse();

    public PersistentCookieCsrfTokenRepository() {
    }

    /** Forwards cookie attributes such as SameSite and path to the underlying repository. */
    public void setCookieCustomizer(Consumer<ResponseCookieBuilder> customizer) {
        delegate.setCookieCustomizer(customizer);
    }

    @Override
    public DeferredCsrfToken loadDeferredToken(HttpServletRequest request, HttpServletResponse response) {
        return delegate.loadDeferredToken(request, response);
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        return delegate.loadToken(request);
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return delegate.generateToken(request);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token == null) {
            // Would normally expire the cookie. Ignoring it is the entire point of this class.
            return;
        }
        delegate.saveToken(token, request, response);
    }
}