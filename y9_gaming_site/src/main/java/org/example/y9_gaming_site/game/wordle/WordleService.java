package org.example.y9_gaming_site.game.wordle;


import org.example.y9_gaming_site.achievement.AchievementService;
import org.example.y9_gaming_site.achievement.UnlockedAchievementDto;
import org.example.y9_gaming_site.game.wordle.dto.AttemptStateDto;
import org.example.y9_gaming_site.game.wordle.dto.GuessFeedbackDto;
import org.example.y9_gaming_site.gameRecord.GameRecordService;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class WordleService {

    public static final String GAME_KEY = "WORDLE";
    public static final int MAX_GUESSES = 6;

    private final WordlePuzzleRepository wordlePuzzleRepository;
    private final WordleAttemptRepository wordleAttemptRepository;
    private final WordleDict dict;
    private final GameRecordService gameRecordService;
    private final UserRepository userRepository;
    private final AchievementService achievementService;

    public WordleService(WordlePuzzleRepository wordlePuzzleRepository, WordleAttemptRepository attemptRepository,
                         WordleDict dict, GameRecordService gameRecordService,AchievementService achievementService, UserRepository userRepository) {
        this.wordlePuzzleRepository = wordlePuzzleRepository;
        this.wordleAttemptRepository = attemptRepository;
        this.dict = dict;
        this.gameRecordService = gameRecordService;
        this.userRepository = userRepository;
        this.achievementService = achievementService;
    }

    public WordlePuzzle getOrCreateDailyPuzzle() {
        LocalDate now = LocalDate.now();
        return wordlePuzzleRepository.findByPuzzleDate(now).orElseGet(() -> {
            Set<String> usedAnswers = new HashSet<>(wordlePuzzleRepository.findAnswerWordByPuzzleDateIsNotNull());
            String answer = dict.pickWord(usedAnswers);
            try {
                return wordlePuzzleRepository.save(new WordlePuzzle(now, answer));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public WordlePuzzle practicePuzzle() {
        String answer = dict.pickWord(Set.of());
        return wordlePuzzleRepository.save(new WordlePuzzle(null, answer));
    }

    public WordlePuzzle getPuzzleById(Long id) {
        return wordlePuzzleRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Puzzle id " + id + " not found!")
        );
    }

    public WordleAttempt getOrStartAttempt(Long userId, Long puzzleId) {
        return wordleAttemptRepository.findByUserIdAndPuzzleId(userId, puzzleId).orElseGet(
                ()->{
                    WordlePuzzle puzzle = getPuzzleById(puzzleId);
                    User user =  userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Puzzle id " + puzzleId + " not found!"));
                    return wordleAttemptRepository.save(new WordleAttempt(user, puzzle));
                }
        );
    }

    public AttemptStateDto getAttemptState(Long userId, long puzzleId) {
        return toDto(getOrStartAttempt(userId, puzzleId), List.of());
    }

    public AttemptStateDto submitGuess(Long userId, Long puzzleId, String rawGuess) {
        WordleAttempt attempt = getOrStartAttempt(userId, puzzleId);
        if(attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("no more guesses accepted!");
        }
        String guess = rawGuess == null ? "" : rawGuess.strip();
        if(!dict.isCorrectFormat(guess)) {
            throw new RuntimeException("invalid guess format!");
        }
        if(!dict.isValidWord(guess)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "WORD_NOT_FOUND");
        }

        String answer = attempt.getPuzzle().getAnswerWord();
        attempt.addGuess(guess);

        if(WordGrader.solved(guess, answer)) {
            attempt.setStatus(AttemptStatus.WON);
            attempt.setCompletedAt(LocalDateTime.now());
        }else if(attempt.guessCount() >= MAX_GUESSES) {
            attempt.setStatus(AttemptStatus.LOST);
            attempt.setCompletedAt(LocalDateTime.now());
        }

        wordleAttemptRepository.save(attempt);
        List<UnlockedAchievementDto> unlocked = new ArrayList<>();
        if(attempt.getStatus() == AttemptStatus.WON) {
            gameRecordService.recordResult(userId, GAME_KEY, puzzleId, (double) attempt.guessCount());
            int tries = attempt.guessCount();

            grant(userId, "WORDLE_FIRST_WIN", unlocked);

            if(tries <= 2){
                grant(userId, "WORDLE_IN_TWO_TRIES", unlocked);
            }
            if(tries == 1){
                grant(userId, "WORDLE_IN_ONE_TRY", unlocked);
            }
            if (attempt.getPuzzle().getPuzzleDate() != null) {
                if (computeDailyStreak(userId) >= 10) {
                    grant(userId, "WORDLE_DAILY_STREAK_10", unlocked);
                }
                long dailyWins = wordleAttemptRepository.countByUser_IdAndStatusAndPuzzle_PuzzleDateIsNotNull(userId, AttemptStatus.WON);
                if (dailyWins >= 50) {
                    grant(userId, "WORDLE_WINS_50", unlocked);
                }
            }

            if (isFlawlessWin(attempt)) {
                grant(userId, "WORDLE_FLAWLESS", unlocked);
            }
        }
        return toDto(attempt, unlocked);
    }

    private void grant(Long userId, String code, List<UnlockedAchievementDto> unlocked) {
        achievementService.grantByCode(userId, code).ifPresent(a -> unlocked.add(UnlockedAchievementDto.from(a)));
    }

    private int computeDailyStreak(Long userId) {
        List<LocalDate> wonDates = wordleAttemptRepository.findWonDailyPuzzleDatesDesc(userId, AttemptStatus.WON);
        int streak = 0;
        LocalDate expected = LocalDate.now();
        for (LocalDate date : wonDates) {
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private boolean isFlawlessWin(WordleAttempt attempt) {
        String answer = attempt.getPuzzle().getAnswerWord();
        for (String guess : attempt.getGuessList()) {
            if (WordGrader.grade(guess, answer).contains(LetterState.PRESENT)) {
                return false;
            }
        }
        return true;
    }

    private AttemptStateDto toDto(WordleAttempt attempt, List<UnlockedAchievementDto> newAchievements) {
        String answer = attempt.getPuzzle().getAnswerWord();
        boolean finished = attempt.getStatus() != AttemptStatus.IN_PROGRESS;
        List<GuessFeedbackDto> guess = attempt.getGuessList().stream()
                .map(gues -> new GuessFeedbackDto(gues, WordGrader.grade(gues, answer))).toList();

        return new AttemptStateDto(
                attempt.getPuzzle().getId(), WordleDict.WORD_LENGTh, MAX_GUESSES,
                attempt.getStatus(), guess, finished ? answer:null, newAchievements
        );
    }
}