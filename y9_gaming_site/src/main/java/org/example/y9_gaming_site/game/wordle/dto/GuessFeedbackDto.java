package org.example.y9_gaming_site.game.wordle.dto;

import org.example.y9_gaming_site.game.wordle.LetterState;

import java.util.List;

public record GuessFeedbackDto(String guessWord, List<LetterState> feedback) { }
