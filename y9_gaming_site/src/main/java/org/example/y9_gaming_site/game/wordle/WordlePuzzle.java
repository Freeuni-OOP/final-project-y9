package org.example.y9_gaming_site.game.wordle;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "wordle_puzzles")
public class WordlePuzzle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "puzzle_date",unique = true)
    private LocalDate puzzleDate;

    @Column(name = "answer_word", nullable = false, length = 5, columnDefinition = "VARCHAR(5)")
    private String answerWord;

    @Column(name = "created_at",nullable = false )
    private LocalDateTime createdAt = LocalDateTime.now();

    public WordlePuzzle() {}

    public WordlePuzzle(LocalDate puzzleDate, String answerWord) {
        this.puzzleDate = puzzleDate;
        this.answerWord = answerWord;
    }
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public LocalDate getPuzzleDate() {return puzzleDate;}
    public void setPuzzleDate(LocalDate puzzleDate) {this.puzzleDate = puzzleDate;}

    public String getAnswerWord() {return answerWord;}
    public void setAnswerWord(String answerWord) {this.answerWord = answerWord;}

    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
}
