package org.example.y9_gaming_site.game.joker;

import org.example.y9_gaming_site.gameRecord.GameResultEvaluator;
import org.springframework.stereotype.Component;

@Component
public class JokerResultEvaluator implements GameResultEvaluator {

    @Override
    public String getGameKey() {
        return "JOKER";
    }

    @Override
    public boolean isBetter(double candidateValue, double currentBestValue) {
        return candidateValue > currentBestValue; // higher score is better
    }
}