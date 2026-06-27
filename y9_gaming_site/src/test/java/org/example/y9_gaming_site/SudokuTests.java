package org.example.y9_gaming_site;

import junit.framework.TestCase;
import org.example.y9_gaming_site.game.SudokuPuzzleRepository;
import org.example.y9_gaming_site.game.sudoku.*;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

public class SudokuTests extends TestCase {

    private SudokuPuzzleRepository mockRepository;
    private SudokuService sudokuService;
    private SudokuPuzzle dailyPuzzle;
    private SudokuPuzzle randomPuzzle;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockRepository = Mockito.mock(SudokuPuzzleRepository.class);
        sudokuService = new SudokuService(mockRepository);

        String blankBoardStr = "000260701680070090190004500820100040004602900050003028009300074040050036703018000";
        String solvedBoardStr = "435269781682571394197834562826195347374682915951743628219364874548957236763418259";

        dailyPuzzle = new SudokuPuzzle();
        dailyPuzzle.setId(1L);
        dailyPuzzle.setPuzzleDate(LocalDate.now());
        dailyPuzzle.setDefinition(blankBoardStr);
        dailyPuzzle.setSolution(solvedBoardStr);
        dailyPuzzle.setDifficulty("MEDIUM");

        randomPuzzle = new SudokuPuzzle();
        randomPuzzle.setId(2L);
        randomPuzzle.setPuzzleDate(null); // Infinite practice boards have no date assigned
        randomPuzzle.setDefinition(blankBoardStr);
        randomPuzzle.setSolution(solvedBoardStr);
        randomPuzzle.setDifficulty("HARD");
    }


    public void test1() {
        Mockito.when(mockRepository.findByPuzzleDate(LocalDate.now())).thenReturn(Optional.of(dailyPuzzle));

        SudokuPuzzle result = sudokuService.getDailyPuzzle();

        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("MEDIUM", result.getDifficulty());
        assertEquals(LocalDate.now(), result.getPuzzleDate());
    }


    public void test2() {
        Mockito.when(mockRepository.findByPuzzleDate(LocalDate.now())).thenReturn(Optional.empty());
        Mockito.when(mockRepository.findRandomByDifficulty("MEDIUM")).thenReturn(Optional.of(randomPuzzle));

        SudokuPuzzle result = sudokuService.getDailyPuzzle();

        assertNotNull(result);
        assertEquals(2L, result.getId().longValue());
        assertEquals("HARD", result.getDifficulty()); // Checked random fallback returned entity successfully
    }


    public void test3() {
        Mockito.when(mockRepository.findById(2L)).thenReturn(Optional.of(randomPuzzle));

        Optional<SudokuPuzzle> result = sudokuService.getPuzzleById(2L);

        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getId().longValue());
        assertNull(result.get().getPuzzleDate());
    }


    public void test4() {
        Mockito.when(mockRepository.findById(1L)).thenReturn(Optional.of(dailyPuzzle));
        boolean isCorrect = sudokuService.verifySolution(1L, "435269781682571394197834562826195347374682915951743628219364874548957236763418259");
        assertTrue(isCorrect);
        boolean isWrong = sudokuService.verifySolution(1L, "000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        assertFalse(isWrong);
    }
}