package com.github.sambhavmahajan.crowping.service.background;
import com.github.sambhavmahajan.crowping.email.EmailService;
import com.github.sambhavmahajan.crowping.entity.PingLog;
import com.github.sambhavmahajan.crowping.entity.PingUrl;
import com.github.sambhavmahajan.crowping.repo.PingLogRepo;
import com.github.sambhavmahajan.crowping.repo.PingUrlRepo;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class PingService {
    private final CopyOnWriteArrayList<PingUrl> pingUrls;
    private final PingLogRepo pingLogRepo;
    private final RestTemplate restTemplate;
    private final ExecutorService exe;
    private final EmailService emailService;
    private final PingUrlRepo pingUrlRepo;
    @Value("${app.nthreads}")
    private int appNThreads;
    private final ConcurrentHashMap<Long, Boolean> previousPingFailed = new ConcurrentHashMap<>();
    private final CacheManager cacheManager;
    private LocalDateTime nextTime;
    public PingService(PingUrlRepo repo, RestTemplate restTemplate, PingLogRepo pingLogRepo, ExecutorService exe, EmailService emailService, PingUrlRepo pingUrlRepo, CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        List<PingUrl> pings = repo.findAllByActiveTrue();
        pingUrls = new CopyOnWriteArrayList<>(pings);
        this.restTemplate = restTemplate;
        this.pingLogRepo = pingLogRepo;
        this.exe = exe;
        this.emailService = emailService;
        this.pingUrlRepo = pingUrlRepo;
    }
    @Scheduled(fixedDelayString = "${app.ping.fixDelay}")
    public void pingNow() {
        nextTime = LocalDateTime.now().plusMinutes(10);
        for(PingUrl url : pingUrls) {
            ping(url);
        }
    }
    @Async
    public void ping(PingUrl url) {
        if(!url.isActive()) return;
        String response = restTemplate.getForEntity(url.getUrl(), String.class).getStatusCode().toString();
        LocalDateTime now = LocalDateTime.now();
        PingLog pingLog = new PingLog();
        pingLog.setUrl(url.getUrl());
        pingLog.setTimestamp(now);
        pingLog.setMessage(response);
        pingLog.setOwnerEmail(url.getOwnerEmail());
        Cache cache = cacheManager.getCache("logs");
        if(cache != null) cache.evict(url.getOwnerEmail());
        exe.submit(() -> {
            Optional<PingLog> toDel = pingLogRepo.findByOwnerEmailAndUrl(url.getOwnerEmail(), pingLog.getUrl());
            if(toDel.isPresent()) pingLogRepo.delete(toDel.get());
            pingLogRepo.save(pingLog);
            String log = pingLog.getMessage();
            char ch = pingLog.getMessage().charAt('0');
            if(previousPingFailed.containsKey(url.getId()) && previousPingFailed.get(url.getId()) == true && !(ch == '5' || ch == '4')) {
                emailService.sendEmail(pingLog.getOwnerEmail(), "Site Up " + log, pingLog.getUrl());
            } else if(ch == '5' || ch == '4') {
                emailService.sendEmail(pingLog.getOwnerEmail(), "Site Down " + log, pingLog.getUrl());
            }
            previousPingFailed.put(url.getId(), (ch == '5' || ch == '4'));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        });
    }
    public void add(PingUrl url) {
        pingUrls.add(url);
        exe.submit(() -> {
           pingUrlRepo.save(url);
        });
    }
    @PreDestroy
    public void stop() {
        exe.shutdown();
    }
}
