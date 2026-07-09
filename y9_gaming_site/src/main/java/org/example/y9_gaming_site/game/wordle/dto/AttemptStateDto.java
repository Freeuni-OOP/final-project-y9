package org.example.y9_gaming_site.game.wordle.dto;

import org.example.y9_gaming_site.achievement.UnlockedAchievementDto;
import org.example.y9_gaming_site.game.wordle.AttemptStatus;

import java.util.List;

public record AttemptStateDto (Long puzzleId,
                               int wordLength,
                               int maxGuesses,
                               AttemptStatus status,
                               List<GuessFeedbackDto> guesses,
                               String answerWord,
                               List<UnlockedAchievementDto> newAchievements){}
