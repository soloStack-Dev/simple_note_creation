package com.example.demo.Security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.Model.AuthSource;

/**
 * Adapts an AuthSource row to Spring Security's UserDetails. Username is the login handle;
 * email and bio are carried along so templates can render the profile without a second query.
 */
public class AppUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final String bio;
    private final boolean enabled;

    public AppUserDetails(AuthSource user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.bio = user.getBio();
        this.enabled = user.isEnabled();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() {
        return bio;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
