package org.example.y9_gaming_site.game.wordle;

public enum LetterState {
    CORRECT, // if its right char
    PRESENT, // if this char is in word
    ABSENT // if this char isn't in word
}
