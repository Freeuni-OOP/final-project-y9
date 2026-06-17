package org.example.y9_gaming_site.auth;

import org.example.y9_gaming_site.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // login გვერდის ჩვენება
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // → templates/login.html
    }
    // login form-ის დამუშავება
    @PostMapping("/login")
    @ResponseBody
    public String login(@RequestBody LoginRequest request,
                        HttpSession session) {
        try {
            User user = authService.login(
                    request.getEmail(),
                    request.getPassword()
            );
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            return "ok";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    // logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
