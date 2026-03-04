package com.github.sambhavmahajan.crowping.repo;

import com.github.sambhavmahajan.crowping.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepo extends JpaRepository<AppUser, String> {
    Optional<AppUser> findByEmail(String email);
}