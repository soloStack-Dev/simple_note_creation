package com.example.demo.Controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.Model.AuthSource;
import com.example.demo.Security.AppUserDetails;
import com.example.demo.Service.AuthService;

/**
 * Supplies the two values every template needs: whether someone is signed in, and who.
 *
 * <p>Done in one place so pages do not have to repeat the lookup, and so templates can rely on
 * {@code ${authenticated}} instead of reaching into the security context.
 */
@ControllerAdvice(annotations = Controller.class)
public class WebModelAdvice {

    private final AuthService authService;

    public WebModelAdvice(AuthService authService) {
        this.authService = authService;
    }

    @ModelAttribute("authenticated")
    public boolean authenticated() {
        return currentAuthentication() != null;
    }

    @ModelAttribute("currentUsername")
    public String currentUsername() {
        Authentication authentication = currentAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    @ModelAttribute("currentUser")
    public AuthSource currentUser() {
        String username = currentUsername();
        if (username == null) {
            return null;
        }
        return authService.findByUsername(username).orElse(null);
    }

    @ModelAttribute("currentUserId")
    public Long currentUserId() {
        Authentication authentication = currentAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails details)) {
            return null;
        }
        return details.getId();
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication;
    }
}
