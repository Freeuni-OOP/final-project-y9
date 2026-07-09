package org.example.y9_gaming_site.game.joker;

import lombok.RequiredArgsConstructor;
import org.example.y9_gaming_site.achievement.UnlockedAchievementDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JokerWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String ROOM_TOPIC = "/topic/joker/";

    // --- Events ---

    public record GameEvent(String type, Object data) {}

    // broadcast any event to all players in room
    private void broadcast(String roomCode, String eventType, Object data) {
        messagingTemplate.convertAndSend(
                ROOM_TOPIC + roomCode,
                new GameEvent(eventType, data)
        );
    }

    // --- Specific broadcasts ---

    public void broadcastPlayerJoined(String roomCode, JokerGameState state) {
        broadcast(roomCode, "PLAYER_JOINED", toPublicState(state));
    }

    public void broadcastGameStarted(String roomCode, JokerGameState state) {
        broadcast(roomCode, "GAME_STARTED", toPublicState(state));
    }

    public void broadcastTrumpSet(String roomCode, String trumpSuit) {
        broadcast(roomCode, "TRUMP_SET", trumpSuit);
    }

    public void broadcastBidPlaced(String roomCode, String username, int bid) {
        broadcast(roomCode, "BID_PLACED", new BidEvent(username, bid));
    }

    public void broadcastBiddingComplete(String roomCode, JokerGameState state) {
        broadcast(roomCode, "BIDDING_COMPLETE", toPublicState(state));
    }

    // FIXED: Upgraded payload to pass critical Joker execution instructions ("HIGH", "LOW", "declaredSuit")
    public void broadcastCardPlayed(String roomCode, String username, Card card, String jokerCall, String declaredSuit) {
        broadcast(roomCode, "CARD_PLAYED", new CardPlayedEvent(username, card, jokerCall, declaredSuit));
    }

    public void broadcastTrickWon(String roomCode, String winnerUsername) {
        broadcast(roomCode, "TRICK_WON", winnerUsername + " won the trick");
    }

    public void broadcastRoundEnd(String roomCode, JokerGameState state) {
        broadcast(roomCode, "ROUND_END", toPublicState(state));
    }

    public void broadcastGameOver(String roomCode, JokerGameState state) {
        broadcast(roomCode, "GAME_OVER", toPublicState(state));
    }

    // --- Public state (safe to broadcast — no private hand info) ---

    public record PublicPlayerState(
            Long userId,
            String username,
            int cardCount,
            int prophecy,
            int current,
            int totalScore,
            java.util.List<UnlockedAchievementDto> newAchievements
    ) {}

    public record PublicGameState(
            String roomCode,
            String status,
            int currentRound,
            int totalRounds,
            String trumpSuit,
            String currentPlayerUsername,
            java.util.List<PublicPlayerState> players
    ) {}

    private PublicGameState toPublicState(JokerGameState state) {
        var achievementsByUser = state.drainPendingAchievements();
        var publicPlayers = state.getPlayers().stream()
                .map(p -> new PublicPlayerState(
                        p.getUserId(),
                        p.getUsername(),
                        p.getCardList().size(),
                        p.getProphecy(),
                        p.getCurrent(), // Ensure getCurrPlayer or custom properties map cleanly here
                        p.getTotalScore(),
                        achievementsByUser.getOrDefault(p.getUserId(), java.util.List.of())
                ))
                .toList();

        return new PublicGameState(
                state.getRoom().getRoomId(),
                state.getStatus().name(),
                state.getCurrRound(),
                state.getConfig().getTotalRounds(),
                state.getTrumpSuit(),
                state.getCurrPlayer().getUsername(), // FIXED: Renamed method from getCurrentPlayer() to getCurrPlayer()
                publicPlayers
        );
    }

    // --- Event records ---

    public record BidEvent(String username, int bid) {}

    // FIXED: Expanded record payload data to preserve complete trick visibility rules for UI rendering
    public record CardPlayedEvent(String username, Card card, String jokerCall, String declaredSuit) {}
}