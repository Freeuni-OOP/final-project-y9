package org.example.y9_gaming_site.wordle;

import org.example.y9_gaming_site.game.wordle.LetterState;
import org.example.y9_gaming_site.game.wordle.WordGrader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WordGraderTest {
    @Test
    public void testSample1(){ // exact match tiks every char
        List<LetterState> res = WordGrader.grade("ცხენი", "ცხენი");
        assertThat(res).containsExactly(LetterState.CORRECT, LetterState.CORRECT,
                LetterState.CORRECT, LetterState.CORRECT, LetterState.CORRECT);
    }

    @Test
    public void testSample2(){// every char is wrong
        List<LetterState> res = WordGrader.grade("კურკა", "ცხენი");
        assertThat(res).containsExactly(LetterState.ABSENT, LetterState.ABSENT,
                LetterState.ABSENT, LetterState.ABSENT, LetterState.ABSENT);
    }

    @Test
    public void testSample3(){ // some are Correct and some are present
        List<LetterState> res = WordGrader.grade("ცარცი", "ცხენი");
        assertThat(res).containsExactly(LetterState.CORRECT, LetterState.ABSENT,
                LetterState.ABSENT, LetterState.ABSENT, LetterState.CORRECT);
        List<LetterState> res1 = WordGrader.grade("ხახვი", "ცხენი");
        assertThat(res1).containsExactly(LetterState.PRESENT, LetterState.ABSENT,
                LetterState.ABSENT, LetterState.ABSENT, LetterState.CORRECT);
    }

    @Test
    public void testSample4(){
        assertThat(WordGrader.solved("წააგო", "ცხენი")).isFalse();
        assertThat(WordGrader.solved("ლილია", "ლილია")).isTrue();
        assertThat(WordGrader.solved(null,"ავაზა")).isFalse();
    }

    @Test
    public void testSample5(){
        assertThatThrownBy(() -> WordGrader.grade("არა", "კვახი"))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("5");
    }
}
