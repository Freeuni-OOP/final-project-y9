package org.example.y9_gaming_site.gameRecord;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {
    List<GameRecord> findByUserIdAndGameId(Long userId, Long gameId);

    List<GameRecord> findByUserIdAndGameIdAndContextId(Long userId, Long gameId, Long contextId);

    List<GameRecord> findByGameIdAndContextId(Long gameId, Long contextId);

    List<GameRecord> findByGameId(Long gameId);

    long countByUserIdAndGameId(Long userId, Long gameId);

    List<GameRecord> findByUserIdAndGameIdOrderByRecordedAtDesc(Long userId, Long gameId);
}
