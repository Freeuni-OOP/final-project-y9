package org.example.y9_gaming_site.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProfilePageController {

    @GetMapping("/profile/{id}")
    public String profile(@PathVariable Long idl) {
      //  model.addAttribute("userId", id);
        return "profile";
    }
}