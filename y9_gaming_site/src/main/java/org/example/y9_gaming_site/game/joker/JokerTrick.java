package org.example.y9_gaming_site.game.joker;

import lombok.Getter;

import java.util.*;

@Getter
public class JokerTrick {
    public  record PlayedCard(
            JokerPlayer player,
            Card card,
            String jokerCall,     // "HIGH", "LOW", or "NONE"(wants to take or give )
            String declaredSuit
    ){}
    private final List<PlayedCard> playedCards = new ArrayList<>();
    private final String trumpSuit;
    private  String ledSuit;
    private String ledJokerCall = "NONE"; // Tracks if a Joker started the trick

    public  JokerTrick(String trumpSuit){
        if(trumpSuit==null){
            this.trumpSuit="NONE";
        }else{
            this.trumpSuit=trumpSuit;
        }
    }


    public boolean isValCard(JokerPlayer player, Card card, String jokerCall, String declaredSuit){
        if (playedCards.isEmpty()) { //first card is always valid
            return true;
        }

        if(card.getIsJoker()) return true; //joker always valid

        if(ledJokerCall.equals("HIGH")){
            // A Joker was led HIGH. Players must follow the declared suit if they have it.
            boolean hasDeclaredSuit = player.hasSuit(ledSuit);
            if(hasDeclaredSuit){
                if(!card.getSuit().equals(ledSuit)) return false;

                // ვპოულობთ ხელში არსებულ მაქსიმალურ კარტს ამ სუიტში
                int maxValInHand = player.getCardList().stream()
                        .filter(c -> !c.getIsJoker() && c.getSuit().equals(ledSuit))
                        .mapToInt(Card::getValue)
                        .max()
                        .orElse(0);

                return card.getValue().intValue() == maxValInHand;
            }

            //If they don't have the declared suit they must play a trump if they have one.
            if(!ledSuit.equals(trumpSuit) && !trumpSuit.equals("NONE")){
                boolean hasTrump = player.hasTrumps(trumpSuit);
                if(hasTrump){
                    return card.getSuit().equals(trumpSuit);
                }
            }
            return true;
        } else { // A normal card or a LOW Joker was led. Standard trick rules apply.
            boolean haveLedSuit = player.hasSuit(ledSuit);
            if (haveLedSuit) {
                return card.getSuit().equals(ledSuit);
            }
            // If they can't follow suit, they MUST play a trump if they have one
            if(!trumpSuit.equals("NONE")){
                boolean hasTrump = player.hasTrumps(trumpSuit);
                if(hasTrump){
                    return card.getSuit().equals(trumpSuit);
                }
            }
            return true;
        }
    }

    public void playCard(JokerPlayer player, Card card, String jokerCall, String declaredSuit){

        if (!isValCard(player, card, jokerCall, declaredSuit)) {
            throw new IllegalArgumentException(player.getUsername() + " played an illegal card: " + card);
        }


        if(playedCards.isEmpty()){
            if(card.getIsJoker()){
                this.ledJokerCall = jokerCall;
                this.ledSuit = declaredSuit;
            } else {
                this.ledJokerCall = "NONE";
                this.ledSuit = card.getSuit();
            }
        }


        playedCards.add(new PlayedCard(player, card, jokerCall, card.getIsJoker() ? ledSuit : "NONE"));


        player.removeCard(card);
    }
    public JokerPlayer winner() {
        if (playedCards.isEmpty()) return null;
        PlayedCard winnerCard=playedCards.get(0);
        for (int i = 1; i < playedCards.size(); i++) {
            PlayedCard currenCard = playedCards.get(i);
            winnerCard = evaluateTwoCards(winnerCard, currenCard);
        }
        return winnerCard.player();
    }

    private PlayedCard evaluateTwoCards(PlayedCard winnerCard, PlayedCard currCard) {
        // 1. თუ ორივე ჯოკერია, ბოლო ჩამოგდებული HIGH ჯოკერი ყოველთვის იგებს.
        if (winnerCard.card().getIsJoker() && currCard.card().getIsJoker()) {
            if (currCard.jokerCall().equals("HIGH")) return currCard;
            return winnerCard;
        }

        // 2. თუ მაგიდაზე უკვე დევს მოგებული HIGH ჯოკერი, მას ჩვეულებრივი კოზირი ვერ მოუგებს.
        if (winnerCard.card().getIsJoker() && winnerCard.jokerCall().equals("HIGH")) {
            return winnerCard;
        }

        // 3. თუ ახალი ჩამოსული ბარათია HIGH ჯოკერი, ის ავტომატურად უგებს ნებისმიერ ჩვეულებრივ კარტს (მათ შორის კოზირსაც).
        if (currCard.card().getIsJoker() && currCard.jokerCall().equals("HIGH")) {
            return currCard;
        }

        // 4. LOW ჯოკერის წესები
        if (currCard.card().getIsJoker() && currCard.jokerCall().equals("LOW")) {
            return winnerCard;
        }
        if (winnerCard.card().getIsJoker() && winnerCard.jokerCall().equals("LOW")) {
            if (currCard.card().getSuit().equals(ledSuit) || currCard.card().getSuit().equals(trumpSuit)) {
                return currCard;
            }
            return winnerCard;
        }

        // 5. ორი ჩვეულებრივი კარტის შედარება
        int bestVal = cardVal(winnerCard.card());
        int challengerVal = cardVal(currCard.card());

        return (challengerVal > bestVal) ? currCard : winnerCard;
    }


    private int cardVal(Card card){
        if (card.getSuit().equals(trumpSuit)) {
            return 1000 + card.getValue();
        }
        if (card.getSuit().equals(ledSuit)) {
            return 500 + card.getValue();
        }
        return card.getValue();
    }

    public boolean isComplete(int playerCount) {
        return playedCards.size() == playerCount;
    }

}
