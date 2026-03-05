package com.github.sambhavmahajan.crowping.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppErrorController implements ErrorController {
    @RequestMapping("/error")
    public String error(Model model, HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String code = "I could not parse error code, sorry";
        if(status != null) {
            code = status.toString();
        }
        model.addAttribute("errorCode", code);
        return "error";
    }
}
