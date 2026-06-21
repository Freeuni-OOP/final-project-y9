package org.example.y9_gaming_site.streak;

import org.springframework.web.bind.annotation.*;
import java.util.Optional;

//to let front see result
@RestController
public class StreakController {

    private final StreakService streakService;

    public StreakController(StreakService streakService) {
        this.streakService = streakService;
    }

    @GetMapping("/streak/{userId}")
    public Optional<Streak> getStreak(@PathVariable Long userId) {
        return streakService.getStreak(userId);
    }
}