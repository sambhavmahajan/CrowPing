package com.github.sambhavmahajan.crowping.repo;

import com.github.sambhavmahajan.crowping.entity.AppUser;
import com.github.sambhavmahajan.crowping.entity.PingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PingLogRepo extends JpaRepository<PingLog, Long> {
    List<PingLog> findAllByOwnerEmail(String email);
    Optional<PingLog> findByOwnerEmailAndUrl(String email, String url);
}
