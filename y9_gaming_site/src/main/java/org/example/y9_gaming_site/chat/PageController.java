package org.example.y9_gaming_site.chat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }
}
