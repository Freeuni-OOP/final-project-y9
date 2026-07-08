package org.example.y9_gaming_site.game;

import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameAnalyticsController {

    @Autowired
    private UserGameTimeRepository userGameTimeRepository;

    @Autowired
    private UserRepository userRepository;

    public static class TimeTrackingRequest {
        public Long gameId;
        public String gameTitle;
        public String category;
        public long durationSeconds;
    }

    // Response structure for categories
    public static class CategoryStatsResponse {
        public String category;
        public long totalTimeSeconds;

        public CategoryStatsResponse(String category, long totalTimeSeconds) {
            this.category = category;
            this.totalTimeSeconds = totalTimeSeconds;
        }
    }

    @PostMapping("/track-time")
    public ResponseEntity<?> logTime(Principal principal, @RequestBody TimeTrackingRequest req) {
        if (principal == null) return ResponseEntity.status(401).build();

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User missing"));

        UserGameTime tracking = userGameTimeRepository.findByUserAndGameTitle(user, req.gameTitle)
                .orElse(new UserGameTime());

        if (tracking.getId() == null) {
            tracking.setUser(user);
            tracking.setGameTitle(req.gameTitle);
            tracking.setCategory(req.category != null ? req.category : "ARCADE");
            tracking.setTotalTimeSeconds(0);
        } else if (req.category != null) {
            tracking.setCategory(req.category);
        }

        tracking.setTotalTimeSeconds(tracking.getTotalTimeSeconds() + req.durationSeconds);
        userGameTimeRepository.save(tracking);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/my-top-3")
    public ResponseEntity<List<UserGameTime>> getMyTop3(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User missing"));

        List<UserGameTime> top3 = userGameTimeRepository.findTop3FavoriteGames(user.getId());
        return ResponseEntity.ok(top3);
    }


    @GetMapping("/my-top-categories")
    public ResponseEntity<List<CategoryStatsResponse>> getMyTopCategories(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User missing"));

        List<Object[]> rawResults = userGameTimeRepository.findTop5CategoriesByUserId(user.getId());
        List<CategoryStatsResponse> formattedResponse = new ArrayList<>();

        for (Object[] row : rawResults) {
            String categoryName = (String) row[0];
            // Safe conversion of database numeric fields to long
            long totalSeconds = ((Number) row[1]).longValue();
            formattedResponse.add(new CategoryStatsResponse(categoryName, totalSeconds));
        }

        return ResponseEntity.ok(formattedResponse);
    }
}