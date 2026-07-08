package org.example.y9_gaming_site.game.sudoku;

import org.example.y9_gaming_site.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sudoku")
public class SudokuController {

    private final SudokuService sudokuService;

    public SudokuController(SudokuService sudokuService) {
        this.sudokuService = sudokuService;
    }

    public record SolveRequest(Long puzzleId, String solution, Integer elapsedSeconds){}

    @PostMapping("/solve")
    public ResponseEntity<Map<String, Boolean>> solve (@RequestBody SolveRequest req, Authentication authentication){
        Long userId = ((User) authentication.getPrincipal()).getId();
        int elapsed = req.elapsedSeconds() == null ? 0 : req.elapsedSeconds();
        boolean correct = sudokuService.recordSolve(userId, req.puzzleId(), req.solution(), elapsed);
        return ResponseEntity.ok(Map.of("correct", correct));
    }

    //mode1: daily game u play once a day. like wordle puzzle
    @GetMapping("/daily")
    public ResponseEntity<SudokuPuzzle> getDaily() {
        return ResponseEntity.ok(sudokuService.getDailyPuzzle());
    }

    //mode 2 n mode 3: you can play as many times as you want and even challenge a friend
    @GetMapping("/board")
    public ResponseEntity<SudokuPuzzle> getBoard(@RequestParam(required = false) Long challengeId,
                                                 @RequestParam(defaultValue = "MEDIUM") String difficulty) {
        if (challengeId != null) {
            //friend opened link
            return sudokuService.getPuzzleById(challengeId)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }

        //solo
        Optional<SudokuPuzzle> randomPuzzle = sudokuService.getSudokuPuzzleRepository()
                .findRandomByDifficulty(difficulty.toUpperCase());

        return randomPuzzle.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}