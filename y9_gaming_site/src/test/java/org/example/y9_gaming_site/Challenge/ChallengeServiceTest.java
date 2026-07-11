package org.example.y9_gaming_site.Challenge;

import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.game.Game;
import org.example.y9_gaming_site.gameRecord.GameRecord;
import org.example.y9_gaming_site.gameRecord.GameRecordService;
import org.example.y9_gaming_site.gameRecord.GameResultEvaluator;
import org.example.y9_gaming_site.gameRecord.GameResultEvaluatorRegistry;
import org.example.y9_gaming_site.notification.NotificationService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChallengeServiceTest {
    @Mock
    private GameChallengeRepository gameChallengeRepository;
    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRecordService gameRecordService;
    @Mock
    private NotificationService notificationService;

    private GameResultEvaluatorRegistry gameResultEvaluatorRegistry;
    private GameChallengeService challengeService;

    private User sender;
    private User receiver;
    private Game sudokuGame;
    private GameRecord gameRecord;

    private final GameResultEvaluator lowerWins = new GameResultEvaluator() {
        @Override
        public String getGameKey() {
            return "Sudoku";
        }

        @Override
        public boolean isBetter(double candidateValue, double currentBestValue) {
            return candidateValue < currentBestValue;
        }
    };

    @BeforeEach
    void setUp() {
        gameResultEvaluatorRegistry = new GameResultEvaluatorRegistry(List.of(lowerWins));
        challengeService = new GameChallengeService(gameChallengeRepository,
                userRepository,friendshipRepository ,gameRecordService, gameResultEvaluatorRegistry, notificationService);
        sender = new User();
        sender.setId(1L);
        receiver = new User();
        receiver.setId(2L);

        sudokuGame = new Game();
        sudokuGame.setId(1L);
        sudokuGame.setTitle("Sudoku");
        gameRecord = new GameRecord(sender, sudokuGame, 99L, 60.0);
    }

    @Test
    public void testSample1(){ // sending challenge works with correct target
        when(friendshipRepository.findBySenderIdAndReceiverId(1L, 2L))
                .thenReturn(new Friendship(1L, 2L, "ACCEPTED"));
        when(gameRecordService.findBest(1L, "Sudoku", 99L))
                .thenReturn(Optional.of(gameRecord));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(gameChallengeRepository.save(any(GameChallenge.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        GameChallenge result = challengeService.sendChallenge(1L, 2L, "Sudoku", 99L);

        assertThat(result.getStatus()).isEqualTo(GameChallengeStatus.PENDING);
        assertThat(result.getTargRecord()).isEqualTo(gameRecord);
        assertThat(result.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(result.getSender()).isEqualTo(sender);
        assertThat(result.getReceiver()).isEqualTo(receiver);
    }

    @Test
    public void testSample1b(){ // being friends the other way around still counts
        when(friendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(friendshipRepository.findBySenderIdAndReceiverId(2L, 1L))
                .thenReturn(new Friendship(2L, 1L, "ACCEPTED"));
        when(gameRecordService.findBest(1L, "Sudoku", 99L)).thenReturn(Optional.of(gameRecord));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(gameChallengeRepository.save(any(GameChallenge.class))).thenAnswer(inv -> inv.getArgument(0));

        GameChallenge result = challengeService.sendChallenge(1L, 2L, "Sudoku", 99L);

        assertThat(result.getStatus()).isEqualTo(GameChallengeStatus.PENDING);
    }

    @Test
    public void testSample2(){ // respond To challenge which was declined
        GameChallenge gameChallenge = new GameChallenge(sender, receiver, gameRecord, LocalDateTime.now().plusDays(7));
        gameChallenge.setId(10L);
        when(gameChallengeRepository.findById(10L)).thenReturn(Optional.of(gameChallenge));
        when(gameChallengeRepository.save(any(GameChallenge.class))).thenAnswer(inv -> inv.getArgument(0));

        GameChallenge res = challengeService.respondToChallenge(10L, 2L, false);
        assertThat(res.getStatus()).isEqualTo(GameChallengeStatus.DECLINED);
        assertThat(res.getResolvedAt()).isNotNull();

    }

    @Test
    public void testSample3(){// Respond to Challenge which was expired
        GameChallenge challenge = new GameChallenge(sender, receiver, gameRecord, LocalDateTime.now().minusMinutes(1));
        challenge.setId(10L);
        when(gameChallengeRepository.findById(10L)).thenReturn(Optional.of(challenge));
        when(gameChallengeRepository.save(any(GameChallenge.class))).thenAnswer(inv -> inv.getArgument(0));

        GameChallenge res = challengeService.respondToChallenge(10L, 2L, true);
        assertThat(res.getStatus()).isEqualTo(GameChallengeStatus.EXPIRED);
    }

    @Test
    public void testSample4(){// receiver winning case
        GameChallenge challenge = new GameChallenge(sender, receiver, gameRecord, LocalDateTime.now().plusDays(1));
        challenge.setId(10L);
        challenge.setStatus(GameChallengeStatus.ACCEPTED);
        when(gameChallengeRepository.findById(10L)).thenReturn(Optional.of(challenge));
        when(gameChallengeRepository.save(any(GameChallenge.class))).thenAnswer(inv -> inv.getArgument(0));

        GameRecord winner = new GameRecord(receiver, sudokuGame, 99L, 42.0);
        when(gameRecordService.recordResult(2L, "Sudoku", 99L, 42.0)).thenReturn(winner);

        GameChallenge result = challengeService.submitAttempt(10L, 2L, 42.0, 99L);
        assertThat(result.getStatus()).isEqualTo(GameChallengeStatus.COMPLETED);
        assertThat(result.getWinner()).isEqualTo(receiver);
        assertThat(result.getResRecord()).isEqualTo(winner);
    }

    @Test
    public void testSample5(){ // tie case
        GameChallenge challenge = new GameChallenge(sender, receiver, gameRecord, LocalDateTime.now().plusDays(1));
        challenge.setId(10L);
        challenge.setStatus(GameChallengeStatus.ACCEPTED);
        when(gameChallengeRepository.findById(10L)).thenReturn(Optional.of(challenge));
        when(gameChallengeRepository.save(any(GameChallenge.class))).thenAnswer(inv -> inv.getArgument(0));

        GameRecord tie = new GameRecord(receiver, sudokuGame, 99L, 60.0);
        when(gameRecordService.recordResult(2L, "Sudoku", 99L, 60.0)).thenReturn(tie);
        GameChallenge res = challengeService.submitAttempt(10L, 2L, 60.0, 99L);

        assertThat(res.getWinner()).isEqualTo(sender);
    }

    @Test
    public void testSample6(){ // test inbox
        GameChallenge challenge = new GameChallenge(sender, receiver, gameRecord, LocalDateTime.now().minusMinutes(1));
        challenge.setId(20L);
        List<GameChallenge> expected = List.of(challenge);
        when(gameChallengeRepository.findByReceiverIdAndStatus(2L, GameChallengeStatus.PENDING)).thenReturn(expected);
        List<GameChallenge> result = challengeService.getInbox(2L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testSample7(){// test history
        GameChallenge challenge = new GameChallenge(sender, receiver, gameRecord, LocalDateTime.now().plusDays(1));
        challenge.setId(21L);
        challenge.setStatus(GameChallengeStatus.COMPLETED);
        List<GameChallenge> expected = List.of(challenge);
        when(gameChallengeRepository.findBySenderIdOrReceiverId(1L, 1L)).thenReturn(expected);
        List<GameChallenge> result = challengeService.getHistory(1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testSample8(){ // test expiring
        GameChallenge challenge = new GameChallenge(sender, receiver, gameRecord, LocalDateTime.now().minusHours(2));
        challenge.setId(22L);
        challenge.setStatus(GameChallengeStatus.PENDING);
        GameChallenge challenge1 = new GameChallenge(sender, receiver, gameRecord, LocalDateTime.now().minusMinutes(5));
        challenge1.setId(23L);
        challenge1.setStatus(GameChallengeStatus.PENDING);
        when(gameChallengeRepository.findByStatusAndExpiresAtBefore(eq(GameChallengeStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(challenge, challenge1));
        when(gameChallengeRepository.save(any(GameChallenge.class))).thenAnswer(inv -> inv.getArgument(0));
        challengeService.expireStaleChallenges();

        assertThat(challenge.getStatus()).isEqualTo(GameChallengeStatus.EXPIRED);
        assertThat(challenge.getResolvedAt()).isNotNull();
        assertThat(challenge1.getStatus()).isEqualTo(GameChallengeStatus.EXPIRED);
        assertThat(challenge1.getResolvedAt()).isNotNull();
        verify(gameChallengeRepository, times(2)).save(any(GameChallenge.class));
    }

    @Test
    public void testSample9(){ // sendChallenge refuses strangers
        when(friendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(friendshipRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(null);

        assertThatThrownBy(() -> challengeService.sendChallenge(1L, 2L, "Sudoku", 99L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("friends");
    }

    @Test
    public void testSample10(){ // sendChallenge refuses when the sender has no record to challenge with
        when(friendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(new Friendship(1L, 2L, "ACCEPTED"));
        when(gameRecordService.findBest(1L, "Sudoku", 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> challengeService.sendChallenge(1L, 2L, "Sudoku", 99L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("record");
    }

    @Test
    public void testSample11(){ // sendChallenge refuses when the receiver doesnt exist
        when(friendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(new Friendship(1L, 2L, "ACCEPTED"));
        when(gameRecordService.findBest(1L, "Sudoku", 99L)).thenReturn(Optional.of(gameRecord));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> challengeService.sendChallenge(1L, 2L, "Sudoku", 99L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("User");
    }

    @Test
    public void testSample12(){ // respondToChallenge refuses an unknown challenge id
        when(gameChallengeRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> challengeService.respondToChallenge(100L, 2L, true))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Challenge");
    }

    @Test
    public void testSample13(){ // respondToChallenge refuses anyone but the actual receiver
        GameChallenge challenge = new GameChallenge(sender, receiver, gameRecord, LocalDateTime.now().plusDays(1));
        challenge.setId(101L);
        when(gameChallengeRepository.findById(101L)).thenReturn(Optional.of(challenge));
        assertThatThrownBy(() -> challengeService.respondToChallenge(101L, 943L, true))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("receiver");
    }

    @Test
    public void testSample14(){ // submitAttempt refuses a challenge that hasnt been accepted yet
        GameChallenge challenge1 = new GameChallenge(sender, receiver, gameRecord, LocalDateTime.now().plusDays(1));
        challenge1.setId(102L);
        when(gameChallengeRepository.findById(102L)).thenReturn(Optional.of(challenge1));

        assertThatThrownBy(() -> challengeService.submitAttempt(102L, 2L, 42.0, 99L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("waiting");
    }

    @Test
    public void testSample15(){ // submitAttempt on a since expired challenge auto expires it and refuses
        GameChallenge nowExpired = new GameChallenge(sender, receiver, gameRecord, LocalDateTime.now().minusMinutes(1));
        nowExpired.setId(103L);
        nowExpired.setStatus(GameChallengeStatus.ACCEPTED);
        when(gameChallengeRepository.findById(103L)).thenReturn(Optional.of(nowExpired));
        when(gameChallengeRepository.save(any(GameChallenge.class))).thenAnswer(inv -> inv.getArgument(0));
        assertThatThrownBy(() -> challengeService.submitAttempt(103L, 2L, 42.0, 99L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("expired");
        assertThat(nowExpired.getStatus()).isEqualTo(GameChallengeStatus.EXPIRED);
    }

    @Test
    public void testSample16(){ // submitAttempt refuses when theres no evaluator registered for the target game
        Game tetris = new Game();
        tetris.setId(2L);
        tetris.setTitle("Tetris");
        GameRecord tetrisTarget = new GameRecord(sender, tetris, null, 100.0);
        GameChallenge unknownGameChallenge = new GameChallenge(sender, receiver, tetrisTarget, LocalDateTime.now().plusDays(1));
        unknownGameChallenge.setId(104L);
        unknownGameChallenge.setStatus(GameChallengeStatus.ACCEPTED);
        when(gameChallengeRepository.findById(104L)).thenReturn(Optional.of(unknownGameChallenge));
        assertThatThrownBy(() -> challengeService.submitAttempt(104L, 2L, 150.0, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Tetris");
    }

}
