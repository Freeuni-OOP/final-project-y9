package org.example.y9_gaming_site.gameRecord;


import org.example.y9_gaming_site.Challenge.GameChallenge;
import org.example.y9_gaming_site.Challenge.GameChallengeStatus;
import org.example.y9_gaming_site.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameRecordRepository extends JpaRepository<GameChallenge, Long> {
    List<GameChallenge> findByReceiverAndStatus(Long receiver, GameChallengeStatus status);

    List<GameChallenge> findBySenderAndStatus(Long sender, Long receiver);

    List<GameChallenge> findByStatusAndExpiresAtBefore(GameChallengeStatus status, LocalDateTime cutoff);
}
