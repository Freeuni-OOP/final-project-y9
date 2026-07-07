package org.example.y9_gaming_site.gameRecord;

import org.example.y9_gaming_site.game.Game;
import org.example.y9_gaming_site.game.GameRepository;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameRecordTest {

    @Mock
    private GameRecordRepository gameRecordRepository;
    @Mock private GameRepository gameRepository;
    @Mock private UserRepository userRepository;

    private GameResultEvaluatorRegistry gameResultEvaluatorRegistry;
    private GameRecordService recordService;

    private final GameResultEvaluator lowerWins = new GameResultEvaluator() {
        public String getGameKey() { return "Sudoku"; }
        public boolean isBetter(double c, double b) { return c < b; }
    };

    @BeforeEach
    void setUp() {
        gameResultEvaluatorRegistry = new GameResultEvaluatorRegistry(List.of(lowerWins));
        recordService = new GameRecordService(gameRecordRepository, gameRepository, userRepository, gameResultEvaluatorRegistry);
    }

    @Test
    void testSample1() {
        Game game = new Game();
        game.setId(1L);
        game.setTitle("Sudoku");
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(game));
        GameRecord slow = new GameRecord(null, game, 99L, 90.0);
        GameRecord fast = new GameRecord(null, game, 99L, 42.0);
        when(gameRecordRepository.findByUserIdAndGameIdAndContextId(5L, 1L, 99L))
                .thenReturn(List.of(slow, fast));
        Optional<GameRecord> best = recordService.findBest(5L, "Sudoku", 99L);

        assertThat(best).isPresent();
        assertThat(best.get().getValue()).isEqualTo(42.0);
    }

    @Test
    void testSample2() {
        Game game = new Game();
        game.setId(1L);
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(game));
        when(gameRecordRepository.findByUserIdAndGameIdAndContextId(5L, 1L, 99L))
                .thenReturn(List.of());

        assertThat(recordService.findBest(5L, "Sudoku", 99L)).isEmpty();
    }

    @Test
    void testSample3() {
        User user = new User();
        user.setId(5L);
        Game game = new Game();
        game.setId(1L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(game));
        when(gameRecordRepository.save(any(GameRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        GameRecord res = recordService.recordResult(5L, "Sudoku", 99L, 37.0);

        assertThat(res.getValue()).isEqualTo(37.0);
        assertThat(res.getUser()).isEqualTo(user);
        assertThat(res.getGame()).isEqualTo(game);
        verify(gameRecordRepository).save(any(GameRecord.class));
    }
}
