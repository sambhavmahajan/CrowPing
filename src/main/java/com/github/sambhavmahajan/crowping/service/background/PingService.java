package com.github.sambhavmahajan.crowping.service.background;
import com.github.sambhavmahajan.crowping.entity.PingLog;
import com.github.sambhavmahajan.crowping.entity.PingUrl;
import com.github.sambhavmahajan.crowping.repo.PingLogRepo;
import com.github.sambhavmahajan.crowping.repo.PingUrlRepo;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PingService {
    private final CopyOnWriteArrayList<PingUrl> pingUrls;
    private final PingLogRepo pingLogRepo;
    private final RestTemplate restTemplate;
    private final ExecutorService exe;
    @Value("${app.nthreads}")
    private int appNThreads;
    public PingService(PingUrlRepo repo, RestTemplate restTemplate, PingLogRepo pingLogRepo, ExecutorService exe) {
        List<PingUrl> pings = repo.findAllByActiveTrue();
        pingUrls = new CopyOnWriteArrayList<>(pings);
        this.restTemplate = restTemplate;
        this.pingLogRepo = pingLogRepo;
        this.exe = exe;
    }
    @Scheduled(fixedDelayString = "${app.ping.fixDelay}")
    public void pingNow() {
        for(PingUrl url : pingUrls) {
            ping(url);
        }
    }
    @Async
    @CacheEvict(value="logs", key="#url.owner.email", condition = "#url.active")
    public void ping(PingUrl url) {
        if(!url.isActive()) return;
        String response = restTemplate.getForEntity(url.getUrl(), String.class).getStatusCode().toString();
        LocalDateTime now = LocalDateTime.now();
        PingLog pingLog = new PingLog();
        pingLog.setUrl(url.getUrl());
        pingLog.setTimestamp(now);
        pingLog.setMessage(response);
        pingLog.setOwner(url.getOwner());
        exe.submit(() -> {
            Optional<PingLog> toDel = pingLogRepo.findByOwnerEmailAndUrl(url.getOwner().getEmail(), pingLog.getUrl());
            if(toDel.isPresent()) pingLogRepo.delete(toDel.get());
            pingLogRepo.save(pingLog);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        });
    }
    public void add(PingUrl url) {
        pingUrls.add(url);
    }
    @PreDestroy
    public void stop() {
        exe.shutdown();
    }
}
