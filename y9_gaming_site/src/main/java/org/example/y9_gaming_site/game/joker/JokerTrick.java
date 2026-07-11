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


        playedCards.add(new PlayedCard(player, card, jokerCall, declaredSuit));


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
        // 1. თუ მიმდინარე ლიდერი (winnerCard) არის HIGH ჯოკერი
        if (winnerCard.card().getIsJoker() && "HIGH".equalsIgnoreCase(winnerCard.jokerCall())) {
            // მას მხოლოდ მეორე HIGH ჯოკერი თუ მოუგებს
            if (currCard.card().getIsJoker() && "HIGH".equalsIgnoreCase(currCard.jokerCall())) {
                return currCard;
            }
            return winnerCard;
        }

        // 2. თუ ახალი შემოსული კარტი (currCard) არის HIGH ჯოკერი
        // რადგან პირველი პირობა გამოვარდა, ეს ნიშნავს რომ winnerCard არ ყოფილა HIGH ჯოკერი, ასე რომ ახალი HIGH ავტომატურად იგებს!
        if (currCard.card().getIsJoker() && "HIGH".equalsIgnoreCase(currCard.jokerCall())) {
            return currCard;
        }

        // 3. თუ ორივე LOW ჯოკერია
        if (winnerCard.card().getIsJoker() && "LOW".equalsIgnoreCase(winnerCard.jokerCall()) &&
                currCard.card().getIsJoker() && "LOW".equalsIgnoreCase(currCard.jokerCall())) {
            // წესით, პირველი LOW ჯოკერი "უფრო დაბალია" (უფრო მეტად ეტენება), ასე რომ ძველი რჩება სატანებლად
            return winnerCard;
        }

        // 4. თუ მაგიდაზე დევს LOW ჯოკერი (winnerCard) და ახალი კარტი ჩვეულებრივია
        if (winnerCard.card().getIsJoker() && "LOW".equalsIgnoreCase(winnerCard.jokerCall())) {
            // ჩვეულებრივი კარტი მოუგებს LOW ჯოკერს მხოლოდ მაშინ, თუ ის არის მოთხოვილი სუიტის (ledSuit) ან კოზირი (trumpSuit)
            if (currCard.card().getSuit().equals(ledSuit) || currCard.card().getSuit().equals(trumpSuit)) {
                return currCard;
            }
            // თუ არცერთი არ არის (მაგალითად გული მოითხოვა ჯოკერმა, კაცს არ ჰყავდა და აგური დადო), აგური ვერ მოუგებს და ისევ LOW ჯოკერს ეტენება!
            return winnerCard;
        }

        // 5. თუ მაგიდაზე დევს ჩვეულებრივი კარტი და ახალი ჩამოსული არის LOW ჯოკერი
        if (currCard.card().getIsJoker() && "LOW".equalsIgnoreCase(currCard.jokerCall())) {
            // შემოჭრილი LOW ჯოკერი ვერასდროს ვერ უგებს უკვე მაგიდაზე დადებულ ჩვეულებრივ კარტს
            return winnerCard;
        }

        // 6. თუ ორივე ჩვეულებრივი კარტია, ჩვეულებრივი მათემატიკური შედარება
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
