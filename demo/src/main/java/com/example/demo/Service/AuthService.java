package com.example.demo.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.AuthSource;
import com.example.demo.Repository.AuthSourceRepository;
import com.example.demo.Security.TokenService;

/**
 * Registration and profile maintenance. Password verification on sign-in is handled by
 * Spring Security's DaoAuthenticationProvider; this service only owns the stored credential.
 */
@Service
public class AuthService {

    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    /** Shortest bio the profile panel accepts. Kept in step with minlength in layout.html. */
    public static final int MIN_BIO_LENGTH = 150;

    private final AuthSourceRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final Clock clock;

    public AuthService(AuthSourceRepository repository, PasswordEncoder passwordEncoder,
            TokenService tokenService, Clock clock) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    public Optional<AuthSource> findByUsername(String username) {
        return repository.findByUsernameIgnoreCase(username);
    }

    public List<String> validateRegistration(String username, String email, String rawPassword) {
        List<String> errors = new java.util.ArrayList<>();
        if (isBlank(username)) {
            errors.add("Username is required.");
        } else if (username.trim().length() < 3) {
            errors.add("Username must be at least 3 characters.");
        } else if (repository.existsByUsernameIgnoreCase(username.trim())) {
            errors.add("That username is already taken.");
        }

        if (isBlank(email)) {
            errors.add("Email is required.");
        } else if (!email.trim().matches(EMAIL_PATTERN)) {
            errors.add("Enter a valid email address.");
        } else if (repository.existsByEmailIgnoreCase(email.trim())) {
            errors.add("That email is already registered.");
        }

        if (isBlank(rawPassword)) {
            errors.add("Password is required.");
        } else if (rawPassword.length() < 6) {
            errors.add("Password must be at least 6 characters.");
        }
        return errors;
    }

    @Transactional
    public AuthSource register(String username, String email, String rawPassword) {
        AuthSource user = new AuthSource(
                username.trim(),
                email.trim(),
                passwordEncoder.encode(rawPassword),
                Instant.now(clock));
        try {
            return repository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // Lost a race with a concurrent signup for the same username/email.
            throw new IllegalStateException("That username or email is already registered.", ex);
        }
    }

    @Transactional
    public AuthSource updateProfile(AuthSource user, String email, String bio) {
        if (email != null && !email.isBlank() && !email.trim().equalsIgnoreCase(user.getEmail())) {
            // The form is type="email", but that is only a client-side hint: a hand-crafted POST
            // would otherwise store an unusable address.
            if (!email.trim().matches(EMAIL_PATTERN)) {
                throw new IllegalArgumentException("Enter a valid email address.");
            }
            if (repository.existsByEmailIgnoreCase(email.trim())) {
                throw new IllegalStateException("That email is already registered.");
            }
            user.setEmail(email.trim());
        }
        user.setBio(validateBio(bio));
        return repository.save(user);
    }

    /**
     * The bio is the one place a user writes about themselves at length, so the UI asks for at
     * least {@value #MIN_BIO_LENGTH} characters. Enforced here as well as in the form, because
     * minlength is only a client-side hint.
     */
    public static String validateBio(String bio) {
        String trimmed = bio == null ? "" : bio.trim();
        if (trimmed.length() < MIN_BIO_LENGTH) {
            throw new IllegalArgumentException(
                    "Bio must be at least " + MIN_BIO_LENGTH + " characters.");
        }
        return trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
