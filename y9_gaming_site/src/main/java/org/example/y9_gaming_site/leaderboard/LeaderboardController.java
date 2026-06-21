package org.example.y9_gaming_site.leaderboard;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class LeaderboardController {
    private final LeaderboardService service;

    public LeaderboardController(LeaderboardService service){
        this.service=service;
    }

    @GetMapping("/leaderboard/{gameName}")
    public List<LeaderboardScore> getTopScored(String gameName){
        return service.getTopScored(gameName);
    }

    @GetMapping("/leaderboard/{gameName}/today")
    public List<LeaderboardScore> getTopScoresToday(@PathVariable String gameName) {
        return service.getTopScoresLast24Hours(gameName);
    }

    @GetMapping("/leaderboard/{gameName}/user/{userId}")
    public List<LeaderboardScore> getUserHistory(@PathVariable String gameName,
                                                 @PathVariable Long userId) {
        return service.getUserHistory(userId, gameName);
    }

}
