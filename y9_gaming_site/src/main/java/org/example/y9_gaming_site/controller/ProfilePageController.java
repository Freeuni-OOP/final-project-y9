package org.example.y9_gaming_site.controller;

import org.example.y9_gaming_site.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
public class ProfilePageController {

    @GetMapping("/profile/{id}")
    public String profile(@PathVariable Long id, Model model) {
        model.addAttribute("userId", id);
        return "profile";
    }
}