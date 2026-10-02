package com.example.demo.Security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.Model.AuthSource;
import com.example.demo.Repository.AuthSourceRepository;
import com.example.demo.Service.ErrorLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * On successful sign-in: mint a fresh token, put it in the cookie, then redirect.
 *
 * <p>Redirect target is the {@code returnUrl} request parameter when present, so a deep link
 * bounced to the login page by {@link LoginRedirectEntryPoint} lands back where the user was
 * headed. External or protocol-relative values are ignored to avoid an open redirect.
 */
@Component
public class TokenAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthSourceRepository repository;
    private final TokenService tokenService;
    private final TokenCookieService cookieService;
    private final ErrorLogService errorLogService;

    public TokenAuthenticationSuccessHandler(AuthSourceRepository repository, TokenService tokenService,
            TokenCookieService cookieService, ErrorLogService errorLogService) {
        this.repository = repository;
        this.tokenService = tokenService;
        this.cookieService = cookieService;
        this.errorLogService = errorLogService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        String username = authentication.getName();
        repository.findByUsernameIgnoreCase(username).ifPresent(user -> {
            cookieService.addCookie(response, tokenService.issue(user));
            errorLogService.record(
                    "Sign-in succeeded",
                    "User '" + username + "' authenticated from " + request.getRemoteAddr(),
                    "Token issued and stored as a SHA-256 digest on AuthSource.",
                    null);
        });

        response.sendRedirect(resolveTarget(request));
    }

    static String resolveTarget(HttpServletRequest request) {
        String returnUrl = request.getParameter("returnUrl");
        if (isSafeLocalPath(returnUrl)) {
            return request.getContextPath() + returnUrl;
        }
        return request.getContextPath() + "/";
    }

    private static boolean isSafeLocalPath(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String decoded = value.trim();
        if (!decoded.startsWith("/") || decoded.startsWith("//")) {
            return false;
        }
        return !decoded.toLowerCase().startsWith("/\\");
    }
}
