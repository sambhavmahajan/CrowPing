package com.github.sambhavmahajan.crowping.controller;

import com.github.sambhavmahajan.crowping.dto.AppUserDTO;
import com.github.sambhavmahajan.crowping.dto.RegisterDTO;
import com.github.sambhavmahajan.crowping.email.EmailService;
import com.github.sambhavmahajan.crowping.entity.AppUser;
import com.github.sambhavmahajan.crowping.exception.ConfirmTokenExpiredException;
import com.github.sambhavmahajan.crowping.repo.ConfirmTokenRepo;
import com.github.sambhavmahajan.crowping.security.ConfirmToken;
import com.github.sambhavmahajan.crowping.service.AppUserService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Controller
public class MainController {
    private final AppUserService appUserService;
    private final EmailService emailService;
    private final ConfirmTokenRepo confirmTokenRepo;
    private final CacheManager cacheManager;

    public MainController(AppUserService appUserService, EmailService emailService, ConfirmTokenRepo confirmTokenRepo,  CacheManager cacheManager) {
        this.appUserService = appUserService;
        this.emailService = emailService;
        this.confirmTokenRepo = confirmTokenRepo;
        this.cacheManager = cacheManager;
    }
    @GetMapping("/login")
    public String login(Authentication auth) {
        if(auth != null && !(auth instanceof AnonymousAuthenticationToken) && auth.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "login";
    }
    @GetMapping("/register")
    public String register(Authentication auth, Model model) {
        if(auth != null && !(auth instanceof AnonymousAuthenticationToken) && auth.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("registerDTO", new RegisterDTO());
        model.addAttribute("message", null);
        model.addAttribute("bgcolor", null);
        return "register";
    }
    @PostMapping("/register")
    public String register(@ModelAttribute("registerDTO") RegisterDTO registerDTO, Model model, RedirectAttributes redirectAttributes) {
        try {
            final AppUser usr = appUserService.registerUser(new AppUserDTO(registerDTO));
            redirectAttributes.addFlashAttribute("message", "User registered successfully! Please verify your email.");
            ConfirmToken token = new ConfirmToken(UUID.randomUUID().toString(), usr.getEmail());
            confirmTokenRepo.save(token);
            emailService.sendEmail(usr.getEmail(), "Email Verification", token.getToken());
        } catch (RuntimeException ex) {
            model.addAttribute("message", ex.getMessage());
            model.addAttribute("bgcolor", "#ff6347;");
            return "register";
        }
        return "redirect:/login";
    }
    @GetMapping("/home")
    public String home() {
        return "home";
    }
    @GetMapping("/ping")
    @ResponseBody
    public String ping() {
        return "pong";
    }
    @GetMapping("/verify/{id}")
    @ResponseBody
    public String verify(@PathVariable("id") String id) {
        Optional<ConfirmToken> token = confirmTokenRepo.findById(id);
        if(token.isEmpty()) {
            return "Invalid token";
        }
        try {
            token.get().confirm();
            AppUser usr = (AppUser) appUserService.loadUserByUsername(token.get().getUserEmail());
            usr.setEnabled(true);
            appUserService.resaveUser(usr);
        } catch(RuntimeException ex) {
            if(ex instanceof ConfirmTokenExpiredException) {
                Cache cache = cacheManager.getCache("users");
                if(cache != null) cache.evict(token.get().getUserEmail());
                confirmTokenRepo.deleteById(id);
            }
            return ex.getMessage();
        }
        Cache cache = cacheManager.getCache("users");
        if(cache != null) cache.evict(token.get().getUserEmail());
        return "Email confirmed";
    }
}
