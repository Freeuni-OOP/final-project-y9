package org.example.y9_gaming_site.AchievementTests;

import junit.framework.TestCase;
import org.example.y9_gaming_site.achievement.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

public class AchievementControllerTests extends TestCase {

    private AchievementService achService;
    private AchievementRepository repository;
    private AchievementController achievementController;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        achService = Mockito.mock(AchievementService.class);
        repository = Mockito.mock(AchievementRepository.class);
        achievementController = new AchievementController(achService, repository);
    }


    public void testGetUserAchievements() {
        UserAchievement ua = new UserAchievement();
        when(achService.getUserAchievement(1L)).thenReturn(List.of(ua));

        List<UserAchievement> result = achievementController.getUserAchievements(1L);

        assertEquals(1, result.size());
        assertEquals(ua, result.get(0));
        verify(achService, times(1)).getUserAchievement(1L);
    }


    public void testGetAchievementsView() {
        AchievementView view = new AchievementView(
                "WORDLE_FIRST_WIN", "პირველი გამარჯვება", "მოიგე ვორდლი პირველად",
                LocalDateTime.now(), 1L);
        when(achService.getEarnedView(1L)).thenReturn(List.of(view));

        List<AchievementView> result = achievementController.getAchievementsView(1L);

        assertEquals(1, result.size());
        assertEquals(view, result.get(0));
        verify(achService, times(1)).getEarnedView(1L);
    }


    public void testGetRarestDefaultLimit() {
        AchievementView view = new AchievementView(
                "WORDLE_IN_ONE_TRY", "შეუძლებელი", "გამოიცანი სიტყვა ერთი ცდით",
                LocalDateTime.now(), 2L);
        when(achService.getRarestEarned(1L, 3)).thenReturn(List.of(view));

        List<AchievementView> result = achievementController.getRarest(1L, 3);

        assertEquals(1, result.size());
        assertEquals(view, result.get(0));
        verify(achService, times(1)).getRarestEarned(1L, 3);
    }


    public void testGetRarestCustomLimit() {
        AchievementView view = new AchievementView(
                "SUDOKU_IN_60", "wtf dude", "ამოხსენი სუდოკუ 1 წუთზე ნაკლებში",
                LocalDateTime.now(), 3L);
        when(achService.getRarestEarned(1L, 5)).thenReturn(List.of(view));

        List<AchievementView> result = achievementController.getRarest(1L, 5);

        assertEquals(1, result.size());
        assertEquals(view, result.get(0));
        verify(achService, times(1)).getRarestEarned(1L, 5);
        verify(achService, never()).getRarestEarned(1L, 3);
    }


    public void testGetCatalog() {
        Achievement a = new Achievement();
        a.setCode("WORDLE_FIRST_WIN");
        when(repository.findAll()).thenReturn(List.of(a));

        List<Achievement> result = achievementController.getCatalog();

        assertEquals(1, result.size());
        assertEquals("WORDLE_FIRST_WIN", result.get(0).getCode());
        verify(repository, times(1)).findAll();
    }

}