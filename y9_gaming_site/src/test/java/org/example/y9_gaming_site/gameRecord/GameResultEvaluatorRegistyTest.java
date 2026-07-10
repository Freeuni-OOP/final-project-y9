package org.example.y9_gaming_site.gameRecord;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameResultEvaluatorRegistryTest {

    private final GameResultEvaluator higherWins = new GameResultEvaluator() {
        public String getGameKey() { return "False_high_score"; }
        public boolean isBetter(double c, double b) { return c > b; }
    };

    private final GameResultEvaluatorRegistry registry =
            new GameResultEvaluatorRegistry(List.of(higherWins));

    @Test
    void testSample1() {
        assertThat(registry.resolve("False_high_score")).isSameAs(higherWins);
    }

    @Test
    void testSample2() {
        assertThatThrownBy(() -> registry.resolve("Tetris"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("gameKey");
    }

    @Test
    void testSample3() { // two evaluators registered under the same gameKey fail fast at construction time
        GameResultEvaluator alsoHighScore = new GameResultEvaluator() {
            public String getGameKey() { return "False_high_score"; }
            public boolean isBetter(double c, double b) { return c > b; }
        };

        assertThatThrownBy(() -> new GameResultEvaluatorRegistry(List.of(higherWins, alsoHighScore)))
                .isInstanceOf(IllegalStateException.class);
    }
}
