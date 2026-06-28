package org.example.y9_gaming_site.Challenge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameChallengeRepository extends JpaRepository<GameChallenge, Long> {
    List<GameChallenge> findByReceiverIdAndStatus(Long receiverId, GameChallengeStatus status);

    List<GameChallenge> findBySenderIdOrReceiverId(Long senderId, Long receiverId);

    List<GameChallenge> findByStatusAndExpiresAtBefore(GameChallengeStatus status, LocalDateTime cutoff);
}
