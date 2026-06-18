package org.example.y9_gaming_site.security;

import java.util.*;

public class ContentModerator {
    private static final Set<String> BANNED_KEYWORDS = new HashSet<>();
    private static final Set<String> TOXIC_WORDS = new HashSet<>();

    static {
        try (java.io.InputStream is = ContentModerator.class.getResourceAsStream("/bad_words.txt");
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
            if (is != null) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim().toLowerCase();
                    if (!trimmed.isEmpty()) {
                        BANNED_KEYWORDS.add(trimmed);
                        String[] pieces = trimmed.split("[\\s_-]+");
                        for (String piece : pieces) {
                            if (piece.length() > 2) {
                                TOXIC_WORDS.add(piece);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading banned words: " + e.getMessage());
        }

        BANNED_KEYWORDS.add("hack");
        TOXIC_WORDS.add("hack");
    }

    public static boolean isFlagged(String input) {
        if (input == null || input.trim().isEmpty()) return false;

        String originalNormalized = input.toLowerCase().trim();

        String decodedText = originalNormalized
                .replace("4", "a")
                .replace("3", "e")
                .replace("1", "i")
                .replace("0", "o")
                .replace("|", "i")
                .replace("5", "s");

        String squishedText = squish(decodedText);

        if (BANNED_KEYWORDS.contains(originalNormalized) || BANNED_KEYWORDS.contains(squishedText)) {
            return true;
        }

        String[] words = decodedText.split("[\\s_-]+");
        String[] squishedWords = squishedText.split("[\\s_-]+");

        Set<String> tokensToTest = new HashSet<>();
        for (String w : words) tokensToTest.add(w.replaceAll("[^a-z0-9]", ""));
        for (String w : squishedWords) tokensToTest.add(w.replaceAll("[^a-z0-9]", ""));

        for (String cleanWord : tokensToTest) {
            if (cleanWord.isEmpty()) continue;

            if (TOXIC_WORDS.contains(cleanWord)) {
                return true;
            }

            for (String toxicWord : TOXIC_WORDS) {
                if (cleanWord.contains(toxicWord)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static String squish(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder res = new StringBuilder();
        res.append(s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != s.charAt(i - 1)) {
                res.append(s.charAt(i));
            }
        }
        return res.toString();
    }
}