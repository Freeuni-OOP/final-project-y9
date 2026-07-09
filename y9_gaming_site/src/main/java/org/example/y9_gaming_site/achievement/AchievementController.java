package org.example.y9_gaming_site.achievement;

import org.springframework.web.bind.annotation.*;
import  java.util.List;

@RestController //returns data as JSON
public class AchievementController {
    private final AchievementService achService;
    private final  AchievementRepository repository;
    public AchievementController(AchievementService achService, AchievementRepository repository){
        this.repository=repository;
        this.achService=achService;
    }

    @GetMapping("/achievements/{userId}")
    public List<UserAchievement> getUserAchievements(@PathVariable Long userId){
        return achService.getUserAchievement(userId);
    }

    @GetMapping("/achievements/{userId}/view")
    public List<AchievementView> getAchievementsView(@PathVariable Long userId){
        return achService.getEarnedView(userId);
    }

    @GetMapping("/achievements/{userId}/rarest")
    public List<AchievementView> getRarest(@PathVariable Long userId, @RequestParam(defaultValue = "3") int limit){
        // top 3 achievements of this user
        return achService.getRarestEarned(userId, limit);
    }

    @GetMapping("/achievements/catalog")
    public List<Achievement> getCatalog() {
        return repository.findAll();
    }
}
