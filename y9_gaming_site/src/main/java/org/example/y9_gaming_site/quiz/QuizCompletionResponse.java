package org.example.y9_gaming_site.quiz;

import org.example.y9_gaming_site.achievement.UnlockedAchievementDto;

import java.util.List;

public record QuizCompletionResponse(List<UnlockedAchievementDto> newAchievements) {
}
