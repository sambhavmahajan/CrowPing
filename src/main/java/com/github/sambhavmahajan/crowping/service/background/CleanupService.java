package com.github.sambhavmahajan.crowping.service.background;

import com.github.sambhavmahajan.crowping.repo.ConfirmTokenRepo;
import com.github.sambhavmahajan.crowping.security.ConfirmToken;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleanupService {
    private final ConfirmTokenRepo repo;
    public CleanupService(ConfirmTokenRepo repo) {
        this.repo = repo;
    }
    @Scheduled(fixedDelay = 86400000)
    @Transactional
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        repo.deleteAllByExpiryDateBefore(now);
        repo.deleteAllByVerifiedTrue();
    }
}
