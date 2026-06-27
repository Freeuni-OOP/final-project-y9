package org.example.y9_gaming_site.game.joker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JokerRoom {
    private final String roomId;
    private final List<Card> deck=new ArrayList<>();
    private int jokerAmount=1;

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

    public void generateDeck(){
        deck.clear();
        String[] suits={"HEARTS", "DIAMONDS", "CLUBS", "SPADES"};
        for (String s:suits){
            for(int val=6; val<=14; val++){
                deck.add(new Card(s,val));
            }
        }
        if(jokerAmount>=1){
            deck.add(new Card(15,"TAKE"));
        }
        if(jokerAmount==2){
            deck.add(new Card(16,"TAKE"));
        }
    }

    public void shuffle(){
        generateDeck();
        Collections.shuffle(deck);
    }
    public List<Card> getDeck(){
        return Collections.unmodifiableList(deck);
    }

    public int getJokerAmount(){
        return jokerAmount;
    }

    public String gerRoomId(){
        return roomId;
    }
}
