package org.example.y9_gaming_site.game.joker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.example.y9_gaming_site.achievement.UnlockedAchievementDto;

import java.util.*;

@Getter
public class JokerGameState {
    public enum GameStatus {WAITING, BIDDING, PLAYING, ROUND_END, FINISHED}

    private final JokerGameConfig config;
    private final List<JokerPlayer> players = new ArrayList<>();
    private final JokerRoom room;

    @JsonIgnore
    private final Map<Long, List<Integer>> roundScoresPerPlayer = new HashMap<>();
    @JsonIgnore
    private final Map<Long, List<Boolean>> prophecyFulfilledPerPlayer = new HashMap<>();
    @JsonIgnore
    private final List<Card> activeDeck = new ArrayList<>();
    @JsonIgnore
    private JokerTrick currentTrick = null;
    @JsonIgnore
    private final Map<Long, List<UnlockedAchievementDto>> pendingAchievements = new HashMap<>();

    private GameStatus status = GameStatus.WAITING;
    private int currRound = 0;
    private int currPlayer = 0;
    private int dealer = 0;
    private String trumpSuit = null;

    public JokerGameState(JokerGameConfig config, JokerRoom room) {
        this.config = config;
        this.room = room;
        this.room.setPlayerCount(config.getPlayers());
        this.room.setJokerAmount(config.getJokerAmount());
    }

    public void addPlayer(JokerPlayer player) {
        if (players.size() >= config.getPlayers()) {
            throw new IllegalStateException("Game is already full");
        }
        players.add(player);
        roundScoresPerPlayer.put(player.getUserId(), new ArrayList<>());
        prophecyFulfilledPerPlayer.put(player.getUserId(), new ArrayList<>());
    }

    public boolean isFull() {
        return players.size() == config.getPlayers();
    }

    public void startNextRound() {
        if (!isFull()) throw new IllegalStateException("Not enough players to start");
        if (currRound >= config.getTotalRounds()) throw new IllegalStateException("Game already finished");

        currRound++;
        dealer = (currRound - 1) % players.size();
        currPlayer = (dealer + 1) % players.size();

        players.forEach(JokerPlayer::resetRoundInfo);
        room.shuffle();
        activeDeck.clear();
        activeDeck.addAll(room.getDeck());
        dealCards();
        determineTrumpSuit();
        currentTrick = new JokerTrick(trumpSuit); // fresh trick for new round
        status = GameStatus.BIDDING;
    }

    public int cardsForRound(int round) {
        if (config.getRoundOption() == JokerGameConfig.RoundOption.QUICK_4) return 9;
        if (config.getRoundOption() == JokerGameConfig.RoundOption.SHORT_8) return round;
        if (config.getRoundOption() == JokerGameConfig.RoundOption.FULL_24) {
            if (round <= 8) return round;
            if (round <= 12) return 9;
            if (round <= 20) return 21 - round;
            return 9;
        }
        throw new IllegalArgumentException("Unknown round option");
    }

    private void dealCards() {
        int perPlayer = cardsForRound(currRound);
        for (int i = 0; i < perPlayer; i++) {
            for (JokerPlayer player : players) {
                if (!activeDeck.isEmpty()) {
                    player.addCard(activeDeck.remove(0));
                }
            }
        }
    }

    private void determineTrumpSuit() {
        if (!activeDeck.isEmpty()) {
            Card trumpCard = activeDeck.get(0);
            this.trumpSuit = trumpCard.getIsJoker() ? "NONE" : trumpCard.getSuit();
        } else {
            JokerPlayer dealerPlayer = players.get(dealer);
            List<Card> dealerCards = dealerPlayer.getCardList();
            if (!dealerCards.isEmpty()) {
                Card last = dealerCards.get(dealerCards.size() - 1);
                this.trumpSuit = last.getIsJoker() ? "NONE" : last.getSuit();
            } else {
                this.trumpSuit = "NONE";
            }
        }
    }

    public void recordRoundRes(JokerScoringService scoringService) {
        int totalTricks = cardsForRound(currRound);
        for (JokerPlayer player : players) {
            int score = scoringService.calculateRoundScore(
                    player,
                    totalTricks,
                    config.getRoundOption(),
                    currRound
            );
            boolean fulfilled = scoringService.fulfilledProphecy(player);
            roundScoresPerPlayer.get(player.getUserId()).add(score);
            prophecyFulfilledPerPlayer.get(player.getUserId()).add(fulfilled);
            player.addScores(score);
        }
    }

    public void setTrumpSuit(String suit) {
        this.trumpSuit = suit; // validation is in JokerService
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public JokerPlayer getCurrPlayer() {
        return players.get(currPlayer);
    }

    public void turn() {
        currPlayer = (currPlayer + 1) % players.size();
    }

    public JokerTrick getCurrentTrick() {
        if (currentTrick == null) {
            currentTrick = new JokerTrick(trumpSuit);
        }
        return currentTrick;
    }

    public void finishTrick(JokerPlayer winner) {
        currPlayer = players.indexOf(winner);
        currentTrick = new JokerTrick(trumpSuit);
    }

    public boolean allPlayersBidded() {
        return players.stream().allMatch(p -> p.getProphecy() >= 0);
    }

    public boolean isRoundOver() {
        return players.stream().allMatch(p -> p.getCardList().isEmpty());
    }

    public boolean isGameOver() {
        return currRound >= config.getTotalRounds();
    }

    public void endRound() {
        status = isGameOver() ? GameStatus.FINISHED : GameStatus.ROUND_END;
    }

    public void addPendingAchievement(Long userId, UnlockedAchievementDto achievement) {
        pendingAchievements.computeIfAbsent(userId, k -> new ArrayList<>()).add(achievement);
    }

    public Map<Long, List<UnlockedAchievementDto>> drainPendingAchievements() {
        Map<Long, List<UnlockedAchievementDto>> copy = new HashMap<>(pendingAchievements);
        pendingAchievements.clear();
        return copy;
    }
}