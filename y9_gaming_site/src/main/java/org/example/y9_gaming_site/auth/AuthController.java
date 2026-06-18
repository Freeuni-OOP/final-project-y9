package org.example.y9_gaming_site.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.example.y9_gaming_site.user.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;

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
                        HttpSession session, HttpServletRequest httpRequest) {

        try {
            String token = authService.login(
                    request.getEmail(),
                    request.getPassword()
            );
            // Tell Spring Security this user is logged in
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);
            httpRequest.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("token", token);
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
