package com.github.sambhavmahajan.crowping.service.background;

import com.github.sambhavmahajan.crowping.entity.PingLog;
import com.github.sambhavmahajan.crowping.repo.PingLogRepo;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PingLogRepoProxy {
    private final PingLogRepo repo;
    private final ConcurrentHashMap<PingLog, Integer> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<PingLog>> emailToList;
    public PingLogRepoProxy(PingLogRepo repo) {
        this.repo = repo;
        List<PingLog> logs = repo.findAll();
        emailToList = new ConcurrentHashMap<>();
        for(var log : logs) {
            if(!(emailToList.containsKey(log.getOwnerEmail()))) {
                emailToList.put(log.getOwnerEmail(), new ArrayList<>());
            }
            emailToList.get(log.getOwnerEmail()).add(log);
            add(log);
        }
    }
    public List<PingLog> getAll() {
        List<PingLog> ret = new ArrayList<>();
        for(var entry : cache.entrySet()) {
            ret.add(entry.getKey());
        }
        return ret;
    }
    public void add(PingLog log) {
        cache.putIfAbsent(log, 0);
    }
    public List<PingLog> getAllByEmail(String email) {
        if(!emailToList.containsKey(email)) return new ArrayList<>();
        return emailToList.get(email);
    }
    @Scheduled(initialDelay = 86400000, fixedDelay = 86400000)
    @PreDestroy
    private void saveAll() {
        List<PingLog> li = new ArrayList<>();
        for(var entry : cache.entrySet()) {
            li.add(entry.getKey());
        }
        repo.saveAll(li);
    }

    public Optional<PingLog> findByOwnerEmailAndUrl(String ownerEmail, String url) {
        var logs = emailToList.get(ownerEmail);
        if(logs == null) {
            logs = new ArrayList<>();
            emailToList.put(ownerEmail, logs);
        }
        if(logs.isEmpty()) {
            return Optional.empty();
        }
        for(var log : logs) {
            if(url.equals(log.getUrl())) return Optional.of(log);
        }
        return Optional.empty();
    }

    public void save(PingLog pingLog) {
        List<PingLog> log = emailToList.get(pingLog.getOwnerEmail()).stream().
                filter((plog) -> plog.getUrl().equals(pingLog.getUrl()))
                .toList();
        if(log.isEmpty()) {
            cache.putIfAbsent(pingLog, 0);
            if(!emailToList.containsKey(pingLog.getOwnerEmail())) {
                emailToList.put(pingLog.getOwnerEmail(), new ArrayList<>());
            }
            emailToList.get(pingLog.getOwnerEmail()).add(pingLog);
        } else {
            PingLog oldLog = log.getFirst();
            oldLog.setTimestamp(pingLog.getTimestamp());
            oldLog.setMessage(pingLog.getMessage());
        }
    }

    public List<PingLog> findAllByOwnerEmail(String email) {
        if(emailToList.containsKey(email)) return emailToList.get(email);
        return new ArrayList<>();
    }
}
