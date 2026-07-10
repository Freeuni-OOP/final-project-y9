package org.example.y9_gaming_site.AchievementTests;

import junit.framework.TestCase;
import org.example.y9_gaming_site.achievement.Achievement;
import org.example.y9_gaming_site.achievement.AchievementRepository;
import org.example.y9_gaming_site.achievement.AchievementSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class AchievementSeederTests extends TestCase {

    private AchievementRepository achievementRepository;
    private AchievementSeeder achievementSeeder;

    private static final int TOTAL_DEFS = 17;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        achievementRepository = Mockito.mock(AchievementRepository.class);
        achievementSeeder = new AchievementSeeder(achievementRepository);
    }


    public void testRunCreatesAllAchievementsWhenNoneExist() {
        when(achievementRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementSeeder.run();

        verify(achievementRepository, times(TOTAL_DEFS)).save(any(Achievement.class));
        verify(achievementRepository, times(TOTAL_DEFS)).findByCode(anyString());
    }


    public void testRunSetsCorrectFieldsForNewAchievement() {
        when(achievementRepository.findByCode("WORDLE_FIRST_WIN")).thenReturn(Optional.empty());
        when(achievementRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementSeeder.run();

        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(TOTAL_DEFS)).save(captor.capture());

        Achievement wordleFirstWin = captor.getAllValues().stream()
                .filter(a -> "WORDLE_FIRST_WIN".equals(a.getCode()))
                .findFirst()
                .orElse(null);

        assertNotNull(wordleFirstWin);
        assertEquals("პირველი გამარჯვება", wordleFirstWin.getName());
        assertEquals("მოიგე ვორდლი პირველად", wordleFirstWin.getDescription());
        assertEquals(100, wordleFirstWin.getPointReward());
    }


    public void testRunUpdatesExistingAchievementInsteadOfDuplicating() {
        Achievement existing = new Achievement();
        existing.setId(42L);
        existing.setCode("WORDLE_FIRST_WIN");
        existing.setName("Old Name");
        existing.setDescription("Old Description");
        existing.setPointReward(1);

        when(achievementRepository.findByCode("WORDLE_FIRST_WIN")).thenReturn(Optional.of(existing));
        when(achievementRepository.findByCode(argThat(code -> !"WORDLE_FIRST_WIN".equals(code))))
                .thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementSeeder.run();

        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(TOTAL_DEFS)).save(captor.capture());

        Achievement updated = captor.getAllValues().stream()
                .filter(a -> "WORDLE_FIRST_WIN".equals(a.getCode()))
                .findFirst()
                .orElse(null);

        assertNotNull(updated);
        assertEquals(Long.valueOf(42L), updated.getId());
        assertEquals("პირველი გამარჯვება", updated.getName());
        assertEquals("მოიგე ვორდლი პირველად", updated.getDescription());
        assertEquals(100, updated.getPointReward());
    }


    public void testRunDoesNotSaveMoreThanDefinedAchievements() {
        when(achievementRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementSeeder.run();

        verify(achievementRepository, atMost(TOTAL_DEFS)).save(any(Achievement.class));
    }

}