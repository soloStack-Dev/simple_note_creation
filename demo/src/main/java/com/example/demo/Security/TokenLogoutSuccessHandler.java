package com.example.demo.Security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * On sign-out: revoke the stored token, clear the cookie, return to the home page.
 */
@Component
public class TokenLogoutSuccessHandler implements LogoutSuccessHandler {

    private final TokenService tokenService;
    private final TokenCookieService cookieService;

    public TokenLogoutSuccessHandler(TokenService tokenService, TokenCookieService cookieService) {
        this.tokenService = tokenService;
        this.cookieService = cookieService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        if (authentication != null) {
            tokenService.revokeByUsername(authentication.getName());
        }
        cookieService.clearCookie(response);
        response.sendRedirect("/");
    }
}
