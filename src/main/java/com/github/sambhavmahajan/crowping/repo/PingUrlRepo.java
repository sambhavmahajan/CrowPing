package com.github.sambhavmahajan.crowping.repo;

import com.github.sambhavmahajan.crowping.entity.AppUser;
import com.github.sambhavmahajan.crowping.entity.PingUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PingUrlRepo extends JpaRepository<PingUrl, Long> {
    List<PingUrl> findAllByActiveTrue();
    List<PingUrl> findAllByOwnerEmail(String email);
    int countByOwnerEmail(String email);
}
