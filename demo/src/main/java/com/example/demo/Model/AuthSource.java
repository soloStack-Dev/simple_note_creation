package com.example.demo.Model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Table AuthSource - one row per registered user, and the only place auth tokens live.
 *
 * <p>The raw token is never stored. {@link #tokenHash} holds a SHA-256 hex digest of the
 * token that was handed to the browser, so a database dump cannot be replayed as a session.
 */
@Entity
@Table(name = "AuthSource")
public class AuthSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** BCrypt digest - never the plain password. */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(length = 500)
    private String bio;

    @Column(name = "tokenHash", length = 64)
    private String tokenHash;

    @Column(name = "tokenExpiresAt")
    private Instant tokenExpiresAt;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean enabled = true;

    public AuthSource() {
    }

    public AuthSource(String username, String email, String password, Instant createdAt) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(Instant tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
