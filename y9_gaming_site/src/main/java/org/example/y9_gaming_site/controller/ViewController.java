package org.example.y9_gaming_site.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String showIndex() {
        return "index"; // Serves unified index.html
    }

    @GetMapping("/login")
    public String showLogin() {
        return "index"; // to index.html
    }

    @GetMapping("/home")
    public String showHome() {
        return "homePage"; //to homepage
    }
}