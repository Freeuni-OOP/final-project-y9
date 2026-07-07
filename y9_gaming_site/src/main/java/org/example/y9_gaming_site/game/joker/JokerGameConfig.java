package org.example.y9_gaming_site.game.joker;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JokerGameConfig {
    public enum PlayerCount { THREE, FOUR }
    public enum RoundOption { FULL_24, SHORT_8, QUICK_4 }

    private final PlayerCount playerCount;
    private final boolean allowRandoms; //if we want to play with random people
    private final RoundOption roundOption;
    private final int jokerAmount;

    public JokerGameConfig(PlayerCount playerCount, boolean allowRandoms, RoundOption roundOption, int jokerAmount){
        this.playerCount=playerCount;
        this.allowRandoms=allowRandoms;
        this.roundOption=roundOption;
        if (jokerAmount < 1 || jokerAmount > 2) {
            throw new IllegalArgumentException("Joker amount must be 1 or 2");
        }
        this.jokerAmount = jokerAmount;
    }

    public int getPlayers(){
        if (playerCount == PlayerCount.THREE) {
            return 3;
        } else if (playerCount == PlayerCount.FOUR) {
            return 4;
        } else {
            throw new IllegalArgumentException("Unknown player count");
        }
    }

    public int getTotalRounds(){
        if (roundOption == RoundOption.FULL_24) {
            return 24;
        } else if (roundOption == RoundOption.SHORT_8) {
            return 8;
        }else if (roundOption == RoundOption.QUICK_4) {
            return 4;
        }
        else {
            throw new IllegalArgumentException("Unknown round option");
        }
    }


}
