package com.github.sambhavmahajan.crowping.service;
import com.github.sambhavmahajan.crowping.dto.AppUserDTO;
import com.github.sambhavmahajan.crowping.dto.PingDTO;
import com.github.sambhavmahajan.crowping.entity.AppUser;
import com.github.sambhavmahajan.crowping.entity.PingLog;
import com.github.sambhavmahajan.crowping.exception.*;
import com.github.sambhavmahajan.crowping.repo.PingLogRepo;
import com.github.sambhavmahajan.crowping.service.background.PingService;
import com.github.sambhavmahajan.crowping.entity.PingUrl;
import com.github.sambhavmahajan.crowping.repo.AppUserRepo;
import com.github.sambhavmahajan.crowping.repo.PingUrlRepo;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppUserService implements UserDetailsService {
    private final AppUserRepo userRepo;
    private final PingService pingService;
    private final PasswordEncoder passwordEncoder;
    private final PingUrlRepo pingUrlRepo;
    private final PingLogRepo pingLogRepo;
    private final CacheManager cacheManager;
    @Value(value = "${app.pinglimit}")
    private int pingLimit;
    public AppUserService(AppUserRepo userRepo, PingService pingService, PasswordEncoder passwordEncoder, PingUrlRepo pingUrlRepo, PingLogRepo pingLogRepo, CacheManager cacheManager) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.pingService = pingService;
        this.pingUrlRepo = pingUrlRepo;
        this.pingLogRepo = pingLogRepo;
        this.cacheManager = cacheManager;
    }
    public void passwordValidator(AppUserDTO userDTO) throws  RuntimeException {
        if(userDTO.getEmail() == null || userDTO.getPassword() == null){
            throw new EmailOrPasswordEmptyException();
        }
        if(userDTO.getPassword().length() < 6 || userDTO.getPassword().length() > 32){
            throw new PasswordLengthException();
        }
        if(userDTO.getRole() == null) {
            throw new RuntimeException("Role cannot be empty");
        }
        boolean hasDigit = false;
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        for(int i = 0; i < userDTO.getPassword().length() && (!hasDigit || !hasLowercase || !hasUppercase); i++){
            if(Character.isUpperCase(userDTO.getPassword().charAt(i))) {
                hasUppercase = true;
            } else if(Character.isLowerCase(userDTO.getPassword().charAt(i))) {
                hasLowercase = true;
            } else if(Character.isDigit(userDTO.getPassword().charAt(i))) {
                hasDigit = true;
            }
        }
        if(!hasDigit || !hasLowercase || !hasUppercase) {
            throw new PasswordFormatException();
        }
    }
    @Transactional
    public AppUser registerUser(AppUserDTO userDTO) throws RuntimeException {
        if(userRepo.findByEmail(userDTO.getEmail()).isPresent()){
            throw new UsernameAlreadyExistsException(userDTO.getEmail());
        }
        passwordValidator(userDTO);
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userDTO.setRole("ROLE_" + userDTO.getRole());
        AppUser appUser = new AppUser(userDTO);
        userRepo.save(appUser);
        Cache cache = cacheManager.getCache("users");
        if(cache != null) cache.put(appUser.getEmail(), appUser);
        return appUser;
    }
    private void userNameMatches(Authentication auth, AppUserDTO userDTO) throws RuntimeException {
        String username = auth.getName();
        if(!username.equals(userDTO.getEmail())){
            throw new RuntimeException("Email does not match");
        }
    }
    @Transactional
    public void updatePassword(Authentication auth, AppUserDTO userDTO) throws RuntimeException {
        Cache cache = cacheManager.getCache("users");
        if(cache != null) cache.evict(auth.getName());
        userNameMatches(auth,userDTO);
        passwordValidator(userDTO);
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userRepo.save(new AppUser(userDTO));
    }
    @Transactional
    public void deleteUser(Authentication auth, AppUserDTO userDTO) throws RuntimeException {
        Cache cache = cacheManager.getCache("users");
        if(cache != null) cache.evict(auth.getName());
        userNameMatches(auth, userDTO);
        userRepo.delete(userRepo.findByEmail(userDTO.getEmail()).orElseThrow(()->new UsernameNotFoundException(userDTO.getEmail())));
    }
    @Transactional
    public void addPingUrl(Authentication auth, Optional<AppUser> appUsr, PingDTO dto) throws RuntimeException {
        if(appUsr.isEmpty()) {
            throw new RuntimeException("Something bad happened! Relogin required");
        }
        if(appUsr.get().getCountUrl() >= pingLimit) {
            throw new MaxPingLimitExceededException();
        }
        Cache cache = cacheManager.getCache("urls");
        if(cache != null) cache.evict(auth.getName());
        PingUrl pingUrl = new PingUrl(dto, appUsr.get().getEmail());
        pingService.add(pingUrl);
        appUsr.get().setCountUrl(appUsr.get().getCountUrl() + 1);
        Cache cache1 = cacheManager.getCache("users");
        if(cache1 != null) cache1.put(auth.getName(),  appUsr.get());
        userRepo.save(appUsr.get());
    }
    @Transactional
    public void deletePingUrl(Authentication auth, PingDTO dto) throws RuntimeException {
        Optional<AppUser> appUsr = userRepo.findByEmail(auth.getName());
        if(appUsr.isEmpty()) {
            throw new RuntimeException("Something bad happened! Relogin required");
        }
        Optional<PingUrl> url = pingUrlRepo.findById(dto.getId());
        if(url.isEmpty()) {
            throw new NoSuchPingUrlExistsException();
        }
        if(!url.get().getOwnerEmail().equals(appUsr.get().getEmail())) {
            throw new OwnerMismatchException();
        }
        Cache cache = cacheManager.getCache("urls");
        if(cache != null) cache.evict(auth.getName());
        appUsr.get().setCountUrl(appUsr.get().getCountUrl() - 1);
        Cache cache1 = cacheManager.getCache("users");
        if(cache1 != null) cache1.put(auth.getName(), appUsr.get());
        url.get().setActive(false);
        userRepo.save(appUsr.get());
        pingUrlRepo.deleteById(url.get().getId());
    }
    @Cacheable(value="urls", key="#email")
    public List<PingUrl> getPingUrlsByEmail(String email) throws RuntimeException {
        return pingUrlRepo.findAllByOwnerEmail(email);
    }
    @Override
    @Cacheable(value="users", key="#username")
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return userRepo.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    @Cacheable(value="logs", key="#email")
    public List<PingLog> getPingLogsByEmail(String email) throws RuntimeException {
        return pingLogRepo.findAllByOwnerEmail(email);
    }
    @CacheEvict(value="users", key="#appUser.email")
    public void resaveUser(AppUser appUser) {
        userRepo.save(appUser);
    }
}