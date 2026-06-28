package org.example.y9_gaming_site.game.joker;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Card {
    private final String suit; //heart, diamond ...
    private final Integer value; //6-14 jack->11 ... joker1->15, joker2->16
    private final Boolean isJoker;
    private final String jokerAction; //take or give (only joker can use this)

    //constructor for standard cards
    public Card(String suit, Integer value){
        this.suit=suit;
        this.value=value;
        this.isJoker=false;
        this.jokerAction=null;
    }

    //constructor for joker
    public Card(Integer value, String jokerAction){
        this.suit="NONE";
        this.value=value;
        this.isJoker=true;
        //by default we take
        if(jokerAction!=null){
            this.jokerAction=jokerAction;
        }else{
            this.jokerAction="TAKE";
        }

    }

    @Override
    public String toString(){
        if(isJoker){
            String res="";
            if(value==15){
                res="JOKER_1";
            }else{
                res= "JOKER_2";
            }
            return res +"(" +jokerAction +")";
        }
        return valueToName()+ " of "+suit;
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
