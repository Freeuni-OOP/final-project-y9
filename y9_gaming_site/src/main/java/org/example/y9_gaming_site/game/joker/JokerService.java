package org.example.y9_gaming_site.game.joker;

import lombok.RequiredArgsConstructor;
import org.example.y9_gaming_site.game.joker.JokerGameConfig.PlayerCount;
import org.example.y9_gaming_site.game.joker.JokerGameConfig.RoundOption;
import org.example.y9_gaming_site.game.joker.JokerGameState.GameStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JokerService {
    private final JokerScoringService scoringService;
    private final Map<String, JokerGameState> activeGames = new ConcurrentHashMap<>();
    private final Map<String, JokerTrick> activeTricks = new ConcurrentHashMap<>();

    public JokerGameState createGame(Long hostId, String hostUsername,
                                     PlayerCount playerCount, RoundOption roundOption,
                                     int jokerAmount, boolean allowRandoms) {
        String roomId = generateRoomId();
        JokerGameConfig config = new JokerGameConfig(playerCount, allowRandoms, roundOption, jokerAmount);
        JokerRoom room = new JokerRoom(roomId);

        JokerGameState state = new JokerGameState(config, room);
        JokerPlayer host = new JokerPlayer(hostId, hostUsername);
        state.addPlayer(host);

        activeGames.put(roomId, state);
        return state;
    }

    public JokerGameState joinGame(String roomId, Long userId, String username) {
        JokerGameState state = getActiveGame(roomId);

        if (state.getStatus() != GameStatus.WAITING) {
            throw new IllegalStateException("Game already started");
        }
        if (state.isFull()) {
            throw new IllegalStateException("Game is full");
        }
        if (isAlreadyInGame(state, userId)) {
            throw new IllegalStateException("You are already in this game");
        }

        state.addPlayer(new JokerPlayer(userId, username));
        return state;
    }

    public List<JokerGameState> findOpenLobbies() {
        return activeGames.values().stream()
                .filter(s -> s.getStatus() == GameStatus.WAITING)
                .filter(s -> s.getConfig().isAllowRandoms())
                .filter(s -> !s.isFull())
                .toList();
    }

    public void invitePlayer(String roomId, Long inviterId, Long receiverId) {
        JokerGameState state = getActiveGame(roomId);

        if (state.getStatus() != GameStatus.WAITING) {
            throw new IllegalStateException("Game already started, cannot invite");
        }
        if (state.isFull()) {
            throw new IllegalStateException("Game is full");
        }
        if (!isAlreadyInGame(state, inviterId)) {
            throw new IllegalStateException("Only players in the lobby can invite");
        }
    }

    public void startGame(String roomId, Long hostId) {
        JokerGameState state = getActiveGame(roomId);

        if (!isHost(state, hostId)) {
            throw new IllegalStateException("Only the host can start the game");
        }
        if (!state.isFull()) {
            throw new IllegalStateException("Lobby is not full yet");
        }
        if (state.getStatus() != GameStatus.WAITING) {
            throw new IllegalStateException("Game already started");
        }

        state.startNextRound();
        prepareNewTrick(roomId, state);
    }

    public void placeBid(String roomId, Long userId, int bid) {
        JokerGameState state = getActiveGame(roomId);

        if (state.getStatus() != GameStatus.BIDDING) {
            throw new IllegalStateException("Not in bidding phase");
        }
        if (!state.getCurrPlayer().getUserId().equals(userId)) {
            throw new IllegalStateException("It's not your turn to bid");
        }
        if (bid < 0) {
            throw new IllegalArgumentException("Bid cannot be negative");
        }

        int totalCardsDealt = state.cardsForRound(state.getCurrRound());
        if (bid > totalCardsDealt) {
            throw new IllegalArgumentException("Bid cannot exceed total cards dealt: " + totalCardsDealt);
        }

        // FIXED: Check that sum of ALL player prophecies doesn't equal total available cards
        boolean isLastPlayerToBid = (state.getCurrPlayer() == state.getPlayers().get(state.getDealer()));
        if (isLastPlayerToBid) {
            // Filter out everyone except the current dealer to sum the existing bids
            int runningBidsSum = state.getPlayers().stream()
                    .filter(p -> !p.getUserId().equals(userId))
                    .mapToInt(JokerPlayer::getProphecy)
                    .filter(p -> p >= 0).sum();

            if (runningBidsSum + bid == totalCardsDealt) {
                throw new IllegalArgumentException("Dealer's bid cannot make total bids equal total cards in play!");
            }
        }

        JokerPlayer player = getPlayerFromGame(state, userId);
        player.setProphecy(bid);
        state.turn();

        // Check if all players completed their bidding sequence
        boolean allBidded = state.getPlayers().stream().allMatch(p -> p.getProphecy() >= 0);
        if (allBidded) {
            // Shift the state cleanly to playing phase
            try {
                java.lang.reflect.Field statusField = JokerGameState.class.getDeclaredField("status");
                statusField.setAccessible(true);
                statusField.set(state, GameStatus.PLAYING);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to update game status state context", e);
            }
        }
    }

    public JokerTrick playCard(String roomId, Long userId, String suit, Integer value, String jokerCall, String declaredSuit) {
        JokerGameState state = getActiveGame(roomId);

        if (state.getStatus() != GameStatus.PLAYING) {
            throw new IllegalStateException("Not in playing phase");
        }
        if (!state.getCurrPlayer().getUserId().equals(userId)) {
            throw new IllegalStateException("It's not your turn");
        }

        JokerPlayer player = getPlayerFromGame(state, userId);
        Card card = findCardInHand(player, suit, value);
        JokerTrick currentTrick = activeTricks.get(roomId);

        if (currentTrick == null) {
            throw new IllegalStateException("Trick tracking instance missing");
        }

        if (!currentTrick.isValCard(player, card, jokerCall, declaredSuit)) {
            throw new IllegalStateException("Invalid card play based on rules hierarchy");
        }

        currentTrick.playCard(player, card, jokerCall, declaredSuit);

        // Evaluate complete trick
        if (currentTrick.isComplete(state.getPlayers().size())) {
            JokerPlayer winner = currentTrick.winner();
            winner.tricksTaken();

            // FIXED: Cleanly match current turn indicator index directly to the trick winner
            int winnerIdx = state.getPlayers().indexOf(winner);
            try {
                java.lang.reflect.Field currPlayerField = JokerGameState.class.getDeclaredField("currPlayer");
                currPlayerField.setAccessible(true);
                currPlayerField.set(state, winnerIdx);
            } catch (Exception e) {
                // Fallback turn loop matching logic if reflection access controls reject fields modification
                while (!state.getCurrPlayer().getUserId().equals(winner.getUserId())) {
                    state.turn();
                }
            }

            if (state.isRoundOver()) {
                // FIXED: Matched method name to state.recordRoundRes
                state.recordRoundRes(scoringService);
                state.endRound();

                if (!state.isGameOver()) {
                    state.startNextRound();
                    prepareNewTrick(roomId, state);
                }
            } else {
                prepareNewTrick(roomId, state);
            }
        } else {
            state.turn();
        }

        return currentTrick;
    }

    public void setTrumpSuit(String roomId, Long userId, String suit) {
        JokerGameState state = getActiveGame(roomId);

        if (!"NONE".equals(state.getTrumpSuit()) && state.getTrumpSuit() != null) {
            throw new IllegalStateException("Trump suit already set");
        }

        JokerPlayer dealer = state.getPlayers().get(state.getDealer());
        if (!dealer.getUserId().equals(userId)) {
            throw new IllegalStateException("Only the dealer can set the trump suit");
        }

        state.setTrumpSuit(suit);
        prepareNewTrick(roomId, state);
    }

    public JokerGameState getGameState(String roomId) {
        return getActiveGame(roomId);
    }

    private void prepareNewTrick(String roomId, JokerGameState state) {
        activeTricks.put(roomId, new JokerTrick(state.getTrumpSuit()));
    }

    private JokerGameState getActiveGame(String roomId) {
        JokerGameState state = activeGames.get(roomId);
        if (state == null) throw new IllegalArgumentException("Game not found: " + roomId);
        return state;
    }

    private boolean isAlreadyInGame(JokerGameState state, Long userId) {
        return state.getPlayers().stream().anyMatch(p -> p.getUserId().equals(userId));
    }

    private boolean isHost(JokerGameState state, Long userId) {
        return state.getPlayers().get(0).getUserId().equals(userId);
    }

    private JokerPlayer getPlayerFromGame(JokerGameState state, Long userId) {
        return state.getPlayers().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not in this game"));
    }

    private Card findCardInHand(JokerPlayer player, String suit, Integer value) {
        return player.getCardList().stream()
                .filter(c -> (c.getIsJoker() && value.equals(c.getValue())) ||
                        (!c.getIsJoker() && c.getSuit().equals(suit) && c.getValue().equals(value)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Card not found in hand (" + suit + ", " + value + ")"));
    }

    private String generateRoomId() {
        return "JOKER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}