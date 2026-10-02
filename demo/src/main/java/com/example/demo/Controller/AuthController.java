package com.example.demo.Controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Model.AuthSource;
import com.example.demo.Security.TokenCookieService;
import com.example.demo.Security.TokenService;
import com.example.demo.Service.AuthService;
import com.example.demo.Service.ErrorLogService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Sign-up, login pages and profile editing.
 *
 * <p>Sign-in itself is handled by Spring Security's form-login filter at POST /login; this
 * controller only renders that form. Sign-up has no filter behind it, so it issues the auth
 * token itself and sends the new user to the home page already signed in.
 */
@Controller
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final TokenCookieService cookieService;
    private final ErrorLogService errorLogService;

    public AuthController(AuthService authService, TokenService tokenService,
            TokenCookieService cookieService, ErrorLogService errorLogService) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.cookieService = cookieService;
        this.errorLogService = errorLogService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String returnUrl,
            Model model) {
        model.addAttribute("pageTitle", "Log In");
        model.addAttribute("loginFailed", error != null);
        model.addAttribute("username", username);
        model.addAttribute("returnUrl", safeReturnUrl(returnUrl));
        return "pages/login";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("pageTitle", "Sign Up");
        if (!model.containsAttribute("errors")) {
            model.addAttribute("errors", List.of());
        }
        return "pages/signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String bio,
            Model model,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        List<String> errors = authService.validateRegistration(username, email, password);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("bio", bio);
            return "pages/signup";
        }

        AuthSource user = null;
        try {
            user = authService.register(username, email, password);
            if (bio != null && !bio.isBlank()) {
                authService.updateProfile(user, null, bio);
            }
        } catch (IllegalStateException ex) {
            model.addAttribute("errors", List.of(ex.getMessage()));
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("bio", bio);
            return "pages/signup";
        } catch (IllegalArgumentException ex) {
            // The account exists by now, so this is a bad bio rather than a bad signup.
            // Send them straight to the profile panel to fix it.
            cookieService.addCookie(response, tokenService.issue(user));
            redirectAttributes.addFlashAttribute("profileError", ex.getMessage());
            return "redirect:/";
        }

        cookieService.addCookie(response, tokenService.issue(user));
        errorLogService.record(
                "Sign-up succeeded",
                "Registered username '" + user.getUsername() + "' (" + user.getEmail() + ")",
                "Row written to AuthSource with a BCrypt password digest.",
                "Auth token issued immediately so the new user lands signed in.");
        redirectAttributes.addFlashAttribute("justSignedUp", true);
        return "redirect:/";
    }

    private String safeReturnUrl(String returnUrl) {
        if (returnUrl == null || returnUrl.isBlank() || !returnUrl.startsWith("/")
                || returnUrl.startsWith("//")) {
            return null;
        }
        return returnUrl;
    }
}
