package org.example.y9_gaming_site.streak;

import junit.framework.TestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class StreakServiceTest extends TestCase {

    private StreakRepository mockStreakRepository;
    private StreakService streakService;
    private Long userId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockStreakRepository = Mockito.mock(StreakRepository.class);
        streakService = new StreakService(mockStreakRepository);
        userId = 42L;
    }

    // test1: ახალი იუზერი პირველად შედის -> იქმნება ახალი სტრიკი (მნიშვნელობით 1)
    public void testUpdateStreak_NewUser_CreatesStreakOfOne() {
        when(mockStreakRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ArgumentCaptor<Streak> streakCaptor = ArgumentCaptor.forClass(Streak.class);

        streakService.updateStreak(userId);

        verify(mockStreakRepository, times(1)).save(streakCaptor.capture());
        Streak savedStreak = streakCaptor.getValue();

        assertEquals(userId, savedStreak.getUserId());
        assertEquals(Integer.valueOf(1), savedStreak.getCurrentStreak());
        assertEquals(LocalDate.now(), savedStreak.getLastLogin());
    }

    // test2: იუზერი დღეს უკვე შევიდა -> სტრიკი უცვლელი რჩება, save არ გამოიძახება
    public void testUpdateStreak_AlreadyLoggedToday_NoChange() {
        Streak existingStreak = new Streak();
        existingStreak.setUserId(userId);
        existingStreak.setCurrentStreak(7);
        existingStreak.setLastLogin(LocalDate.now());

        when(mockStreakRepository.findByUserId(userId)).thenReturn(Optional.of(existingStreak));

        streakService.updateStreak(userId);

        // რადგან დღეს უკვე შესულია, ბაზაში ახალი მონაცემი არ უნდა ჩაიწეროს
        verify(mockStreakRepository, never()).save(any(Streak.class));
        assertEquals(Integer.valueOf(7), existingStreak.getCurrentStreak());
    }

    // test3: ბოლო შესვლა იყო გუშინ -> სტრიკი იზრდება 1-ით
    public void testUpdateStreak_LastLoginYesterday_IncrementsStreak() {
        Streak existingStreak = new Streak();
        existingStreak.setUserId(userId);
        existingStreak.setCurrentStreak(4);
        existingStreak.setLastLogin(LocalDate.now().minusDays(1)); // გუშინ

        when(mockStreakRepository.findByUserId(userId)).thenReturn(Optional.of(existingStreak));

        streakService.updateStreak(userId);

        verify(mockStreakRepository, times(1)).save(existingStreak);
        assertEquals(Integer.valueOf(5), existingStreak.getCurrentStreak());
        assertEquals(LocalDate.now(), existingStreak.getLastLogin());
    }

    // test4: ბოლო შესვლა იყო რამდენიმე დღის წინ (გაწყდა) -> სტრიკი უნდა დავარესეტოთ 1-ზე
    public void testUpdateStreak_StreakBroken_ResetsToOne() {
        Streak existingStreak = new Streak();
        existingStreak.setUserId(userId);
        existingStreak.setCurrentStreak(12);
        existingStreak.setLastLogin(LocalDate.now().minusDays(3)); // 3 დღის წინ

        when(mockStreakRepository.findByUserId(userId)).thenReturn(Optional.of(existingStreak));

        streakService.updateStreak(userId);

        verify(mockStreakRepository, times(1)).save(existingStreak);
        assertEquals(Integer.valueOf(1), existingStreak.getCurrentStreak()); // ჩამოყარა 1-ზე
        assertEquals(LocalDate.now(), existingStreak.getLastLogin());
    }
}