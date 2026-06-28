package org.example.y9_gaming_site.game.joker;

import lombok.Getter;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public class JokerGameState {
    public enum GameStatus { WAITING, BIDDING, PLAYING, ROUND_END, FINISHED }
    private  final JokerGameConfig config;
    private final List<JokerPlayer> players=new ArrayList<>();
    private final JokerRoom room;
    private final Map<Long, List<Integer>> roundScoresPerPlayer = new HashMap<>();
    private final Map<Long, List<Boolean>> prophecyFulfilledPerPlayer = new HashMap<>();

    private GameStatus status = GameStatus.WAITING;
    private int currRound=0;
    private int currPlayer=0; //whose turn it is
    private int dealer=0; //who deals tha card, rotates each round
    private String trumpSuit =null; //koziri
    private final List<Card> activeDeck = new ArrayList<>(); // Track the actual live deck state

    public JokerGameState(JokerGameConfig config, JokerRoom room){
        this.config=config;
        this.room=room;

        this.room.setPlayerCount(config.getPlayers());
        this.room.setJokerAmount(config.getJokerAmount());
    }

    public void addPlayer(JokerPlayer player){
        if(players.size()>= config.getPlayers()){
            throw new IllegalStateException("Game is already full");
        }
        players.add(player);
        roundScoresPerPlayer.put(player.getUserId(), new ArrayList<>());
        prophecyFulfilledPerPlayer.put(player.getUserId(), new ArrayList<>());
    }

    public boolean isFull(){
        return players.size()== config.getPlayers();
    }

    public void startNextRound(){
        if (!isFull()) throw new IllegalStateException("Not enough players to start");
        if(currRound>= config.getTotalRounds()) throw new IllegalStateException("Game already finished");

        currRound++;
        // rotates each round
        dealer=(currRound-1)%players.size();
        // player after dealer goes first
        currPlayer=(dealer+1)%players.size();
        for(JokerPlayer p:players){
            p.resetRoundInfo();
        }
        room.shuffle();
        activeDeck.clear();
        activeDeck.addAll(room.getDeck());
        dealCards();
        determineTrumpSuit();
        status=GameStatus.BIDDING;

    }


    //rounds go up then down (1,2,3...max...3,2,1)
    public int cardsForRound(int round) {
        if (config.getRoundOption() == JokerGameConfig.RoundOption.QUICK_4) {
            return 9;
        }
        if (config.getRoundOption() == JokerGameConfig.RoundOption.SHORT_8) {
            return round;
        }
        if (config.getRoundOption() == JokerGameConfig.RoundOption.FULL_24) {

            if (round <= 8) {
                return round;
            }
            if (round <= 12) {
                return 9;
            }
            if (round <= 20) {
                return 21 - round;
            }
            return 9;
        }
        throw new IllegalArgumentException("Unknown round option");
    }



    private  void dealCards(){
        int perPlayerAmount = cardsForRound(currRound);
        for(int i = 0; i < perPlayerAmount; i++){
            for(JokerPlayer player : players){
                if(!activeDeck.isEmpty()){
                    player.addCard(activeDeck.remove(0));
                }
            }
        }

    }

    private void determineTrumpSuit() {
        if (!activeDeck.isEmpty()) {
            // Cards are remaining; flip the top card to dictate trump suit
            Card trumpCard = activeDeck.get(0);
            if (trumpCard.getIsJoker()) {
                this.trumpSuit = "NONE"; // Joker turned up means No-Trump hand
            } else {
                this.trumpSuit = trumpCard.getSuit();
            }
        }else{
            //dealer's  last card will be trump
            JokerPlayer dealerPlayer = players.get(dealer);
            List<Card> dealerCards = dealerPlayer.getCardList();
            if (!dealerCards.isEmpty()) {
                Card dealersLastCard = dealerCards.get(dealerCards.size() - 1);
                if (dealersLastCard.getIsJoker()) {
                    this.trumpSuit = "NONE";
                } else {
                    this.trumpSuit = dealersLastCard.getSuit();
                }
            } else {
                this.trumpSuit = "NONE";
            }
        }
    }

    public void recordRoundRes(JokerScoringService scoringService){
        int totalTricks = cardsForRound(currRound);
        for (JokerPlayer player : players) {
            int score = scoringService.calculateRoundScore(player, totalTricks, config.getRoundOption(), currRound);
            boolean fulfilled = scoringService.fulfilledProphecy(player);

            roundScoresPerPlayer.get(player.getUserId()).add(score);
            prophecyFulfilledPerPlayer.get(player.getUserId()).add(fulfilled);
            player.addScores(score);
        }
    }
    public void setTrumpSuit(String suit){
        if (trumpSuit != null) throw new IllegalStateException("Trump suit already set");
        this.trumpSuit = suit;

    }

    public  JokerPlayer getCurrPlayer(){
        return players.get(currPlayer);
    }

    public void turn(){
        currPlayer=(currPlayer+1)%players.size();
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


}

