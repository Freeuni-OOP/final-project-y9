package org.example.y9_gaming_site.game.wordle;

import java.util.*;

public class WordGrader {

    public static List<LetterState> grade (String guess, String answer){
        if(guess == null || answer == null || answer.length() != guess.length()){
            throw new IllegalArgumentException("guess must be 5 char long");
        }
        int len = answer.length();
        LetterState[] states = new LetterState[len];
        boolean[] answerMatched = new boolean[len];
        for(int i = 0; i < len; i++){
            if(guess.charAt(i) == answer.charAt(i)){
                states[i] = LetterState.CORRECT;
                answerMatched[i] = true;
            }
        }
        Map<Character, Integer> remaining = new HashMap<>();
        for(int i = 0; i < len; i++){
            if(!answerMatched[i]){
                remaining.merge(answer.charAt(i), 1, Integer::sum);
            }
        }
        for(int i = 0; i < len; i++){
            if(states[i]== LetterState.CORRECT){
                continue;
            }
            char letter = guess.charAt(i);
            if(remaining.getOrDefault(letter,0) > 0){
                states[i] = LetterState.PRESENT;
                remaining.merge(letter, -1, Integer::sum);
            }else{
                states[i] = LetterState.ABSENT;
            }
        }
        List<LetterState> out = new ArrayList<>(len);
        Collections.addAll(out, states);
        return out;
    }

    public static boolean solved(String guess, String answer){
        return guess != null && guess.equals(answer);
    }

}
