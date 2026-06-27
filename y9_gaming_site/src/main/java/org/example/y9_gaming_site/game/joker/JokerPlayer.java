package org.example.y9_gaming_site.game.joker;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class JokerPlayer {
    private final Long userId;
    //what cards does user hold in hands
    private final List<Card> cardList=new ArrayList<>();
    @Setter private int prophecy=0; //how many tricks user claims to take
    @Setter private int current=0; //how many tricks  user has for this time
    private  int totalScore=0;

    public JokerPlayer(Long userId){
        this.userId=userId;
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

    public void resetRoundInfo(){
        this.cardList.clear();
        this.prophecy=0;
        this.current=0;
    }

    public void tricksTaken(){
        this.current++;
    }

    public void addScores(int point){
        this.totalScore+=point;
    }
    public List<Card> getCardList() {
        return Collections.unmodifiableList(cardList);
    }

}
