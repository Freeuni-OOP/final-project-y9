package org.example.y9_gaming_site.game.joker;

import lombok.RequiredArgsConstructor;
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

    public void saveFinalScores(String roomCode, List<JokerPlayer> players) {
        JokerSession session = getSession(roomCode);

        for (JokerPlayer player : players) {
            // contextId = session id, same pattern as Wordle uses puzzleId
            gameRecordService.recordResult(
                    player.getUserId(),
                    GAME_KEY,
                    session.getId(),
                    (double) player.getTotalScore()
            );
        }
    }

    // --- Helper ---

    private JokerSession getSession(String roomCode) {
        return sessionRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + roomCode));
    }
}