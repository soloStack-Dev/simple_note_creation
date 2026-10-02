package com.example.demo.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.Model.AuthSource;
import com.example.demo.Repository.AuthSourceRepository;
import com.example.demo.Security.TokenService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private AuthSourceRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(repository, passwordEncoder, tokenService,
                Clock.fixed(Instant.parse("2026-01-02T03:04:05Z"), ZoneOffset.UTC));
    }

    @Test
    void validateRegistrationRejectsShortUsername() {
        when(repository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);

        assertThat(authService.validateRegistration("ab", "a@b.com", "secret123"))
                .containsExactly("Username must be at least 3 characters.");
    }

    @Test
    void validateRegistrationRejectsMalformedEmail() {
        when(repository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);

        assertThat(authService.validateRegistration("someone", "not-an-email", "secret123"))
                .containsExactly("Enter a valid email address.");
    }

    @Test
    void validateRegistrationRejectsShortPassword() {
        when(repository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);

        assertThat(authService.validateRegistration("someone", "a@b.com", "12345"))
                .containsExactly("Password must be at least 6 characters.");
    }

    @Test
    void validateRegistrationRejectsDuplicateUsername() {
        when(repository.existsByUsernameIgnoreCase("taken")).thenReturn(true);

        assertThat(authService.validateRegistration("taken", "a@b.com", "secret123"))
                .containsExactly("That username is already taken.");
    }

    @Test
    void validateRegistrationAcceptsAValidRegistration() {
        when(repository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(repository.existsByEmailIgnoreCase(anyString())).thenReturn(false);

        assertThat(authService.validateRegistration("someone", "a@b.com", "secret123")).isEmpty();
    }

    @Test
    void updateProfileRefusesAMalformedEmail() {
        AuthSource user = new AuthSource("someone", "old@b.com", "hash", null);

        assertThatThrownBy(() -> authService.updateProfile(user, "not-an-email", "bio"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Enter a valid email address.");

        verify(repository, never()).save(any());
        assertThat(user.getEmail()).isEqualTo("old@b.com");
    }

    @Test
    void updateProfileRefusesAnEmailAnotherUserOwns() {
        AuthSource user = new AuthSource("someone", "old@b.com", "hash", null);
        when(repository.existsByEmailIgnoreCase("taken@b.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.updateProfile(user, "taken@b.com", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("That email is already registered.");
    }

    @Test
    void updateProfileSavesANewEmailAndBio() {
        AuthSource user = new AuthSource("someone", "old@b.com", "hash", null);
        when(repository.existsByEmailIgnoreCase("new@b.com")).thenReturn(false);
        when(repository.save(any(AuthSource.class))).thenAnswer(call -> call.getArgument(0));

        AuthSource saved = authService.updateProfile(user, "new@b.com", bio());

        assertThat(saved.getEmail()).isEqualTo("new@b.com");
        assertThat(saved.getBio()).isEqualTo(bio());
    }

    @Test
    void updateProfileLeavesTheEmailAloneWhenUnchanged() {
        AuthSource user = new AuthSource("someone", "same@b.com", "hash", null);
        when(repository.save(any(AuthSource.class))).thenAnswer(call -> call.getArgument(0));

        authService.updateProfile(user, "SAME@b.com", bio());

        assertThat(user.getEmail()).isEqualTo("same@b.com");
        assertThat(user.getBio()).isEqualTo(bio());
    }

    @Test
    void updateProfileRefusesABioUnderTheMinimumLength() {
        AuthSource user = new AuthSource("someone", "old@b.com", "hash", null);

        assertThatThrownBy(() -> authService.updateProfile(user, null, "too short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bio must be at least " + AuthService.MIN_BIO_LENGTH + " characters.");

        verify(repository, never()).save(any());
    }

    @Test
    void updateProfileRefusesAnEmptyBio() {
        assertThatThrownBy(() -> AuthService.validateBio("   "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AuthService.validateBio(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateBioAcceptsExactlyTheMinimumAndTrimsIt() {
        String exact = "a".repeat(AuthService.MIN_BIO_LENGTH);

        assertThat(AuthService.validateBio("  " + exact + "  ")).isEqualTo(exact);
    }

    /** A bio long enough to clear AuthService.MIN_BIO_LENGTH. */
    private static String bio() {
        return "b".repeat(AuthService.MIN_BIO_LENGTH + 10);
    }

    @Test
    void registerStoresABcryptDigest() {
        when(repository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(repository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("bcrypt-digest");
        when(repository.save(any(AuthSource.class))).thenAnswer(call -> call.getArgument(0));

        AuthSource user = authService.register("someone", "a@b.com", "secret123");

        assertThat(user.getUsername()).isEqualTo("someone");
        assertThat(user.getPassword()).isEqualTo("bcrypt-digest");
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    void findByUsernameIsCaseInsensitive() {
        AuthSource user = new AuthSource("Someone", "a@b.com", "hash", null);
        when(repository.findByUsernameIgnoreCase("someone")).thenReturn(Optional.of(user));

        assertThat(authService.findByUsername("someone")).contains(user);
    }
}
