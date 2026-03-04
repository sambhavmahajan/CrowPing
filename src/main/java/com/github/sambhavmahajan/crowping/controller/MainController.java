package com.github.sambhavmahajan.crowping.controller;

import com.github.sambhavmahajan.crowping.dto.AppUserDTO;
import com.github.sambhavmahajan.crowping.dto.RegisterDTO;
import com.github.sambhavmahajan.crowping.service.AppUserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {
    private final AppUserService appUserService;
    public MainController(AppUserService appUserService) {
        this.appUserService = appUserService;
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
            appUserService.registerUser(new AppUserDTO(registerDTO));
        } catch (RuntimeException ex) {
            model.addAttribute("message", ex.getMessage());
            model.addAttribute("bgcolor", "#ff6347;");
            return "register";
        }
        redirectAttributes.addFlashAttribute("message", "User registered successfully!");
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
}
