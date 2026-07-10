package org.example.y9_gaming_site.streakTests;

import junit.framework.TestCase;
import org.example.y9_gaming_site.streak.Streak;
import org.example.y9_gaming_site.streak.StreakController;
import org.example.y9_gaming_site.streak.StreakService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class StreakControllerTests extends TestCase {

    private StreakService streakService;
    private StreakController streakController;

    private static final Long USER_ID = 1L;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        streakService = Mockito.mock(StreakService.class);
        streakController = new StreakController(streakService);
    }


    public void testGetStreakReturnsStreakWhenPresent() {
        Streak streak = new Streak();
        streak.setUserId(USER_ID);
        streak.setCurrentStreak(7);
        streak.setLastLogin(LocalDate.now());

        when(streakService.getStreak(USER_ID)).thenReturn(Optional.of(streak));

        Optional<Streak> result = streakController.getStreak(USER_ID);

        assertTrue(result.isPresent());
        assertEquals(streak, result.get());
        verify(streakService, times(1)).getStreak(USER_ID);
    }


    public void testGetStreakReturnsEmptyWhenNotFound() {
        when(streakService.getStreak(USER_ID)).thenReturn(Optional.empty());

        Optional<Streak> result = streakController.getStreak(USER_ID);

        assertTrue(result.isEmpty());
        verify(streakService, times(1)).getStreak(USER_ID);
    }

}