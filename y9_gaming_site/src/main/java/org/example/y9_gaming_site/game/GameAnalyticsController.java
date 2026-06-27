package org.example.y9_gaming_site.game;

import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameAnalyticsController {

    @Autowired
    private UserGameTimeRepository userGameTimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameService gameService;

    // homepage js madzlevs amas - aligned mapping naming properties exactly with payload keys
    public static class TimeTrackingRequest {
        public Long gameId;
        public String gameTitle;
        public String category;
        public long durationSeconds;
    }

    @PostMapping("/track-time")
    public ResponseEntity<?> logTime(Principal principal, @RequestBody TimeTrackingRequest req) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String principalName = principal.getName();
        User user = null;

        if (principal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth) {
            Object details = auth.getPrincipal();
            if (details instanceof User) {
                user = (User) details;
            }
        }

        if (user == null) {
            user = userRepository.findByUsername(principalName)
                    .orElseGet(() -> {
                        try {
                            Long userId = Long.parseLong(principalName);
                            return userRepository.findById(userId).orElse(null);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    });
        }

        if (user == null) {
            return ResponseEntity.status(404).body("User context missing for identifier: " + principalName);
        }

        UserGameTime tracking = userGameTimeRepository.findByUserAndGameTitle(user, req.gameTitle)
                .orElse(new UserGameTime());

        if (tracking.getId() == null) {
            tracking.setUser(user);
            tracking.setGameTitle(req.gameTitle);
            tracking.setCategory(req.category != null ? req.category : "ARCADE");
            tracking.setTotalTimeSeconds(0);
        } else {
            if (req.category != null) {
                tracking.setCategory(req.category);
            }
        }

        tracking.setTotalTimeSeconds(tracking.getTotalTimeSeconds() + req.durationSeconds);
        userGameTimeRepository.save(tracking);
        return ResponseEntity.ok().build();
    }

    // top5 tamashi useris
    @GetMapping("/my-top-5")
    public ResponseEntity<List<UserGameTime>> getMyTop5(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User context missing"));

        List<UserGameTime> top5 = userGameTimeRepository.findTop5ByUserIdOrderByTotalTimeSecondsDesc(user.getId());
        return ResponseEntity.ok(top5);
    }
}