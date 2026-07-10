package org.example.y9_gaming_site.streakTests;

import junit.framework.TestCase;
import org.example.y9_gaming_site.streak.Streak;
import org.example.y9_gaming_site.streak.StreakRepository;
import org.example.y9_gaming_site.streak.StreakService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class StreakServiceTests extends TestCase {

    private StreakRepository streakRepository;
    private StreakService streakService;

    private static final Long USER_ID = 1L;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        streakRepository = Mockito.mock(StreakRepository.class);
        streakService = new StreakService(streakRepository);
    }


    public void testUpdateStreakCreatesNewStreakForNewUser() {
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        streakService.updateStreak(USER_ID);

        ArgumentCaptor<Streak> captor = ArgumentCaptor.forClass(Streak.class);
        verify(streakRepository, times(1)).save(captor.capture());

        Streak saved = captor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(Integer.valueOf(1), saved.getCurrentStreak());
        assertEquals(LocalDate.now(), saved.getLastLogin());
    }


    public void testUpdateStreakDoesNothingWhenAlreadyLoggedInToday() {
        Streak existing = new Streak();
        existing.setUserId(USER_ID);
        existing.setCurrentStreak(5);
        existing.setLastLogin(LocalDate.now());

        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        streakService.updateStreak(USER_ID);

        assertEquals(Integer.valueOf(5), existing.getCurrentStreak());
        assertEquals(LocalDate.now(), existing.getLastLogin());
        verify(streakRepository, never()).save(any());
    }


    public void testUpdateStreakIncrementsWhenLastLoginWasYesterday() {
        Streak existing = new Streak();
        existing.setUserId(USER_ID);
        existing.setCurrentStreak(5);
        existing.setLastLogin(LocalDate.now().minusDays(1));

        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        streakService.updateStreak(USER_ID);

        assertEquals(Integer.valueOf(6), existing.getCurrentStreak());
        assertEquals(LocalDate.now(), existing.getLastLogin());
        verify(streakRepository, times(1)).save(existing);
    }


    public void testUpdateStreakResetsWhenGapInLogins() {
        Streak existing = new Streak();
        existing.setUserId(USER_ID);
        existing.setCurrentStreak(10);
        existing.setLastLogin(LocalDate.now().minusDays(3));

        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        streakService.updateStreak(USER_ID);

        assertEquals(Integer.valueOf(1), existing.getCurrentStreak());
        assertEquals(LocalDate.now(), existing.getLastLogin());
        verify(streakRepository, times(1)).save(existing);
    }


    public void testGetStreakReturnsExistingStreak() {
        Streak existing = new Streak();
        existing.setUserId(USER_ID);
        existing.setCurrentStreak(3);
        existing.setLastLogin(LocalDate.now());

        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        Optional<Streak> result = streakService.getStreak(USER_ID);

        assertTrue(result.isPresent());
        assertEquals(existing, result.get());
        verify(streakRepository, times(1)).findByUserId(USER_ID);
    }


    public void testGetStreakReturnsEmptyWhenNoneExists() {
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        Optional<Streak> result = streakService.getStreak(USER_ID);

        assertTrue(result.isEmpty());
        verify(streakRepository, times(1)).findByUserId(USER_ID);
    }

}