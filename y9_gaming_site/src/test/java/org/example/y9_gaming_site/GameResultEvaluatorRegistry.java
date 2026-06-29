package org.example.y9_gaming_site;

import org.example.y9_gaming_site.gameRecord.GameResultEvaluator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameResultEvaluatorRegistry {
    private final Map<String, GameResultEvaluator> evalByGameKey;

    public GameResultEvaluatorRegistry(List<GameResultEvaluator> gameResultEvaluators) {
        this.evalByGameKey = gameResultEvaluators.stream().collect(Collectors
                .toMap(GameResultEvaluator:: getGameKey, e->e));
    }

    public GameResultEvaluator resolve (String gameKey) {
        GameResultEvaluator gameResultEvaluator = evalByGameKey.get(gameKey);
        if (gameResultEvaluator == null) {
            throw new IllegalArgumentException("no gameResultEvaluator for gameKey: " + gameKey);
        }
        return gameResultEvaluator;
    }
}
