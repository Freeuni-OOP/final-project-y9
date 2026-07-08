package org.example.y9_gaming_site.achievement;

public record UnlockedAchievementDto(String code, String name, String description) {
    public static UnlockedAchievementDto from(Achievement achievement) {
        return new UnlockedAchievementDto(achievement.getCode(), achievement.getName(), achievement.getDescription());
    }
}
