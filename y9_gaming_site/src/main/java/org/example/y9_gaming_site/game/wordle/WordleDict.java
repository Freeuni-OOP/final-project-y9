package org.example.y9_gaming_site.game.wordle;


import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Component
public class WordleDict {
    public static final int WORD_LENGTh = 5;
    private static final String WORD_LIST = "/wordlists/5letter_words.txt";

    private static final Pattern damwerloba = Pattern.compile("^[\u10D0-\u10F0]+$");

    private final Set<String> dict;

    public WordleDict() {
        this.dict = Collections.unmodifiableSet(loadWords());
        if(this.dict.isEmpty()) {
            throw new RuntimeException("No words found!");
        }
    }

    private Set<String> loadWords() {
        Set<String> words = new HashSet<>();
        ClassPathResource resource = new ClassPathResource(WORD_LIST);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.strip();
                if(word.isEmpty()) {
                    continue;
                }
                if(word.length() != WORD_LENGTh || !damwerloba.matcher(word).matches()) {
                    continue;
                }
                words.add(word);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return words;
    }

    public boolean isValidWord(String word) {
        return word != null && dict.contains(word);
    }

    public boolean isCorrectFormat(String word) {
        return word != null && word.length() == WORD_LENGTh && damwerloba.matcher(word).matches();
    }

    public int size() {
        return dict.size();
    }

    public String pickWord(Set<String> exlude) {
        List<String> words = new ArrayList<>(dict);
        if(exlude != null && !exlude.isEmpty()) {
            words.removeAll(exlude);
            if(words.isEmpty()) {
                words = new ArrayList<>(dict);
            }
        }
        int index = ThreadLocalRandom.current().nextInt(0, words.size());
        return words.get(index);
    }

}
