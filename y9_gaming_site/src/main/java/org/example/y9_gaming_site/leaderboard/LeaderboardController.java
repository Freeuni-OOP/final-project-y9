package org.example.y9_gaming_site.leaderboard;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class LeaderboardController {
    private final LeaderboardService service;

    public LeaderboardController(LeaderboardService service){
        this.service=service;
    }

    @GetMapping("/leaderboard/{gameName}")
    public List<LeaderboardScore> getTopScored(@PathVariable String gameName){
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

    @GetMapping("/leaderboard")
    public ModelAndView leaderboardPage() {
        return new ModelAndView("leaderboard");
    }

}
