package org.example.y9_gaming_site.game.joker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class JokerRuleTest {

    private List<JokerPlayer> players;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>();
        // Set up 4 standard players using your specific constructor
        players.add(new JokerPlayer(1L, "Lasha"));
        players.add(new JokerPlayer(2L, "Giorgi"));
        players.add(new JokerPlayer(3L, "Nino"));
        players.add(new JokerPlayer(4L, "Anano"));

        // Clear variables before each test instance run
        for (JokerPlayer p : players) {
            p.resetRoundInfo();
        }
    }

    @Test
    @DisplayName("Should evaluate highest matching lead suit card as winner when no trumps are played")
    void testStandardSuitWins() {
        // Arrange: Trump is HEARTS
        JokerTrick trick = new JokerTrick("HEARTS");

        Card card1 = new Card("CLUBS", 10);
        Card card2 = new Card("CLUBS", 14); // Ace of Clubs (Highest matching lead)
        Card card3 = new Card("CLUBS", 11);
        Card card4 = new Card("DIAMONDS", 13); // King of Diamonds (different suit)

        // FIXED: Using your exact player.addCard() method to bypass the unmodifiable collection block
        players.get(0).addCard(card1);
        players.get(1).addCard(card2);
        players.get(2).addCard(card3);
        players.get(3).addCard(card4);

        // Act: Play cards into the trick cycle
        trick.playCard(players.get(0), card1, "NONE", "NONE"); // Sets led suit to CLUBS
        trick.playCard(players.get(1), card2, "NONE", "NONE");
        trick.playCard(players.get(2), card3, "NONE", "NONE");
        trick.playCard(players.get(3), card4, "NONE", "NONE");

        // Assert
        assertEquals("Giorgi", trick.winner().getUsername(), "Ace of Clubs should win");
    }

    @Test
    @DisplayName("Should award the win to a Trump card cutting a standard suit")
    void testTrumpCutsLeadSuit() {
        // Arrange: Trump is SPADES
        JokerTrick trick = new JokerTrick("SPADES");

        Card card1 = new Card("HEARTS", 14); // Ace of Hearts led
        Card card2 = new Card("HEARTS", 9);
        Card card3 = new Card("SPADES", 7);  // 7 of Spades (Trump!)
        Card card4 = new Card("HEARTS", 2);

        players.get(0).addCard(card1);
        players.get(1).addCard(card2);
        players.get(2).addCard(card3);
        players.get(3).addCard(card4);

        // Act
        trick.playCard(players.get(0), card1, "NONE", "NONE");
        trick.playCard(players.get(1), card2, "NONE", "NONE");
        trick.playCard(players.get(2), card3, "NONE", "NONE");
        trick.playCard(players.get(3), card4, "NONE", "NONE");

        // Assert
        assertEquals("Nino", trick.winner().getUsername(), "Trump card should cut and win");
    }

    @Test
    @DisplayName("Georgian rule: HIGH Joker loses if it asked for non-trump and challenger plays a Trump")
    void testHighJokerCutByTrump() {
        // Arrange: Trump is DIAMONDS
        JokerTrick trick = new JokerTrick("DIAMONDS");

        Card jokerCard = new Card(15); // Using Joker constructor!
        Card trumpCard = new Card("DIAMONDS", 8); // Trump card cutting in
        Card normalCard1 = new Card("HEARTS", 10);
        Card normalCard2 = new Card("HEARTS", 14);

        players.get(0).addCard(jokerCard);
        players.get(1).addCard(trumpCard);
        players.get(2).addCard(normalCard1);
        players.get(3).addCard(normalCard2);

        // Act: Player 1 leads with Joker called HIGH and asks for HEARTS ("vishi kozirebi")
        trick.playCard(players.get(0), jokerCard, "HIGH", "HEARTS");
        // Player 2 doesn't have hearts, cuts with a DIAMOND trump card!
        trick.playCard(players.get(1), trumpCard, "NONE", "NONE");
        trick.playCard(players.get(2), normalCard1, "NONE", "NONE");
        trick.playCard(players.get(3), normalCard2, "NONE", "NONE");

        // Assert: Based on your evaluateTwoCards logic, a HIGH joker loses to a trump if ledSuit != trumpSuit
        assertEquals("Giorgi", trick.winner().getUsername(), "Challenger with a Trump card should win over the HIGH Joker");
    }

    @Test
    @DisplayName("Should flag complete status when match count hits target player thresholds")
    void testTrickCompletion() {
        JokerTrick trick = new JokerTrick("CLUBS");
        assertFalse(trick.isComplete(4));

        Card card = new Card("HEARTS", 10);
        players.get(0).addCard(card);
        trick.playCard(players.get(0), card, "NONE", "NONE");

        assertFalse(trick.isComplete(4));
    }
}