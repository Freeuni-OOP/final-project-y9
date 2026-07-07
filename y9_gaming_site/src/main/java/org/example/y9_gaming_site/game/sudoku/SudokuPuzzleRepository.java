package org.example.y9_gaming_site.game;

import org.example.y9_gaming_site.game.sudoku.SudokuPuzzle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionUsageException;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SudokuPuzzleRepository extends JpaRepository<SudokuPuzzle, Long> {


    Optional<SudokuPuzzle> findByPuzzleDate(LocalDate puzzleDate);

    Optional<SudokuPuzzle> findById(Long ID);

    @Query(value = "SELECT * FROM sudoku_puzzles WHERE difficulty = :difficulty ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<SudokuPuzzle> findRandomByDifficulty(@Param("difficulty") String difficulty);
}