package org.example.y9_gaming_site.leaderboard;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface LeaderboardScoreRepository extends JpaRepository<LeaderboardScore, Long> {

    // Top 10 scores for each game
    List<LeaderboardScore> findTop10ByGameNameOrderByScoreDesc(String gameType);

    // history of specific user on specific game
    List<LeaderboardScore> findByUserIdAndGameName(Long userId, String gameType);

    // top players in last 24 hour
    List<LeaderboardScore> findTop10ByGameNameAndPlayedAtAfterOrderByScoreDesc(
            String gameType, LocalDateTime since
    );
}