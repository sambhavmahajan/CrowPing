package com.github.sambhavmahajan.crowping.controller;

import com.github.sambhavmahajan.crowping.dto.AppUserDTO;
import com.github.sambhavmahajan.crowping.dto.PingDTO;
import com.github.sambhavmahajan.crowping.dto.UrlDTO;
import com.github.sambhavmahajan.crowping.entity.AppUser;
import com.github.sambhavmahajan.crowping.entity.PingLog;
import com.github.sambhavmahajan.crowping.entity.PingUrl;
import com.github.sambhavmahajan.crowping.exception.MaxPingLimitExceededException;
import com.github.sambhavmahajan.crowping.repo.PingUrlRepo;
import com.github.sambhavmahajan.crowping.service.AppUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    private final AppUserService appUserService;
    private final PingUrlRepo pingUrlRepo;
    private final PasswordEncoder passwordEncoder;

    public DashboardController(AppUserService appUserService, PingUrlRepo pingUrlRepo, PasswordEncoder passwordEncoder) {
        this.appUserService = appUserService;
        this.pingUrlRepo = pingUrlRepo;
        this.passwordEncoder = passwordEncoder;
    }
    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("UrlDTO", new UrlDTO());
        List<PingUrl> pingUrls = appUserService.getPingUrlsByEmail(authentication.getName());
        model.addAttribute("pingUrls", pingUrls);
        List<PingLog> logs = appUserService.getPingLogsByEmail(authentication.getName());
        model.addAttribute("logs", logs);
        return "dashboard";
    }
    @PostMapping("/createurl")
    public String createUrl(@ModelAttribute UrlDTO urlDTO, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        PingDTO pingUrl = new PingDTO();
        pingUrl.setUrl(urlDTO.getUrl());
        try {
            appUserService.addPingUrl(
                    Optional.of((AppUser) appUserService.loadUserByUsername(authentication.getName())), pingUrl);
        } catch (MaxPingLimitExceededException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard";
    }
    @DeleteMapping("/deleteurl")
    public String deleteUrl(Authentication authentication, @RequestParam long id, Model model, RedirectAttributes redirectAttributes) {
        PingUrl url = pingUrlRepo.findById(id).orElse(null);
        AppUser usr = (AppUser) appUserService.loadUserByUsername(authentication.getName());
        if(url == null) {
            redirectAttributes.addFlashAttribute("error", "PingUrl not found");
        }else if(url.getOwner().equals(usr)) {
            redirectAttributes.addFlashAttribute("error", "PingUrl owner not the same");
        } else pingUrlRepo.delete(url);
        return "redirect:/dashboard";
    }
    @GetMapping("/changepassword")
    public String changePassword(Model model, Authentication authentication) {
        AppUserDTO dto = new AppUserDTO();
        dto.setEmail(authentication.getName());
        model.addAttribute("dto", dto);
        return "change-password";
    }
    @PostMapping("/changepassword")
    public String changePasswordPost(@RequestParam("password") String oldPassword, @RequestParam("newpassword") String newPassword, @RequestParam("confirmnewpassword") String confirmNewPasssword, Principal principal, RedirectAttributes redirectAttributes) {
        AppUser usr = (AppUser) appUserService.loadUserByUsername(principal.getName());
        if(!passwordEncoder.matches(oldPassword, usr.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Incorrect password");
        } else if(!newPassword.equals(confirmNewPasssword)) {
            redirectAttributes.addFlashAttribute("error", "New Password Mismatch");
        } else {
            AppUserDTO appUserDTO = new AppUserDTO();
            appUserDTO.setPassword(newPassword);
            appUserDTO.setEmail(usr.getEmail());
            appUserDTO.setRole(usr.getRole());
            try {
                if(passwordEncoder.matches(appUserDTO.getPassword(),  usr.getPassword())) {
                    throw new RuntimeException("New Password cannot be same as old password");
                }
                appUserService.passwordValidator(appUserDTO);
                usr.setPassword(passwordEncoder.encode(newPassword));
                appUserService.resaveUser(usr);
                redirectAttributes.addFlashAttribute("success", "Password changed successfully");
            } catch (RuntimeException ex) {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
            }
        }
        return "redirect:/dashboard/changepassword";
    }
}
