package org.example.y9_gaming_site.game.joker;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Card {
    private final String suit; //heart, diamond ...
    private final Integer value; //6-14 jack->11 ... joker1->15, joker2->16
    private final Boolean isJoker;

    //constructor for standard cards
    public Card(String suit, Integer value){
        this.suit=suit;
        this.value=value;
        this.isJoker=false;

    }

    //constructor for joker
    public Card(Integer value){
        this.suit="NONE";
        this.value=value;
        this.isJoker=true;

    }

    @Override
    public String toString() {
        if (isJoker) {
            return "JOKER (" + value + ")";
        }
        return suit + "_" + valueToName();
    }

    private String valueToName() {
        return switch (value) {
            case 11 -> "Jack";
            case 12 -> "Queen";
            case 13 -> "King";
            case 14 -> "Ace";
            default -> String.valueOf(value);
        };
    }
}
