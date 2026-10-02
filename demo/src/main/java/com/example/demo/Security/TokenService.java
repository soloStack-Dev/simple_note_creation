package com.example.demo.Security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.AuthSource;
import com.example.demo.Repository.AuthSourceRepository;

/**
 * Issues, resolves and revokes the bearer tokens stored on AuthSource.
 *
 * <p>Token flow: a 32-byte random value is generated, its SHA-256 digest plus an expiry are
 * written to the database, and only the raw value goes to the browser. Each successful
 * sign-in issues a fresh token, so an older one stops working immediately.
 */
@Service
public class TokenService {

    private static final int TOKEN_BYTES = 32;

    private final AuthSourceRepository repository;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Duration ttl;

    public TokenService(AuthSourceRepository repository, Clock clock,
            @Value("${app.security.token.ttl:PT8H}") Duration ttl) {
        this.repository = repository;
        this.clock = clock;
        this.ttl = ttl;
    }

    /**
     * @return the raw token, which is the only copy the caller may hand to the client.
     */
    @Transactional
    public String issue(AuthSource user) {
        String rawToken = newToken();
        user.setTokenHash(hash(rawToken));
        user.setTokenExpiresAt(Instant.now(clock).plus(ttl));
        repository.save(user);
        return rawToken;
    }

    @Transactional(readOnly = true)
    public Optional<AuthSource> resolve(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return repository.findByTokenHashAndTokenExpiresAtAfter(hash(rawToken), Instant.now(clock));
    }

    @Transactional
    public void revoke(AuthSource user) {
        user.setTokenHash(null);
        user.setTokenExpiresAt(null);
        repository.save(user);
    }

    @Transactional
    public void revokeByUsername(String username) {
        repository.findByUsernameIgnoreCase(username).ifPresent(this::revoke);
    }

    private String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }
}
