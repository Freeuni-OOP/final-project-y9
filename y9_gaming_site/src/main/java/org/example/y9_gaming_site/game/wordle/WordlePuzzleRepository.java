package org.example.y9_gaming_site.game.wordle;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WordlePuzzleRepository extends JpaRepository<WordlePuzzle, Long> {
    Optional<WordlePuzzle> findByPuzzleDate(LocalDate puzzleDate);
    @Query("SELECT p.answerWord FROM WordlePuzzle p WHERE p.puzzleDate IS NOT NULL")
    List<String> findAnswerWordByPuzzleDateIsNotNull();

}