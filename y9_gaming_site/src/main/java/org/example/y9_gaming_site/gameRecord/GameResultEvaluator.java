package org.example.y9_gaming_site.gameRecord;

public interface GameResultEvaluator {

    String getGameKey();

    boolean isBetter(double candidateValue, double currentBestValue);
}
