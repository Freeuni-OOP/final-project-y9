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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameRecordServiceTest {

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

    private Game sudoku;

    @BeforeEach
    void setUp() {
        gameResultEvaluatorRegistry = new GameResultEvaluatorRegistry(List.of(lowerWins));
        recordService = new GameRecordService(gameRecordRepository, gameRepository, userRepository, gameResultEvaluatorRegistry);
        sudoku = new Game();
        sudoku.setId(1L);
        sudoku.setTitle("Sudoku");
    }

    private User userWithId(long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private GameRecord recordFor(User user, Long contextId, double value) {
        return new GameRecord(user, sudoku, contextId, value);
    }

    @Test
    void testSample1() {
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(sudoku));
        GameRecord slow = new GameRecord(null, sudoku, 99L, 90.0);
        GameRecord fast = new GameRecord(null, sudoku, 99L, 42.0);
        when(gameRecordRepository.findByUserIdAndGameIdAndContextId(5L, 1L, 99L))
                .thenReturn(List.of(slow, fast));
        Optional<GameRecord> best = recordService.findBest(5L, "Sudoku", 99L);

        assertThat(best).isPresent();
        assertThat(best.get().getValue()).isEqualTo(42.0);
    }

    @Test
    void testSample2() {
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(sudoku));
        when(gameRecordRepository.findByUserIdAndGameIdAndContextId(5L, 1L, 99L))
                .thenReturn(List.of());

        assertThat(recordService.findBest(5L, "Sudoku", 99L)).isEmpty();
    }

    @Test
    void testSample3() {
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(sudoku));
        GameRecord record = new GameRecord(null, sudoku, null, 15.0);
        when(gameRecordRepository.findByUserIdAndGameId(5L, 1L)).thenReturn(List.of(record));

        Optional<GameRecord> best = recordService.findBest(5L, "Sudoku", null);

        assertThat(best).contains(record);
        verify(gameRecordRepository, never())
                .findByUserIdAndGameIdAndContextId(any(), any(), any());
    }

    @Test
    void testSample4() {
        when(gameRepository.findByTitle("Tetris")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.findBest(5L, "Tetris", 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No game found");
    }

    @Test
    void testSample5() {
        User user = userWithId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(sudoku));
        when(gameRecordRepository.save(any(GameRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        GameRecord res = recordService.recordResult(5L, "Sudoku", 99L, 37.0);

        assertThat(res.getValue()).isEqualTo(37.0);
        assertThat(res.getUser()).isEqualTo(user);
        assertThat(res.getGame()).isEqualTo(sudoku);
        verify(gameRecordRepository).save(any(GameRecord.class));
    }

    @Test
    void testSample6() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.recordResult(5L, "Sudoku", 99L, 37.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("user not found");
    }

    @Test
    void testSample7() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(userWithId(5L)));
        when(gameRepository.findByTitle("Tetris")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.recordResult(5L, "Tetris", 99L, 37.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No game found");
    }

    @Test
    void testSample8() {
        User a = userWithId(1L);
        User b = userWithId(2L);
        User c = userWithId(3L);
        GameRecord aWorse = recordFor(a, 99L, 50.0);
        GameRecord aBetter = recordFor(a, 99L, 30.0);
        GameRecord bOnly = recordFor(b, 99L, 40.0);
        GameRecord cBest = recordFor(c, 99L, 20.0);
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(sudoku));
        when(gameRecordRepository.findByGameIdAndContextId(1L, 99L))
                .thenReturn(List.of(aWorse, aBetter, bOnly, cBest));

        List<GameRecord> leaderboard = recordService.findLeaderboard("Sudoku", 99L, 2);

        assertThat(leaderboard).hasSize(2);
        assertThat(leaderboard.get(0).getUser()).isEqualTo(c);
        assertThat(leaderboard.get(1).getUser()).isEqualTo(a);
        assertThat(leaderboard.get(1).getValue()).isEqualTo(30.0);
    }

    @Test
    void testSample9() {
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(sudoku));
        GameRecord record = recordFor(userWithId(1L), null, 10.0);
        when(gameRecordRepository.findByGameId(1L)).thenReturn(List.of(record));

        List<GameRecord> leaderboard = recordService.findLeaderboard("Sudoku", null, 10);

        assertThat(leaderboard).containsExactly(record);
        verify(gameRecordRepository, never()).findByGameIdAndContextId(any(), any());
    }

    @Test
    void testSample10() {
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(sudoku));
        GameRecord worse = recordFor(userWithId(1L), 99L, 25.0);
        GameRecord better = recordFor(userWithId(2L), 99L, 15.0);
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        when(gameRecordRepository.findByGameIdAndRecordedAtAfter(1L, since))
                .thenReturn(List.of(worse, better));

        List<GameRecord> leaderboard = recordService.findLeaderboardSince("Sudoku", since, 10);

        assertThat(leaderboard).extracting(GameRecord::getValue).containsExactly(15.0, 25.0);
    }

    @Test
    void testSample11() {
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(sudoku));
        when(gameRecordRepository.countByUserIdAndGameId(5L, 1L)).thenReturn(12L);

        assertThat(recordService.countRecords(5L, "Sudoku")).isEqualTo(12L);
    }

    @Test
    void testSample12() {
        when(gameRepository.findByTitle("Sudoku")).thenReturn(Optional.of(sudoku));
        GameRecord r1 = recordFor(userWithId(5L), 1L, 10.0);
        GameRecord r2 = recordFor(userWithId(5L), 2L, 20.0);
        GameRecord r3 = recordFor(userWithId(5L), 3L, 30.0);
        when(gameRecordRepository.findByUserIdAndGameIdOrderByRecordedAtDesc(5L, 1L))
                .thenReturn(List.of(r1, r2, r3));

        List<GameRecord> recent = recordService.findRecentRecords(5L, "Sudoku", 2);

        assertThat(recent).containsExactly(r1, r2);
    }

    @Test
    void testSample13() {
        GameRecord mine = recordFor(userWithId(5L), 99L, 20.0);
        mine.setId(1L);
        GameRecord worseOther = recordFor(userWithId(6L), 99L, 40.0);
        worseOther.setId(2L);
        when(gameRecordRepository.findByGameIdAndContextId(1L, 99L)).thenReturn(List.of(mine, worseOther));

        assertThat(recordService.isBestInContext(mine)).isTrue();
    }

    @Test
    void testSample14() {
        GameRecord mine = recordFor(userWithId(5L), 99L, 40.0);
        mine.setId(1L);
        GameRecord betterOther = recordFor(userWithId(6L), 99L, 20.0);
        betterOther.setId(2L);
        when(gameRecordRepository.findByGameIdAndContextId(1L, 99L)).thenReturn(List.of(mine, betterOther));

        assertThat(recordService.isBestInContext(mine)).isFalse();
    }

    @Test
    void testSample15() {
        GameRecord mine = recordFor(userWithId(5L), 99L, 20.0);
        mine.setId(1L);
        when(gameRecordRepository.findByGameIdAndContextId(1L, 99L)).thenReturn(List.of(mine));

        assertThat(recordService.isBestInContext(mine)).isTrue();
    }
}