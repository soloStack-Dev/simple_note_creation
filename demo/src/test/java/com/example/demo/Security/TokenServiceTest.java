package com.example.demo.Security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
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

import com.example.demo.Model.AuthSource;
import com.example.demo.Repository.AuthSourceRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-02T03:04:05Z");
    private static final Duration TTL = Duration.ofHours(8);

    @Mock
    private AuthSourceRepository repository;

    private TokenService tokenService;
    private AuthSource user;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(repository, Clock.fixed(NOW, ZoneOffset.UTC), TTL);
        user = new AuthSource("someone", "a@b.com", "hash", null);
        when(repository.save(any(AuthSource.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void issueStoresOnlyADigestAndNeverTheRawToken() {
        String raw = tokenService.issue(user);

        assertThat(raw).isNotBlank();
        assertThat(user.getTokenHash()).isNotBlank().isNotEqualTo(raw);
        assertThat(user.getTokenExpiresAt()).isEqualTo(NOW.plus(TTL));
    }

    @Test
    void issueProducesADifferentTokenEveryTime() {
        assertThat(tokenService.issue(user)).isNotEqualTo(tokenService.issue(user));
    }

    @Test
    void resolveRejectsAnAbsentToken() {
        assertThat(tokenService.resolve(null)).isEmpty();
        assertThat(tokenService.resolve("  ")).isEmpty();
    }

    @Test
    void resolveReturnsTheUserForALiveToken() {
        when(repository.findByTokenHashAndTokenExpiresAtAfter(anyString(), any(Instant.class)))
                .thenReturn(Optional.of(user));

        assertThat(tokenService.resolve(tokenService.issue(user))).contains(user);
    }

    @Test
    void revokeClearsTheStoredCredential() {
        tokenService.issue(user);

        tokenService.revoke(user);

        assertThat(user.getTokenHash()).isNull();
        assertThat(user.getTokenExpiresAt()).isNull();
    }

    @Test
    void revokeByUsernameIsANoOpForAnUnknownUser() {
        when(repository.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());

        tokenService.revokeByUsername("nobody");
    }
}
