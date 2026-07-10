package org.example.y9_gaming_site.wordle;

import org.example.y9_gaming_site.game.wordle.AttemptStatus;
import org.example.y9_gaming_site.game.wordle.WordleController;
import org.example.y9_gaming_site.game.wordle.WordlePuzzle;
import org.example.y9_gaming_site.game.wordle.WordleService;
import org.example.y9_gaming_site.game.wordle.dto.AttemptStateDto;
import org.example.y9_gaming_site.game.wordle.dto.GuessRequest;
import org.example.y9_gaming_site.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WordleControllerTest {

    @Mock
    private WordleService wordleService;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private WordleController wordleController;

    private static final Long USER_ID = 7L;
    private static final Long PUZZLE_ID = 42L;

    @BeforeEach
    void setUp() {
        User principal = new User();
        principal.setId(USER_ID);
        when(authentication.getPrincipal()).thenReturn(principal);
    }

    private AttemptStateDto dto(AttemptStatus status) {
        return new AttemptStateDto(PUZZLE_ID, 5, 6, status, List.of(), List.of(), 0, null, List.of());
    }

    @Test
    void testSample1(){ // getDaily creates or fetches the daily puzzle then loads the callers attempt state for it
        WordlePuzzle puzzle = new WordlePuzzle(null, "ABCDE");
        puzzle.setId(PUZZLE_ID);
        when(wordleService.getOrCreateDailyPuzzle()).thenReturn(puzzle);
        when(wordleService.getAttemptState(USER_ID, PUZZLE_ID)).thenReturn(dto(AttemptStatus.IN_PROGRESS));

        AttemptStateDto result = wordleController.getDaily(authentication);

        assertThat(result.status()).isEqualTo(AttemptStatus.IN_PROGRESS);
        verify(wordleService).getOrCreateDailyPuzzle();
        verify(wordleService).getAttemptState(USER_ID, PUZZLE_ID);
    }

    @Test
    void testSample2(){ // startPractice creates a practice puzzle then loads the callers attempt state for it
        WordlePuzzle puzzle = new WordlePuzzle(null, "ABCDE");
        puzzle.setId(PUZZLE_ID);
        when(wordleService.practicePuzzle()).thenReturn(puzzle);
        when(wordleService.getAttemptState(USER_ID, PUZZLE_ID)).thenReturn(dto(AttemptStatus.IN_PROGRESS));

        AttemptStateDto result = wordleController.startPractice(authentication);

        assertThat(result.status()).isEqualTo(AttemptStatus.IN_PROGRESS);
        verify(wordleService).practicePuzzle();
    }

    @Test
    void testSample3(){ // getAttemptState just forwards the caller id and requested puzzle id
        when(wordleService.getAttemptState(USER_ID, PUZZLE_ID)).thenReturn(dto(AttemptStatus.WON));

        AttemptStateDto result = wordleController.getAttemptState(PUZZLE_ID, authentication);

        assertThat(result.status()).isEqualTo(AttemptStatus.WON);
    }

    @Test
    void testSample4(){ // guess forwards callers id puzzle id and guess text to service
        when(wordleService.submitGuess(USER_ID, PUZZLE_ID, "ABCDE")).thenReturn(dto(AttemptStatus.WON));

        AttemptStateDto result = wordleController.guess(PUZZLE_ID, new GuessRequest("ABCDE"), authentication);

        assertThat(result.status()).isEqualTo(AttemptStatus.WON);
        verify(wordleService).submitGuess(USER_ID, PUZZLE_ID, "ABCDE");
    }
}