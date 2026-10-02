package com.example.demo.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Model.AuthSource;
import com.example.demo.Service.AuthService;
import com.example.demo.Service.ErrorLogService;

/**
 * Saves the profile panel (email + bio) that hangs off the avatar dropdown.
 *
 * <p>A plain form POST with a redirect afterwards, so it still works if htmx is unavailable.
 */
@Controller
public class ProfileController {

    private final AuthService authService;
    private final ErrorLogService errorLogService;

    public ProfileController(AuthService authService, ErrorLogService errorLogService) {
        this.authService = authService;
        this.errorLogService = errorLogService;
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam(required = false) String email,
            @RequestParam(required = false) String bio,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authService.findByUsername(authentication.getName()).isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "Sign in to edit your profile.");
            return "redirect:/login?returnUrl=/";
        }

        AuthSource user = authService.findByUsername(authentication.getName()).orElseThrow();
        try {
            authService.updateProfile(user, email, bio);
            redirectAttributes.addFlashAttribute("profileSaved", true);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            errorLogService.record(
                    "Profile update rejected",
                    ex.getMessage(),
                    "The submitted email was malformed or already used by another AuthSource row.",
                    "User keeps the previous email and can retry.");
            redirectAttributes.addFlashAttribute("profileError", ex.getMessage());
        }
        return "redirect:" + safeTarget(referer);
    }

    private String safeTarget(String referer) {
        if (referer == null || referer.isBlank()) {
            return "/";
        }
        try {
            java.net.URI uri = java.net.URI.create(referer);
            String path = uri.getPath();
            if (path == null || path.isBlank()) {
                return "/";
            }
            String query = uri.getQuery();
            return path + (query == null ? "" : "?" + query);
        } catch (IllegalArgumentException ex) {
            return "/";
        }
    }
}
