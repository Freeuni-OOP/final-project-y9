package org.example.y9_gaming_site.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.example.y9_gaming_site.user.UserService;
import org.example.y9_gaming_site.security.PasswordUtil;
import org.example.y9_gaming_site.security.TokenUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.example.y9_gaming_site.streak.StreakService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserLoginController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final StreakService streakService;

    public UserLoginController(UserRepository userRepository, UserService userService, StreakService streakService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.streakService = streakService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto loginDto, HttpServletResponse response) {
        try {
            User user = userRepository.findByUsername(loginDto.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

            String incomingHashedPassword = PasswordUtil.hashPassword(loginDto.getPassword(), user.getSalt());

            if (!user.getPassword().equals(incomingHashedPassword)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
            }

            streakService.updateStreak(user.getId());

            String token = TokenUtil.generateToken(user.getUsername());
            addJwtCookie(response, token);

            return ResponseEntity.ok(Map.of("token", token, "username", user.getUsername(), "role", user.getRole().toString()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("EXCEPTION: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<?> joinAsGuest(HttpServletResponse response) {
        try {
            User guest = userService.createGuestUser();
            String token = TokenUtil.generateToken(guest.getUsername());
            addJwtCookie(response, token);

            return ResponseEntity.ok(Map.of("token", token, "username", guest.getUsername(), "role", guest.getRole().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not create guest session.");
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)      // must match addJwtCookie exactly
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return "redirect:/login";
    }

    private void addJwtCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false)      // set true only once you're actually on HTTPS in prod
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}