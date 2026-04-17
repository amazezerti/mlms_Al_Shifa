package com.alshifa.mlms_al_shifa.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        for (var authority : authentication.getAuthorities()) {
            switch (authority.getAuthority()) {
                case "ROLE_ADMIN":
                    return "redirect:/admin/dashboard";
                case "ROLE_RECEPTIONIST":
                    return "redirect:/receptionist/dashboard";
                case "ROLE_DOCTOR":
                    return "redirect:/doctor/dashboard";
                case "ROLE_LAB_TECHNICIAN":
                    return "redirect:/technician/dashboard";
            }
        }
        return "redirect:/login";
    }
}