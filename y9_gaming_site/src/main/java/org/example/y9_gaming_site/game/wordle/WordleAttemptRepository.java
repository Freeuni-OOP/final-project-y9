package org.example.y9_gaming_site.game.wordle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WordleAttemptRepository extends JpaRepository<WordleAttempt, Long> {
    Optional<WordleAttempt> findByUserIdAndPuzzleId(Long userId, Long puzzleId);
}
