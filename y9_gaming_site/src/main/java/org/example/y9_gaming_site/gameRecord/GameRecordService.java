package org.example.y9_gaming_site.gameRecord;

import org.example.y9_gaming_site.game.Game;
import org.example.y9_gaming_site.game.GameRepository;
import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
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
            candidates = new java.util.ArrayList<>(gameRecordRepository.findByGameId(game.getId()));
        }else {
            candidates = new java.util.ArrayList<>(gameRecordRepository.findByGameIdAndContextId(game.getId(), contextId));
        }
        candidates.removeIf(gameRecord -> gameRecord.getUser().getRole() == Role.GUEST);
        return sortedByEvaluator(candidates, gameKey, limit);
    }

    // same as findLeaderboard but only counting records from limit
    public List<GameRecord> findLeaderboardSince(String gameKey, LocalDateTime since, int limit) {
        Game game = gameRepository.findByTitle(gameKey).orElseThrow(() -> new RuntimeException("No game found"));
        List<GameRecord> candidates = new java.util.ArrayList<>(gameRecordRepository.findByGameIdAndRecordedAtAfter(game.getId(), since));
        candidates.removeIf(gameRecord -> gameRecord.getUser().getRole() == Role.GUEST);
        return sortedByEvaluator(candidates, gameKey, limit);
    }

    private List<GameRecord> sortedByEvaluator(List<GameRecord> candidates, String gameKey, int limit) {
        GameResultEvaluator evaluator = gameResultEvaluatorRegistry.resolve(gameKey);
        Comparator<GameRecord> comparator = (a, b) -> {
            if (evaluator.isBetter(a.getValue(), b.getValue())) return -1;
            if(evaluator.isBetter(b.getValue(), a.getValue())) return 1;
            return 0;
        };

        Collection<GameRecord> bestPerUser = candidates.stream()
                .collect(Collectors.toMap(
                        record -> record.getUser().getId(),
                        record -> record,
                        (a, b) -> comparator.compare(a, b) <= 0 ? a : b
                ))
                .values();

        return bestPerUser.stream().sorted(comparator).limit(limit).collect(Collectors.toList());
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

