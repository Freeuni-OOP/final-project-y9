package org.example.y9_gaming_site.game.joker;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
@Getter
public class JokerPlayer {
    private final Long userId;
    private final String username;
    //what cards does user hold in hands
    private final List<Card> cardList=new ArrayList<>();
    @Setter private int prophecy=-1; //how many tricks user claims to take
    @Setter private int current=0; //how many tricks  user has for this time
    private  int totalScore=0;

    public JokerPlayer(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    //calling this while dealing cards
    public void addCard(Card newCard){
        this.cardList.add(newCard);
    }

    public void removeCard(Card card){
        this.cardList.removeIf(c->c.getSuit().equals(card.getSuit())&&
                c.getValue().equals(card.getValue()) &&
                c.getIsJoker().equals(card.getIsJoker()));
    }

    public boolean hasSuit(String suit) {
        return cardList.stream().anyMatch(c -> c.getSuit().equals(suit) && !c.getIsJoker());
    }

    public boolean hasTrumps(String trumpSuit) {
        return cardList.stream().anyMatch(c -> c.getSuit().equals(trumpSuit) && !c.getIsJoker());
    }
    public void resetRoundInfo(){
        this.cardList.clear();
        this.prophecy=-1;
        this.current=0;
    }

    public void tricksTaken(){
        this.current++;
    }

    public void addScores(int point){
        this.totalScore+=point;
    }

    @JsonProperty("cardList")
    public List<Card> getCardList() {
        return Collections.unmodifiableList(cardList);
    }

}
