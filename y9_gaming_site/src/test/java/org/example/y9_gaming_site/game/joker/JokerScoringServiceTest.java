package org.example.y9_gaming_site.game.joker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class JokerScoringServiceTest {

    private JokerScoringService jkScore;
    private List<JokerPlayer> players;

    @BeforeEach
    void setUp(){
        jkScore = new JokerScoringService();
        players= new ArrayList<>();
        players.add(new JokerPlayer(1L, "dolche"));
        players.add(new JokerPlayer(2L, "cico"));
    }

    @Test
    @DisplayName("Should award (prophecy * 50) + 50 points when player hits their prophecy exactly")
    void testcorrectProScore(){
        JokerPlayer player=players.get(0);
        player.setProphecy(2);
        player.setCurrent(2);
        int score = jkScore.calculateRoundScore(player, 4, null, 1);
        assertEquals(150, score, "Should score exactly 150 points for matching a bid of 2");

    }

    @Test
    @DisplayName("Should award prophecy * 100 if player takes all tricks in the round")
    void testTakeAllTricksBonus() {
        JokerPlayer player = players.get(0);
        player.setProphecy(4);
        player.setCurrent(4); // Took every single trick!

        int score = jkScore.calculateRoundScore(player, 4, null, 1);

        // Math: 4 * 100 = 400
        assertEquals(400, score, "Should score 400 points for taking all 4 tricks in the round");
    }

    @Test
    @DisplayName("Should penalize -500 points for gavxishtet (prophecy > 0, actual == 0) during Full 24 penalty rounds")
    void testGavxishtetFull24PenaltyRound() {
        JokerPlayer player = players.get(0);
        player.setProphecy(3);
        player.setCurrent(0); // Bid 3 but took 0! (Gavxishtet)

        // Test inside the Set 2 penalty round bracket (e.g., round 9) with FULL_24 enabled
        int score = jkScore.calculateRoundScore(player, 8, JokerGameConfig.RoundOption.FULL_24, 9);

        assertEquals(-500, score, "Should penalize with -500 points during round 9 of Full 24 variant");
    }

    @Test
    @DisplayName("Should penalize -200 points for gavxishtet outside of Full 24 special penalty brackets")
    void testGavxishtetStandardPenalty() {
        JokerPlayer player = players.get(0);
        player.setProphecy(3);
        player.setCurrent(0); // Bid 3 but took 0!

        int score = jkScore.calculateRoundScore(player, 4, null, 1);

        assertEquals(-200, score, "Should default to a -200 penalty outside specific round windows");
    }

    @Test
    @DisplayName("Should award 50 points if player bids 0 and successfully takes 0 tricks")
    void testPasiScore() {
        JokerPlayer player = players.get(0);
        player.setProphecy(0);
        player.setCurrent(0);

        int score = jkScore.calculateRoundScore(player, 4, null, 1);

        assertEquals(50, score, "Successful pass (0/0) must reward exactly 50 points");
    }

    @Test
    @DisplayName("Should test applyRoundScores loop updates players total scores directly")
    void testApplyRoundScoresUpdatesTotalScore() {
        players.get(0).setProphecy(1);
        players.get(0).setCurrent(1); // (1 * 50) + 50 = 100 points

        players.get(1).setProphecy(0);
        players.get(1).setCurrent(0); // 50 points

        // Test the list runner method
        jkScore.applyRoundScores(players, 4, null, 1);

        assertEquals(100, players.get(0).getTotalScore());
        assertEquals(50, players.get(1).getTotalScore());
    }

    @Test
    @DisplayName("Should correctly return boolean flag for fulfilledProphecy check validation")
    void testFulfilledProphecy() {
        JokerPlayer player = players.get(0);
        player.setProphecy(2);
        player.setCurrent(2);
        assertTrue(jkScore.fulfilledProphecy(player));

        player.setCurrent(1);
        assertFalse(jkScore.fulfilledProphecy(player));
    }
}
