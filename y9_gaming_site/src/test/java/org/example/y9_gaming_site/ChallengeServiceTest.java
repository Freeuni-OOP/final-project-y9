package org.example.y9_gaming_site;

import org.example.y9_gaming_site.Challenge.GameChallengeRepository;
import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.game.Game;
import org.example.y9_gaming_site.gameRecord.GameRecord;
import org.example.y9_gaming_site.gameRecord.GameRecordService;
import org.example.y9_gaming_site.gameRecord.GameResultEvaluator;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    private GameResultEvaluatorRegistry  gameResultEvaluatorRegistry;
    private GameResultEvaluator gameResultEvaluator;

    private User sender;
    private User receiver;
    private Game sudokuGame;
    private GameRecord gameRecord;

    private final GameResultEvaluator lowerWins = new GameResultEvaluator() {
        @Override
        public String getGameKey() {
            return "SUDOKU";
        }

        @Override
        public boolean isBetter(double candidateValue, double currentBestValue) {
            return candidateValue < currentBestValue;
        }
    };

    @BeforeEach
    void setUp() {
        gameResultEvaluatorRegistry = new GameResultEvaluatorRegistry(List.of(lowerWins));
    }
}
