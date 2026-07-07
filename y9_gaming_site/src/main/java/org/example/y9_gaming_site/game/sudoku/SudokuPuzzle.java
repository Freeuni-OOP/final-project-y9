package org.example.y9_gaming_site.game.sudoku;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sudoku_puzzles")
public class SudokuPuzzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "puzzle_date", unique = true)
    private LocalDate puzzleDate;

    @Column(nullable = false, length = 81)
    private String definition;

    @Column(nullable = false, length = 81)
    private String solution;

    @Column(nullable = false)
    private String difficulty;

    public SudokuPuzzle(LocalDate puzzleDate, String definition, String solution, String difficulty) {
        this.puzzleDate = puzzleDate;
        this.definition = definition;
        this.solution = solution;
        this.difficulty = difficulty;
    }

    public SudokuPuzzle() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getPuzzleDate() {
        return puzzleDate;
    }

    public void setPuzzleDate(LocalDate puzzleDate) {
        this.puzzleDate = puzzleDate;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}