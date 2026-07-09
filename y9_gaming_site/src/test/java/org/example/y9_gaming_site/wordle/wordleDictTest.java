package org.example.y9_gaming_site.wordle;

import org.example.y9_gaming_site.game.wordle.WordleDict;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class wordleDictTest {
    private WordleDict dict = new WordleDict();

    @Test
    void testSample1(){ // loads Words
        assertThat(dict.size()).isGreaterThan(500);
    }

    @Test
    void testSample2(){ // checks if word is 5 char length
        String sample = dict.pickWord(Set.of());
        assertThat(sample).hasSize(dict.WORD_LENGTh);
        assertThat(dict.isCorrectFormat(sample)).isTrue();
    }

    @Test
    void testSample3(){ // rejects wrong length and format
        assertThat(dict.isCorrectFormat("ასდს")).isFalse();
        assertThat(dict.isCorrectFormat("ეპიკური")).isFalse();
        assertThat(dict.isCorrectFormat("drunk"));
    }

    @Test
    void testSample4(){
        assertThat(dict.isValidWord("ნალლი")).isFalse();
    }

    @Test
    public void testSample6(){
        assertThat(dict.isCorrectFormat(null)).isFalse();
    }

    @Test
    void testSample7(){ // pickWord actually avoids words in a nonempty set
        String first = dict.pickWord(Set.of());
        String second = dict.pickWord(Set.of(first));
        assertThat(second).isNotEqualTo(first);
    }

    @Test
    void testSample8(){ // isValidWord is true for word thats actually in dictionary
        String realWord = dict.pickWord(Set.of());
        assertThat(dict.isValidWord(realWord)).isTrue();
    }
}
