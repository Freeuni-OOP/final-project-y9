package org.example.y9_gaming_site.gameRecord;

import org.example.y9_gaming_site.game.Game;
import org.example.y9_gaming_site.game.GameRepository;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        User user  = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user not found"));
        Game game  = gameRepository.findByTitle(gameKey).orElseThrow(() -> new RuntimeException("No game found"));

        GameRecord record = new GameRecord(user, game, contextId, value);
        return gameRecordRepository.save(record);
    }

    public Optional<GameRecord> findBest(Long userId, String gameKey, Long contextId) {
        Game game = gameRepository.findByTitle(gameKey).orElseThrow(() -> new RuntimeException("No game found"));
        List<GameRecord> candidates;
        if(contextId == null) {
            candidates = gameRecordRepository.findByUserIdAndGameId(userId, game.getId());
        }else {
            candidates = gameRecordRepository.findByUserIdAndGameIdAndContextId(userId, game.getId(), contextId);
        }

        if(candidates.isEmpty()) {
            return Optional.empty();
        }

        GameResultEvaluator evaluator = gameResultEvaluatorRegistry.resolve(gameKey);
        GameRecord record = candidates.get(0);
        for(GameRecord gameRecord : candidates) {
            if(evaluator.isBetter(gameRecord.getValue(), record.getValue())) {
                record = gameRecord;
            }

        }
        return Optional.of(record);
    }

    public List<GameRecord> findLeaderboard(String gameKey, Long contextId, int limit) {
        Game game = gameRepository.findByTitle(gameKey).orElseThrow(() -> new RuntimeException("No game found"));
        List<GameRecord> candidates;
        if(contextId == null) {
            candidates = gameRecordRepository.findByGameId(game.getId());
        }else {
            candidates = gameRecordRepository.findByGameIdAndContextId(game.getId(), contextId);
        }

        GameResultEvaluator evaluator = gameResultEvaluatorRegistry.resolve(gameKey);
        Comparator<GameRecord> comparator = (a, b) -> {
            if (evaluator.isBetter(a.getValue(), b.getValue())) return -1;
            if(evaluator.isBetter(b.getValue(), a.getValue())) return 1;
            return 0;
        };

        return candidates.stream().sorted(comparator).limit(limit).collect(Collectors.toList());
    }

    public long countRecords(Long userId, String gameKey) {
        Game game = gameRepository.findByTitle(gameKey).orElseThrow(() -> new RuntimeException("No game found"));
        return gameRecordRepository.countByUserIdAndGameId(userId, game.getId());
    }

    public List<GameRecord> findRecentRecords(Long userId, String gameKey, int limit) {
        Game game = gameRepository.findByTitle(gameKey).orElseThrow(() -> new RuntimeException("No game found"));
        return gameRecordRepository.findByUserIdAndGameIdOrderByRecordedAtDesc(userId, game.getId())
                .stream().limit(limit).toList();
    }

    public boolean isBestInContext(GameRecord record) {
        List<GameRecord> all = gameRecordRepository.findByGameIdAndContextId(record.getGame().getId(), record.getContextId());
        GameResultEvaluator evaluator = gameResultEvaluatorRegistry.resolve(record.getGame().getTitle());
        for (GameRecord other : all) {
            if (other.getId().equals(record.getId())) continue;
            if (evaluator.isBetter(other.getValue(), record.getValue())) {
                return false;
            }
        }
        return true;
    }

}

