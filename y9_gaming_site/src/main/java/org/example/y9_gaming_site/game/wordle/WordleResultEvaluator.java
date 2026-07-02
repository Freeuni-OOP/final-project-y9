package org.example.y9_gaming_site.game.wordle;


import org.example.y9_gaming_site.gameRecord.GameResultEvaluator;
import org.springframework.stereotype.Component;

@Component
public class WordleResultEvaluator implements GameResultEvaluator {

    @Override
    public String getGameKey()
    {
        return WordleService.GAME_KEY;
    }

    @Override
    public boolean isBetter(double receiver, double sender) {
        return receiver<sender;
    }

}
