package com.github.sambhavmahajan.crowping.service.background;
import com.github.sambhavmahajan.crowping.entity.PingUrl;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.*;

@Service
public class PingService {
    private final AsyncPingService asyncPingService;
    @Getter
    private LocalDateTime nextTime = LocalDateTime.now();
    public PingService(AsyncPingService asyncPingService) {
        this.asyncPingService = asyncPingService;
    }
    public void add(PingUrl url) {
        asyncPingService.add(url);
    }
    @Scheduled(fixedDelayString = "${app.ping.fixDelay}")
    public void pingNow() {
        nextTime = LocalDateTime.now().plusMinutes(10);
        for(PingUrl url : asyncPingService.getPingUrls()) {
            asyncPingService.ping(url);
        }
    }
}