package org.example.y9_gaming_site.wordle;

import org.example.y9_gaming_site.game.wordle.WordleResultEvaluator;
import org.example.y9_gaming_site.game.wordle.WordleService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WordleResultEvaluatorTest {
    private WordleResultEvaluator evaluator = new WordleResultEvaluator();

    @Test
    public void testSample1() {
        assertThat(evaluator.isBetter(3.0, 5.0)).isTrue(); //fewer guess is better
        assertThat(evaluator.isBetter(6.0, 5.0)).isFalse();// more is worse
        assertThat(evaluator.isBetter(5.0, 5.0)).isFalse();// equal isn't win
        assertThat(evaluator.getGameKey()).isEqualTo(WordleService.GAME_KEY);
    }
}
