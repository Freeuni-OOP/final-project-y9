package org.example.y9_gaming_site.game.joker;

import lombok.RequiredArgsConstructor;
import org.example.y9_gaming_site.game.joker.JokerGameConfig.PlayerCount;
import org.example.y9_gaming_site.game.joker.JokerGameConfig.RoundOption;
import org.example.y9_gaming_site.game.joker.JokerGameState.GameStatus;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JokerService {

    private final JokerScoringService scoringService;
    private final JokerDbService jokerDbService;
    private final UserRepository userRepository;
    private final Map<String, JokerGameState> activeGames = new ConcurrentHashMap<>();

    public JokerGameState createGame(Long hostId, String hostUsername,
                                     PlayerCount playerCount, RoundOption roundOption,
                                     int jokerAmount, boolean allowRandoms) {
        String roomCode = generateRoomCode();
        JokerGameConfig config = new JokerGameConfig(playerCount, allowRandoms, roundOption, jokerAmount);
        JokerRoom room = new JokerRoom(roomCode);

        JokerGameState state = new JokerGameState(config, room);
        JokerPlayer host = new JokerPlayer(hostId, hostUsername);
        state.addPlayer(host);
        activeGames.put(roomCode, state);

        User hostUser = userRepository.findById(hostId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        jokerDbService.saveNewSession(
                hostUser, roomCode,
                config.getPlayers(),
                config.getTotalRounds(),
                allowRandoms,
                jokerAmount
        );

        return state;
    }

    public JokerGameState joinGame(String roomCode, Long userId, String username) {
        JokerGameState state = getActiveGame(roomCode);

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

    public void invitePlayer(String roomCode, Long inviterId, Long receiverId) {
        JokerGameState state = getActiveGame(roomCode);

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

    public void startGame(String roomCode, Long hostId) {
        JokerGameState state = getActiveGame(roomCode);

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
        jokerDbService.updateSessionStatus(roomCode, "IN_PROGRESS");
    }

    public void placeBid(String roomCode, Long userId, int bid) {
        JokerGameState state = getActiveGame(roomCode);

        if (state.getStatus() != GameStatus.BIDDING) {
            throw new IllegalStateException("Not in bidding phase");
        }
        if (!state.getCurrPlayer().getUserId().equals(userId)) {
            throw new IllegalStateException("It's not your turn to bid");
        }
        if (bid < 0) {
            throw new IllegalArgumentException("Bid cannot be negative");
        }

        int totalCards = state.cardsForRound(state.getCurrRound());
        if (bid > totalCards) {
            throw new IllegalArgumentException("Bid cannot exceed total cards dealt: " + totalCards);
        }

        // 1. ვითვლით, რამდენი მოთამაშეს აქვს უკვე ნათქვამი ბიდი (ვისი პროფეციაც >= 0)
        List<JokerPlayer> players = state.getPlayers();
        long biddedCount = players.stream().filter(p -> p.getProphecy() >= 0).count();

        // 2. თუ ეს მოთამაშე არის ბოლო (ანუ თამაშში მყოფთაგან ყველამ თქვა მის გარდა)
        if (biddedCount == players.size() - 1) {
            int existingBidsSum = players.stream()
                    .filter(p -> p.getProphecy() >= 0) // მხოლოდ მათ, ვინც უკვე თქვა
                    .mapToInt(JokerPlayer::getProphecy)
                    .sum();

            // 3. თუ მიმდინარე ბიდის დამატებით ჯამი ზუსტად გაუტოლდება კარტების რაოდენობას
            if (existingBidsSum + bid == totalCards) {
                int forbiddenBid = totalCards - existingBidsSum;
                throw new IllegalArgumentException(forbiddenBid + "-ს გარდა ხარ!");
            }
        }

        // თუ ვალიდაციამ წარმატებით გაიარა:
        JokerPlayer player = getPlayerFromGame(state, userId);
        player.setProphecy(bid);
        state.turn();

        if (state.allPlayersBidded()) {
            state.setStatus(GameStatus.PLAYING);
        }
    }

    public JokerTrick playCard(String roomCode, Long userId, String suit, Integer value,
                               String jokerCall, String declaredSuit) {
        JokerGameState state = getActiveGame(roomCode);

        if (state.getStatus() != GameStatus.PLAYING) {
            throw new IllegalStateException("Not in playing phase");
        }
        if (!state.getCurrPlayer().getUserId().equals(userId)) {
            throw new IllegalStateException("It's not your turn");
        }

        JokerPlayer player = getPlayerFromGame(state, userId);
        Card card = findCardInHand(player, suit, value);
        JokerTrick currentTrick = state.getCurrentTrick();

        currentTrick.playCard(player, card, jokerCall, declaredSuit);

        if (currentTrick.isComplete(state.getPlayers().size())) {
            JokerPlayer winner = currentTrick.winner();
            winner.tricksTaken();
            state.finishTrick(winner);

            if (state.isRoundOver()) {
                state.recordRoundRes(scoringService);
                state.endRound();

                if (state.isGameOver()) {
                    jokerDbService.saveFinalScores(roomCode, state); // for achievement
                    jokerDbService.updateSessionStatus(roomCode, "FINISHED");
                } else {
                    state.startNextRound();
                }
            }
        } else {
            state.turn();
        }

        return currentTrick;
    }

    public void setTrumpSuit(String roomCode, Long userId, String suit) {
        JokerGameState state = getActiveGame(roomCode);

        JokerPlayer dealer = state.getPlayers().get(state.getDealer());
        if (!dealer.getUserId().equals(userId)) {
            throw new IllegalStateException("Only the dealer can set the trump suit");
        }

        state.setTrumpSuit(suit);
    }

    public JokerGameState getGameState(String roomCode) {
        return getActiveGame(roomCode);
    }

    // --- Helpers ---

    private JokerGameState getActiveGame(String roomCode) {
        JokerGameState state = activeGames.get(roomCode);
        if (state == null) throw new IllegalArgumentException("Game not found: " + roomCode);
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
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + suit + ", " + value));
    }

    private String generateRoomCode() {
        return "JOKER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}