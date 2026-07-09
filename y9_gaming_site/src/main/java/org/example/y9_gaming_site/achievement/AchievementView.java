package org.example.y9_gaming_site.achievement;

import java.time.LocalDateTime;

public record AchievementView(
        String code, String name, String description,
        LocalDateTime earnedTime, long earnedCount
) {
}
