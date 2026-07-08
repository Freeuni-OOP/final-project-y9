package org.example.y9_gaming_site.game.joker;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class JokerRoom {
    @Getter  private final String roomId;
    private final List<Card> deck=new ArrayList<>();
    @Getter  private int jokerAmount = 2;
    @Getter  private int playerCount = 4;

    public JokerRoom(String roomId){
        this.roomId=roomId;
    }

    //user can choose how many jokers they want to have
    public void setJokerAmount(int amount){
        if (amount < 1 || amount > 2) {
            throw new IllegalArgumentException("Joker amount must be 1 or 2");
        }
        this.jokerAmount=amount;
    }

    public void setPlayerCount(int count) {
        if (count != 3 && count != 4) {
            throw new IllegalArgumentException("Only 3 or 4 players supported");
        }
        this.playerCount = count;
    }

    public void generateDeck(){
        deck.clear();
        String[] suits={"HEARTS", "DIAMONDS", "CLUBS", "SPADES"};
        //if we have 4 player then we must start from 6 and if not from 8
        int startValue = (playerCount == 4) ? 6 : 8;
        for (String s:suits){
            for(int val = startValue; val<=14; val++){
                deck.add(new Card(s,val));
            }
        }
        //normalization
       if(playerCount==4){
           if(jokerAmount==2){//we must leave only red 6s
               deck.removeIf(c->c.getValue()==6 && (c.getSuit().equals("CLUBS") || c.getSuit().equals("SPADES")));
               deck.add(new Card(15));
               deck.add(new Card(16));
           }else{
               deck.removeIf(c -> c.getValue() == 6 && c.getSuit().equals("SPADES"));
               deck.add(new Card(15));
           }
       }else if(playerCount==3){
           if (jokerAmount == 2) {
               //After all the cards are dealt, each player should have 9 cards.
               deck.removeIf(c -> c.getValue() == 8 && (c.getSuit().equals("SPADES") || c.getSuit().equals("CLUBS")));
               deck.add(new Card(15));
               deck.add(new Card(16));
           }else{
               deck.removeIf(c -> c.getValue() == 8 && c.getSuit().equals("SPADES"));
               deck.add(new Card(15));
           }
       }
    }

    public void shuffle(){
        generateDeck();
        Collections.shuffle(deck);
    }
    @JsonIgnore
    public List<Card> getDeck(){
        return Collections.unmodifiableList(deck);
    }


}
