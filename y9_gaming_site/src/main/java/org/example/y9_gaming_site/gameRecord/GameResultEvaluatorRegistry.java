package org.example.y9_gaming_site.gameRecord;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GameResultEvaluatorRegistry {
    private final Map<String, GameResultEvaluator> evaluatorsByGameKey;

    public GameResultEvaluatorRegistry(List<GameResultEvaluator> evaluators) {
        this.evaluatorsByGameKey = evaluators.stream().collect(Collectors.toMap(GameResultEvaluator::getGameKey, e->e));
    }

    public GameResultEvaluator resolve (String gameKey) {
        GameResultEvaluator evaluator = evaluatorsByGameKey.get(gameKey);
        if (evaluator == null) {
            throw new IllegalArgumentException("No such gameKey: " + gameKey);
        }
        return evaluator;
    }

}
