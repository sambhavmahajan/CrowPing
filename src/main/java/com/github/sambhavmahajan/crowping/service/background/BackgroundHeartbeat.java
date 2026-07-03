package com.github.sambhavmahajan.crowping.service.background;

import com.github.sambhavmahajan.crowping.repo.ConfirmTokenRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class BackgroundHeartbeat {
    public final ConfirmTokenRepo repo;
    public BackgroundHeartbeat(ConfirmTokenRepo repo) {
        this.repo = repo;
    }
    /*
    @Scheduled(fixedDelay = 210000)
    public void heartBeat() {
        try {
            long start = System.nanoTime();
            repo.ping();
            long end = System.nanoTime();
            long tot = (end - start) / 1000000;
            System.out.println("Heartbeat: " + tot + " milliseconds");
        }catch(Exception ex) {
            System.out.println("heartbeat failed");
        }
    }
    */
}
