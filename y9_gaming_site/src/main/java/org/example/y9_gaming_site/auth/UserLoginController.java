package org.example.y9_gaming_site.auth;

import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.example.y9_gaming_site.user.UserService;
import org.example.y9_gaming_site.security.PasswordUtil;
import org.example.y9_gaming_site.security.TokenUtil;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> login(@RequestBody UserLoginDto loginDto) {
        try {
            User user = userRepository.findByUsername(loginDto.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

            String incomingHashedPassword = PasswordUtil.hashPassword(loginDto.getPassword(), user.getSalt());

            if (!user.getPassword().equals(incomingHashedPassword)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
            }

            streakService.updateStreak(user.getId());

            String token = TokenUtil.generateToken(user.getUsername());
            return ResponseEntity.ok(Map.of("token", token, "username", user.getUsername(), "role", user.getRole().toString()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<?> joinAsGuest() {
        try {
            User guest = userService.createGuestUser();
            String token = TokenUtil.generateToken(guest.getUsername());
            return ResponseEntity.ok(Map.of("token", token, "username", guest.getUsername(), "role", guest.getRole().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not create guest session.");
        }
    }
}