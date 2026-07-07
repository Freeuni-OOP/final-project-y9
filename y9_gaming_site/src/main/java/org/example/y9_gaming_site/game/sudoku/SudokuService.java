package org.example.y9_gaming_site.game.sudoku;

import org.example.y9_gaming_site.game.SudokuPuzzleRepository;
import org.example.y9_gaming_site.game.sudoku.SudokuPuzzle;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class SudokuService {

    private final SudokuPuzzleRepository sudokuPuzzleRepository;

    public SudokuService(SudokuPuzzleRepository sudokuPuzzleRepository) {
        this.sudokuPuzzleRepository = sudokuPuzzleRepository;
    }

    //todays puzzle, if not found medium puzzle
    public SudokuPuzzle getDailyPuzzle() {
        return sudokuPuzzleRepository.findByPuzzleDate(LocalDate.now())
                .orElseGet(() -> sudokuPuzzleRepository.findRandomByDifficulty("MEDIUM")
                        .orElseThrow(() -> new RuntimeException("No Sudoku puzzles found in the database! Please run seed data.")));
    }


    public Optional<SudokuPuzzle> getPuzzleById(Long id) {
        return sudokuPuzzleRepository.findById(id);
    }

    //answer check
    public boolean verifySolution(Long puzzleId, String playerSolution) {
        return sudokuPuzzleRepository.findById(puzzleId)
                .map(puzzle -> puzzle.getSolution().equals(playerSolution))
                .orElse(false);
    }

    public SudokuPuzzleRepository getSudokuPuzzleRepository() {
        return this.sudokuPuzzleRepository;
    }
}