package com.example.demo.Security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Sends an anonymous visitor to the sign-in page and remembers where they were going.
 *
 * <p>Spring Security's default entry point drops the original URL, which strands anyone who
 * clicks a protected link before signing in. Carrying it as {@code returnUrl} lets
 * {@code AuthController} put it back on the form and
 * {@link TokenAuthenticationSuccessHandler} use it afterwards.
 */
@Component
public class LoginRedirectEntryPoint implements AuthenticationEntryPoint {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        String target = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null && !query.isBlank()) {
            target = target + "?" + query;
        }

        // The URL is context-relative: RedirectStrategy prepends the context path itself, and
        // passing an absolute one would double it up under anything but "/".
        String loginUrl = "/login?returnUrl=" + URLEncoder.encode(target, StandardCharsets.UTF_8);
        redirectStrategy.sendRedirect(request, response, loginUrl);
    }
}
