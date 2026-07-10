package org.example.y9_gaming_site.controller;

import org.example.y9_gaming_site.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProfilePageController {
    @GetMapping("/profile")
    public String myProfile(@AuthenticationPrincipal User currentUser) {
        return "redirect:/profile/" + currentUser.getId();
    }

    @GetMapping("/profile/{id}")
    public String profile(@PathVariable Long id, Model model) {
        model.addAttribute("userId", id);
        return "profile";
    }
}