package org.example.y9_gaming_site.game;

import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
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


    public static class GameTimeResponse {
        public Long gameId;
        public String gameTitle;
        public String category;
        public long totalTimeSeconds;

        public GameTimeResponse(UserGameTime t) {
            this.gameId = t.getId();
            this.gameTitle = t.getGameTitle();
            this.category = t.getCategory();
            this.totalTimeSeconds = t.getTotalTimeSeconds();
        }
    }


    @GetMapping("/{userId}/top-3")
    public ResponseEntity<?> getTop3(@PathVariable Long userId) {
        try {
            List<UserGameTime> games = userGameTimeRepository.findTop3FavoriteGames(
                    userId, PageRequest.of(0, 3));

            List<GameTimeResponse> response = games.stream()
                    .map(GameTimeResponse::new)
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage(), "type", e.getClass().getName()));
        }
    }

    @GetMapping("/{userId}/top-categories")
    public ResponseEntity<List<CategoryStatsResponse>> getTopCategories(@PathVariable Long userId) {
        List<Object[]> rawResults = userGameTimeRepository.findTop3CategoriesByUserId(
                userId, PageRequest.of(0, 3));

        List<CategoryStatsResponse> formattedResponse = new ArrayList<>();
        for (Object[] row : rawResults) {
            String categoryName = (String) row[0];
            long totalSeconds = ((Number) row[1]).longValue();
            formattedResponse.add(new CategoryStatsResponse(categoryName, totalSeconds));
        }

        return ResponseEntity.ok(formattedResponse);
    }
}