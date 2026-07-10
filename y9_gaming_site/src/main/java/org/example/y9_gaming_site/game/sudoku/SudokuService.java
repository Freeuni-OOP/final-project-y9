package org.example.y9_gaming_site.game.sudoku;

import org.example.y9_gaming_site.achievement.AchievementService;
import org.example.y9_gaming_site.achievement.UnlockedAchievementDto;
import org.example.y9_gaming_site.game.SudokuPuzzleRepository;
import org.example.y9_gaming_site.game.sudoku.SudokuPuzzle;
import org.example.y9_gaming_site.gameRecord.GameRecordService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SudokuService {

    public static final String GAME_KEY = "SUDOKU";
    private final SudokuPuzzleRepository sudokuPuzzleRepository;
    private final AchievementService achievementService;
    private final GameRecordService gameRecordService;

    public SudokuService(SudokuPuzzleRepository sudokuPuzzleRepository,
                         AchievementService achievementService,
                         GameRecordService gameRecordService) {
        this.sudokuPuzzleRepository = sudokuPuzzleRepository;
        this.achievementService = achievementService;
        this.gameRecordService = gameRecordService;
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

    public SudokuSolveResponse submitSolve(Long userId, Long puzzleId, int secondsTaken) {
        gameRecordService.recordResult(userId, GAME_KEY, puzzleId, secondsTaken);

        List<UnlockedAchievementDto> unlocked = new ArrayList<>();
        grant(userId, "SUDOKU_FIRST_SOLVE", unlocked);
        if (secondsTaken < 120) {
            grant(userId, "SUDOKU_IN_120", unlocked);
        }
        if (secondsTaken < 60) {
            grant(userId, "SUDOKU_IN_60", unlocked);
        }
        return new SudokuSolveResponse(unlocked);
    }

    private void grant(Long userId, String code, List<UnlockedAchievementDto> unlocked) {
        achievementService.grantByCode(userId, code).ifPresent(a -> unlocked.add(UnlockedAchievementDto.from(a)));
    }
}