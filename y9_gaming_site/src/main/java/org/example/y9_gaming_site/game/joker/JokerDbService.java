package org.example.y9_gaming_site.game.joker;

import lombok.RequiredArgsConstructor;
import org.example.y9_gaming_site.achievement.AchievementService;
import org.example.y9_gaming_site.achievement.UnlockedAchievementDto;
import org.example.y9_gaming_site.gameRecord.GameRecord;
import org.example.y9_gaming_site.gameRecord.GameRecordService;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JokerDbService {

    private final JokerSessionRepository sessionRepository;
    private final GameRecordService gameRecordService;
    private final UserRepository userRepository;
    private final AchievementService achievementService;

    public static final String GAME_KEY = "JOKER"; // same pattern as WordleService

    // --- Called when host creates a game ---

    public JokerSession saveNewSession(User host, String roomCode,
                                       int playerCount, int totalRounds,
                                       boolean isOpen, int jokerAmount) {
        JokerSession session = new JokerSession(
                host, roomCode, playerCount, totalRounds, isOpen, jokerAmount
        );
        return sessionRepository.save(session);
    }

    // --- Called when game status changes ---

    public void updateSessionStatus(String roomCode, String status) {
        JokerSession session = getSession(roomCode);
        session.setStatus(status);
        if (status.equals("FINISHED")) {
            session.setEndedAt(LocalDateTime.now());
        }
        sessionRepository.save(session);
    }

    // --- Called when game ends — save final scores ---

    public void saveFinalScores(String roomCode, JokerGameState state) {
        JokerSession session = getSession(roomCode);
        List<JokerPlayer> players = state.getPlayers();

        for (JokerPlayer player : players) {
            // contextId = session id, same pattern as Wordle uses puzzleId
            gameRecordService.recordResult(
                    player.getUserId(),
                    GAME_KEY,
                    session.getId(),
                    (double) player.getTotalScore()
            );
        }
        grantAchievements(state, players);
    }

    private void grantAchievements(JokerGameState state, List<JokerPlayer> players) {
        for (JokerPlayer player : players) {
            Long userId = player.getUserId();

            grant(state, userId, "JOKER_FIRST_GAME");
            if (player.getTotalScore() >= 10000) {
                grant(state, userId, "JOKER_10K");
            }
            if (player.getTotalScore() >= 20000) {
                grant(state, userId, "JOKER_20K");
            }
            if (gameRecordService.countRecords(userId, GAME_KEY) >= 50) {
                grant(state, userId, "JOKER_GAMES_50");
            }
            if (computeWinStreak(userId) >= 10) {
                grant(state, userId, "JOKER_WIN_STREAK_10");
            }

            List<Boolean> fulfilled = state.getProphecyFulfilledPerPlayer().get(userId);
            if (fulfilled != null && !fulfilled.isEmpty() && fulfilled.stream().allMatch(Boolean::booleanValue)) {
                grant(state, userId, "JOKER_PERFECT_BID");
            }
        }
    }

    private void grant(JokerGameState state, Long userId, String code) {
        achievementService.grantByCode(userId, code)
                .ifPresent(a -> state.addPendingAchievement(userId, UnlockedAchievementDto.from(a)));
    }

    private int computeWinStreak(Long userId) {
        List<GameRecord> recent = gameRecordService.findRecentRecords(userId, GAME_KEY, 10);
        int streak = 0;
        for (GameRecord record : recent) {
            if (gameRecordService.isBestInContext(record)) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    // --- Helper ---

    private JokerSession getSession(String roomCode) {
        return sessionRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + roomCode));
    }
}