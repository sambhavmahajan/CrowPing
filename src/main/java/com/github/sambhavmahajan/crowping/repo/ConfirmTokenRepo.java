package com.github.sambhavmahajan.crowping.repo;

import com.github.sambhavmahajan.crowping.security.ConfirmToken;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConfirmTokenRepo extends JpaRepository<ConfirmToken, String> {
    @Cacheable(value="tokens", key="#token")
    Optional<ConfirmToken> findByToken(String token);
    void deleteAllByExpiryDateBefore(LocalDateTime now);
    void deleteAllByVerifiedTrue();
}
