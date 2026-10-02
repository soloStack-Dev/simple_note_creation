package com.example.demo.Repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.AuthSource;

public interface AuthSourceRepository extends JpaRepository<AuthSource, Long> {

    Optional<AuthSource> findByUsernameIgnoreCase(String username);

    Optional<AuthSource> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<AuthSource> findByTokenHashAndTokenExpiresAtAfter(String tokenHash, Instant now);
}
