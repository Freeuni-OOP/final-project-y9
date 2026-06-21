package org.example.y9_gaming_site.leaderboard;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaderboardService {
    private final LeaderboardScoreRepository scoreRep;

    public LeaderboardService (LeaderboardScoreRepository scoreRep) {
        this.scoreRep=scoreRep;
    }

    //save new score for each new game played
    public void saveScore(Long userId, String gameName, Integer score){
        LeaderboardScore current = new LeaderboardScore();
        current.setId(userId);
        current.setGameName(gameName);
        current.setScore(score);
        current.setPlayedAt(LocalDateTime.now());
        scoreRep.save(current);
    }

    //returns list of users that are in top 10 in specific game
    public List<LeaderboardScore> getTopScored(String gameName){
        return scoreRep.findTop10ByGameNameOrderByScoreDesc(gameName);
    }

    //returns list of users who were in top 10 in last 24 hour
    public List<LeaderboardScore> getTopScoresLast24Hours(String gameName){
        LocalDateTime begin=LocalDateTime.now().minusHours(24);
        return scoreRep.findTop10ByGameNameAndPlayedAtAfterOrderByScoreDesc(gameName, begin);
    }

    //return all history of user in specific game
    public List<LeaderboardScore> getUserHistory(Long userId, String gameName){
        return scoreRep.findByUserIdAndGameName(userId, gameName);
    }

}
