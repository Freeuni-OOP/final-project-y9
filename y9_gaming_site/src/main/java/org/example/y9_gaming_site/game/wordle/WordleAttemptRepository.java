package org.example.y9_gaming_site.game.wordle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WordleAttemptRepository extends JpaRepository<WordleAttempt, Long> {
    Optional<WordleAttempt> findByUserIdAndPuzzleId(Long userId, Long puzzleId);
    long countByUser_IdAndStatusAndPuzzle_PuzzleDateIsNotNull(Long userId, AttemptStatus status);

    @Query("SELECT wa.puzzle.puzzleDate FROM WordleAttempt wa " +
            "WHERE wa.user.id = :userId AND wa.status = :status AND wa.puzzle.puzzleDate IS NOT NULL " +
            "ORDER BY wa.puzzle.puzzleDate DESC")
    List<LocalDate> findWonDailyPuzzleDatesDesc(@Param("userId") Long userId, @Param("status") AttemptStatus status);
}
