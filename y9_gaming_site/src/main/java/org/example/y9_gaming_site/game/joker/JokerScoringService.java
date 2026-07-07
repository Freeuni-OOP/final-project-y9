package org.example.y9_gaming_site.game.joker;


import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class JokerScoringService {
    public int calculateRoundScore(JokerPlayer player, int totalTricksInRound, JokerGameConfig.RoundOption option, int currentRound){
        int prophecy = player.getProphecy();
        int actual = player.getCurrent();
        //gavxishtet
        if (prophecy > 0 && actual == 0) {
            if (option == JokerGameConfig.RoundOption.FULL_24) {
                // In Full 24: Sets 2 (rounds 9-12) and Set 4 (rounds 21-24) penalize -500 points
                if ((currentRound >= 9 && currentRound <= 12) || (currentRound >= 21 && currentRound <= 24)) {
                    return -500;
                }
            }
            return -200;
        }
        if(prophecy==0 && actual==0) return 50;
        if(prophecy==0 && actual>0)return 10 * actual;
        if (prophecy == totalTricksInRound && actual == totalTricksInRound) {
            return prophecy * 100;
        }
        if(prophecy==actual)return (prophecy*50)+50;
        return actual*10;
    }

    public boolean fulfilledProphecy(JokerPlayer player) {
        return player.getProphecy() == player.getCurrent();
    }
    public void applyRoundScores(List<JokerPlayer> players, int totalTricksInRound, JokerGameConfig.RoundOption option, int currentRound) {
        for (JokerPlayer player : players) {
            int score = calculateRoundScore(player, totalTricksInRound, option, currentRound);
            player.addScores(score);
        }
    }

}
