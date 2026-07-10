package org.example.y9_gaming_site.game.sudoku;

import org.example.y9_gaming_site.gameRecord.GameResultEvaluator;
import org.springframework.stereotype.Component;

@Component
public class SudokuResultEvaluator implements GameResultEvaluator {

    @Override
    public String getGameKey() {
        return "SUDOKU";
    }

    @Override
    public boolean isBetter(double candidateValue, double currentBestValue) {
        return candidateValue < currentBestValue; // fewer seconds is better
    }
}
