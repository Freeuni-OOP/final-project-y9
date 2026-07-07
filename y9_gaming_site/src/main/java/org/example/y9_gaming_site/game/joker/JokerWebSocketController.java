package org.example.y9_gaming_site.game.joker;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class JokerWebSocketController {

    private final JokerService jokerService;
    private final JokerWebSocketService jokerWebSocketService;

    public record SocketBidPayload(int bid) {}

    public record SocketPlayPayload(
            String suit,
            Integer value,
            String jokerCall,
            String declaredSuit
    ) {}

    // 1. Listen for players placing a bid via WebSocket
    @MessageMapping("/joker/{roomCode}/bid")
    public void handleWebSocketBid(
            @DestinationVariable String roomCode,
            @Payload SocketBidPayload payload,
            Principal principal
    ) {
        // Safe extraction of username from the authenticated WebSocket session connection
        String username = principal.getName();

        // Fetch current game state to map username securely back to user ID parameters
        JokerGameState state = jokerService.getGameState(roomCode);
        JokerPlayer player = state.getPlayers().stream()
                .filter(p -> p.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not in session room"));

        // Process game bid rule logic changes inside core engine
        jokerService.placeBid(roomCode, player.getUserId(), payload.bid());

        // Broadcast move updates instantly to all table screens
        jokerWebSocketService.broadcastBidPlaced(roomCode, username, payload.bid());

        // Automatically shift stage when player sequences fulfill bidding state
        if (state.getStatus() == JokerGameState.GameStatus.PLAYING) {
            jokerWebSocketService.broadcastBiddingComplete(roomCode, state);
        }
    }

    // 2. Listen for players throwing a card via WebSocket
    @MessageMapping("/joker/{roomCode}/play")
    public void handleWebSocketPlayCard(
            @DestinationVariable String roomCode,
            @Payload SocketPlayPayload payload,
            Principal principal
    ) {
        String username = principal.getName();
        JokerGameState state = jokerService.getGameState(roomCode);

        JokerPlayer player = state.getPlayers().stream()
                .filter(p -> p.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not in session room"));

        // Track state factors before the move executes to check if a trick/round terminates
        int currentRoundBeforeMove = state.getCurrRound();

        // Pass payload down into the engine rule validations
        Card playedCard = player.getCardList().stream()
                .filter(c -> (c.getIsJoker() && payload.value().equals(c.getValue())) ||
                        (!c.getIsJoker() && c.getSuit().equals(payload.suit()) && c.getValue().equals(payload.value())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Card not in hand context"));

        JokerTrick updatedTrick = jokerService.playCard(
                roomCode, player.getUserId(),
                payload.suit(), payload.value(),
                payload.jokerCall(), payload.declaredSuit()
        );

        // Broadcast specific card execution variables to opponents
        jokerWebSocketService.broadcastCardPlayed(roomCode, username, playedCard, payload.jokerCall(), payload.declaredSuit());

        // Check if the card completed a trick cycle
        if (updatedTrick.winner() != null) {
            // The trick completed and a winner took it!
            String roundWinner = updatedTrick.winner().getUsername();
            jokerWebSocketService.broadcastTrickWon(roomCode, roundWinner);

            // Check if the move also terminated the current round profile
            if (state.getCurrRound() > currentRoundBeforeMove || state.getStatus() == JokerGameState.GameStatus.ROUND_END) {
                jokerWebSocketService.broadcastRoundEnd(roomCode, state);
            } else if (state.getStatus() == JokerGameState.GameStatus.FINISHED) {
                jokerWebSocketService.broadcastGameOver(roomCode, state);
            }
        }
    }
}