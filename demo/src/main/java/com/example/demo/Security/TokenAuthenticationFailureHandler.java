package com.example.demo.Security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.Service.ErrorLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Records failed sign-in attempts in ErrorEnquiry, then bounces back to the login page.
 */
@Component
public class TokenAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ErrorLogService errorLogService;

    public TokenAuthenticationFailureHandler(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        String username = request.getParameter("username");
        errorLogService.record(
                "Sign-in failed",
                "Rejected sign-in for username '" + username + "': " + exception.getMessage(),
                "Username not found or password did not match an AuthSource row.",
                "User retries the form; the account itself is unaffected.");

        // returnUrl is carried through so a failed attempt at a protected page comes
        // back there after the retry succeeds.
        UriComponentsBuilder target = UriComponentsBuilder.fromPath("/login")
                .queryParam("error", true)
                .queryParam("username", username);

        String returnUrl = request.getParameter("returnUrl");
        if (returnUrl != null && returnUrl.startsWith("/") && !returnUrl.startsWith("//")) {
            target.queryParam("returnUrl", returnUrl);
        }
        response.sendRedirect(target.build().toUriString());
    }
}
