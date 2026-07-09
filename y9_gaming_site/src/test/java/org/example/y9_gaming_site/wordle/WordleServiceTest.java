package org.example.y9_gaming_site.wordle;

import org.example.y9_gaming_site.achievement.AchievementService;
import org.example.y9_gaming_site.game.wordle.*;
import org.example.y9_gaming_site.game.wordle.dto.AttemptStateDto;
import org.example.y9_gaming_site.gameRecord.GameRecordService;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WordleServiceTest {

    @Mock
    private WordlePuzzleRepository wordlePuzzleRepository;
    @Mock
    private WordleAttemptRepository wordleAttemptRepository;
    @Mock
    private WordleDict dict;
    @Mock
    private GameRecordService gameRecordService;
    @Mock
    private AchievementService achievementService;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private WordleService wordleService;

    private static final Long USER_ID = 1L;
    private static final Long PUZZLE_ID = 10L;
    private static final String ANSWER = "ABCDE";

    @BeforeEach
    void setUp() {
        lenient().when(achievementService.grantByCode(anyLong(), anyString())).thenReturn(Optional.empty());
    }

    private WordlePuzzle puzzle(LocalDate date) {
        WordlePuzzle p = new WordlePuzzle(date, ANSWER);
        p.setId(PUZZLE_ID);
        return p;
    }

    private WordleAttempt existingAttempt(WordlePuzzle p) {
        User user = new User();
        user.setId(USER_ID);
        WordleAttempt attempt = new WordleAttempt(user, p);
        when(wordleAttemptRepository.findByUserIdAndPuzzleId(USER_ID, PUZZLE_ID)).thenReturn(Optional.of(attempt));
        return attempt;
    }

    @Test
    void testSample1(){ // returns today's puzzle when one already exists without touching the dictionary
        WordlePuzzle existing = puzzle(LocalDate.now());
        when(wordlePuzzleRepository.findByPuzzleDate(LocalDate.now())).thenReturn(Optional.of(existing));

        WordlePuzzle result = wordleService.getOrCreateDailyPuzzle();

        assertThat(result).isEqualTo(existing);
        verify(dict, never()).pickWord(any());
        verify(wordlePuzzleRepository, never()).save(any());
    }

    @Test
    void testSample2(){ // creates and saves a new daily puzzle when none exists yet
        when(wordlePuzzleRepository.findByPuzzleDate(LocalDate.now())).thenReturn(Optional.empty());
        when(wordlePuzzleRepository.findAnswerWordByPuzzleDateIsNotNull()).thenReturn(List.of("ZZZZZ"));
        when(dict.pickWord(any())).thenReturn(ANSWER);
        when(wordlePuzzleRepository.save(any(WordlePuzzle.class))).thenAnswer(inv -> inv.getArgument(0));

        WordlePuzzle result = wordleService.getOrCreateDailyPuzzle();

        assertThat(result.getAnswerWord()).isEqualTo(ANSWER);
        assertThat(result.getPuzzleDate()).isEqualTo(LocalDate.now());
        verify(wordlePuzzleRepository).save(any(WordlePuzzle.class));
    }

    @Test
    void testSample3(){ // practicePuzzle always creates a fresh, undated puzzle
        when(dict.pickWord(any())).thenReturn(ANSWER);
        when(wordlePuzzleRepository.save(any(WordlePuzzle.class))).thenAnswer(inv -> inv.getArgument(0));

        WordlePuzzle result = wordleService.practicePuzzle();

        assertThat(result.getPuzzleDate()).isNull();
        assertThat(result.getAnswerWord()).isEqualTo(ANSWER);
    }

    @Test
    void testSample4(){ // getPuzzleById - found vs not found
        WordlePuzzle p = puzzle(null);
        when(wordlePuzzleRepository.findById(PUZZLE_ID)).thenReturn(Optional.of(p));
        assertThat(wordleService.getPuzzleById(PUZZLE_ID)).isEqualTo(p);

        when(wordlePuzzleRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> wordleService.getPuzzleById(404L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testSample5(){ // getOrStartAttempt returns the existing attempt without creating a new one
        WordlePuzzle p = puzzle(null);
        WordleAttempt attempt = existingAttempt(p);

        WordleAttempt result = wordleService.getOrStartAttempt(USER_ID, PUZZLE_ID);

        assertThat(result).isEqualTo(attempt);
        verify(wordleAttemptRepository, never()).save(any());
    }

    @Test
    void testSample6(){ // getOrStartAttempt creates and saves a fresh attempt when none exists
        WordlePuzzle p = puzzle(null);
        User user = new User();
        user.setId(USER_ID);
        when(wordleAttemptRepository.findByUserIdAndPuzzleId(USER_ID, PUZZLE_ID)).thenReturn(Optional.empty());
        when(wordlePuzzleRepository.findById(PUZZLE_ID)).thenReturn(Optional.of(p));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(wordleAttemptRepository.save(any(WordleAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        WordleAttempt result = wordleService.getOrStartAttempt(USER_ID, PUZZLE_ID);

        assertThat(result.getStatus()).isEqualTo(AttemptStatus.IN_PROGRESS);
        assertThat(result.getPuzzle()).isEqualTo(p);
        verify(wordleAttemptRepository).save(any(WordleAttempt.class));
    }

    @Test
    void testSample7(){ // getOrStartAttempt throws when the user doesnt exist
        WordlePuzzle p = puzzle(null);
        when(wordleAttemptRepository.findByUserIdAndPuzzleId(USER_ID, PUZZLE_ID)).thenReturn(Optional.empty());
        when(wordlePuzzleRepository.findById(PUZZLE_ID)).thenReturn(Optional.of(p));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wordleService.getOrStartAttempt(USER_ID, PUZZLE_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSample8(){ // submitGuess rejects any further guesses once the attempt is already finished
        WordlePuzzle p = puzzle(null);
        WordleAttempt attempt = existingAttempt(p);
        attempt.setStatus(AttemptStatus.WON);

        assertThatThrownBy(() -> wordleService.submitGuess(USER_ID, PUZZLE_ID, ANSWER))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no more guesses");
    }

    @Test
    void testSample9(){ // submitGuess rejects a guess in the wrong format before ever checking the dictionary
        WordlePuzzle p = puzzle(null);
        existingAttempt(p);
        when(dict.isCorrectFormat("123")).thenReturn(false);

        assertThatThrownBy(() -> wordleService.submitGuess(USER_ID, PUZZLE_ID, "123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid guess format");
        verify(dict, never()).isValidWord(any());
    }

    @Test
    void testSample10(){ // submitGuess rejects a well formed guess that isnt real dictionary word
        WordlePuzzle p = puzzle(null);
        existingAttempt(p);
        when(dict.isCorrectFormat("ZZZZZ")).thenReturn(true);
        when(dict.isValidWord("ZZZZZ")).thenReturn(false);

        assertThatThrownBy(() -> wordleService.submitGuess(USER_ID, PUZZLE_ID, "ZZZZZ"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("WORD_NOT_FOUND");
    }

    @Test
    void testSample11(){ // a correct guess wins the game and reveals the answer in the response
        WordlePuzzle p = puzzle(null);
        existingAttempt(p);
        when(dict.isCorrectFormat(ANSWER)).thenReturn(true);
        when(dict.isValidWord(ANSWER)).thenReturn(true);

        AttemptStateDto result = wordleService.submitGuess(USER_ID, PUZZLE_ID, ANSWER);

        assertThat(result.status()).isEqualTo(AttemptStatus.WON);
        assertThat(result.answerWord()).isEqualTo(ANSWER);
        assertThat(result.guesses()).hasSize(1);
        verify(gameRecordService).recordResult(USER_ID, WordleService.GAME_KEY, PUZZLE_ID, 1.0);
    }

    @Test
    void testSample12(){ // a wrong guess below the guess limit stays in progress and keeps the answer hidden
        WordlePuzzle p = puzzle(null);
        existingAttempt(p);
        when(dict.isCorrectFormat("FGHIJ")).thenReturn(true);
        when(dict.isValidWord("FGHIJ")).thenReturn(true);

        AttemptStateDto result = wordleService.submitGuess(USER_ID, PUZZLE_ID, "FGHIJ");

        assertThat(result.status()).isEqualTo(AttemptStatus.IN_PROGRESS);
        assertThat(result.answerWord()).isNull();
        verify(gameRecordService, never()).recordResult(any(), any(), any(), anyDouble());
    }

    @Test
    void testSample13(){ // using up every guess without solving it results in lose and reveals the answer
        WordlePuzzle p = puzzle(null);
        WordleAttempt attempt = existingAttempt(p);
        for (int i = 0; i < WordleService.MAX_GUESSES - 1; i++) {
            attempt.addGuess("FGHIJ");
        }
        when(dict.isCorrectFormat("FGHIJ")).thenReturn(true);
        when(dict.isValidWord("FGHIJ")).thenReturn(true);

        AttemptStateDto result = wordleService.submitGuess(USER_ID, PUZZLE_ID, "FGHIJ");

        assertThat(result.status()).isEqualTo(AttemptStatus.LOST);
        assertThat(result.answerWord()).isEqualTo(ANSWER);
        verify(gameRecordService, never()).recordResult(any(), any(), any(), anyDouble());
    }

    @Test
    void testSample14(){ // getAttemptState on a brand new attempt reports zero guesses and hidden answer
        WordlePuzzle p = puzzle(null);
        User user = new User();
        user.setId(USER_ID);
        when(wordleAttemptRepository.findByUserIdAndPuzzleId(USER_ID, PUZZLE_ID)).thenReturn(Optional.empty());
        when(wordlePuzzleRepository.findById(PUZZLE_ID)).thenReturn(Optional.of(p));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(wordleAttemptRepository.save(any(WordleAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        AttemptStateDto result = wordleService.getAttemptState(USER_ID, PUZZLE_ID);

        assertThat(result.status()).isEqualTo(AttemptStatus.IN_PROGRESS);
        assertThat(result.guesses()).isEmpty();
        assertThat(result.answerWord()).isNull();
        assertThat(result.newAchievements()).isEmpty();
    }
}
