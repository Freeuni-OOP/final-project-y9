package org.example.y9_gaming_site.game.joker;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class JokerGameState {
    public enum GameStatus { WAITING, BIDDING, PLAYING, ROUND_END, FINISHED }
    private  final JokerGameConfig config;
    private final List<JokerPlayer> players=new ArrayList<>();
    private final JokerRoom room;

    private GameStatus status = GameStatus.WAITING;
    private int currRound=0;
    private int currPlayer=0; //whose turn it is
    private int dealer=0; //who deals tha card, rotates each round
    private String trumpSuit =null; //koziri

    public JokerGameState(JokerGameConfig config, JokerRoom room){
        this.config=config;
        this.room=room;

    }

    public void addPlayer(JokerPlayer player){
        if(players.size()>= config.getPlayers()){
            throw new IllegalStateException("Game is already full");
        }
        players.add(player);
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
        dealCards();
        determineTrumpSuit();

        status=GameStatus.BIDDING;

    }

    //rounds go up then down (1,2,3...max...3,2,1)
    private int cardsForRound(int curr, int total){
        int half = total / 2;
        return (curr<= half) ? curr : total - curr + 1;
    }

    private  void dealCards(){
        int perPlayerAmount=cardsForRound(currRound,config.getTotalRounds());
        List<Card>deck= new ArrayList<>(room.getDeck());
        for(int i=0; i<perPlayerAmount; i++){
            for(JokerPlayer player :players){
                if(!deck.isEmpty()){
                    player.addCard(deck.remove(0));
                }
            }
        }

    }

    private void determineTrumpSuit() {
        List<Card>deck= new ArrayList<>(room.getDeck());
        int dealCardsAmount=cardsForRound(currRound,config.getTotalRounds()) * players.size();
        if(dealCardsAmount<deck.size()){
            Card trumpCard=deck.get(dealCardsAmount);
            if(trumpCard.getIsJoker()){
                trumpSuit =null;
            }else{
                trumpSuit =trumpCard.getSuit();
            }
        }else{
            trumpSuit =null;
        }
    }

    //TODO:kozis asaxelbdes pirveli sami kartidan da tu k=jokeri amovida iyos bezkoziri
    // dealer sets trumpsuit
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

