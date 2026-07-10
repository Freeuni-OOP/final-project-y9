package org.example.y9_gaming_site.game.sudoku;

import org.example.y9_gaming_site.achievement.AchievementService;
import org.example.y9_gaming_site.achievement.UnlockedAchievementDto;
import org.example.y9_gaming_site.game.sudoku.SudokuPuzzleRepository;
import org.example.y9_gaming_site.game.sudoku.SudokuPuzzle;
import org.example.y9_gaming_site.game.wordle.WordlePuzzle;
import org.example.y9_gaming_site.gameRecord.GameRecordService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

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
                .orElseGet(() -> {
                    SudokuPuzzle picked = sudokuPuzzleRepository.findRandomByDifficulty("MEDIUM")
                            .orElseThrow(() -> new RuntimeException("No Sudoku puzzles found in the database o no"));
                    picked.setPuzzleDate(LocalDate.now());
                    return sudokuPuzzleRepository.save(picked);
                });
    }

//    aqedan viweer hahah
//    public WordlePuzzle getOrCreateDailyPuzzle() {
//        LocalDate now = LocalDate.now();
//        return wordlePuzzleRepository.findByPuzzleDate(now).orElseGet(() -> {
//            Set<String> usedAnswers = new HashSet<>(wordlePuzzleRepository.findAnswerWordByPuzzleDateIsNotNull());
//            String answer = dict.pickWord(usedAnswers);
//            try {
//                return wordlePuzzleRepository.save(new WordlePuzzle(now, answer));
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }


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