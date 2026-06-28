package org.example.y9_gaming_site.gameRecord;

import org.example.y9_gaming_site.game.GameRepository;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameRecordService {
    private final GameRecordRepository gameRecordRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final GameResultEvaluatorRegistry gameResultEvaluatorRegistry;

    public GameRecordService(GameRecordRepository gameRecordRepository,
                             GameRepository gameRepository,
                             UserRepository userRepository,
                             GameResultEvaluatorRegistry gameResultEvaluatorRegistry) {
        this.gameRecordRepository = gameRecordRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.gameResultEvaluatorRegistry = gameResultEvaluatorRegistry;
    }

    public GameRecord recordResult(Long userId, String gameKey, Long contextId, double value) {
        return null;
    }

    public Optional<GameRecord> findBest(Long userId, String gameKey, Long contextId) {
        return null;
    }

    public List<GameRecord> findLeaderboard(String gameKey, Long contextId, int limit) {
        return null;
    }

}

