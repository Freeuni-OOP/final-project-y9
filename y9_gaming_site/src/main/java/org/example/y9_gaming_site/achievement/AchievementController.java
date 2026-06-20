package org.example.y9_gaming_site.achievement;

import org.springframework.web.bind.annotation.*;
import  java.util.List;

@RestController //returns data as JSON
public class AchievementController {
    private final AchievementService achService;
    public AchievementController(AchievementService achService){
        this.achService=achService;
    }

    @GetMapping("/achievements/{userId}")
    public List<UserAchievement> getUserAchievements(@PathVariable Long userId){
        return achService.getUserAchievement(userId);
    }
}
