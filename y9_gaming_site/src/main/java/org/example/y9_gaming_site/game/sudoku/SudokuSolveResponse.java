package org.example.y9_gaming_site.game.sudoku;

import org.example.y9_gaming_site.achievement.UnlockedAchievementDto;

import java.util.List;

public record SudokuSolveResponse(List<UnlockedAchievementDto> newAchievements) {
}
