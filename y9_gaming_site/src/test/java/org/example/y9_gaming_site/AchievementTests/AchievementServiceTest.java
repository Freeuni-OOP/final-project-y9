package org.example.y9_gaming_site.AchievementTests;

import org.example.y9_gaming_site.achievement.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AchievementServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long ACHIEVEMENT_ID = 42L;
    private static final String CODE = "TEST_CODE";

    private AchievementRepository achievementRepository;
    private UserAchievementRepository userAchievementRepository;
    private AchievementService service;

    @BeforeEach
    void setUp() {
        achievementRepository = Mockito.mock(AchievementRepository.class);
        userAchievementRepository = Mockito.mock(UserAchievementRepository.class);
        service = new AchievementService(achievementRepository, userAchievementRepository, null);
    }

    private Achievement achievement() {
        Achievement a = new Achievement();
        a.setId(ACHIEVEMENT_ID);
        a.setCode(CODE);
        a.setName("Test achievement");
        a.setDescription("Test description");
        return a;
    }

    @Test
    void testSample1() {
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID)).thenReturn(Optional.empty());
        when(achievementRepository.findById(ACHIEVEMENT_ID)).thenReturn(Optional.of(achievement()));

        Optional<Achievement> result = service.grantAchievement(USER_ID, ACHIEVEMENT_ID);

        assertTrue(result.isPresent());
        assertEquals(CODE, result.get().getCode());
        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    void testSample2() {
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID))
                .thenReturn(Optional.of(new UserAchievement()));

        Optional<Achievement> result = service.grantAchievement(USER_ID, ACHIEVEMENT_ID);

        assertTrue(result.isEmpty());
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    void testSample3() {
        when(achievementRepository.findByCode("NOPE")).thenReturn(Optional.empty());

        Optional<Achievement> result = service.grantByCode(USER_ID, "NOPE");

        assertTrue(result.isEmpty());
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    void testSample4() {
        when(achievementRepository.findByCode(CODE)).thenReturn(Optional.of(achievement()));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID)).thenReturn(Optional.empty());
        when(achievementRepository.findById(ACHIEVEMENT_ID)).thenReturn(Optional.of(achievement()));

        Optional<Achievement> result = service.grantByCode(USER_ID, CODE);

        assertTrue(result.isPresent());
        assertEquals(CODE, result.get().getCode());
    }

    @Test
    void testSample5() {
        when(achievementRepository.findByCode(CODE)).thenReturn(Optional.of(achievement()));
        when(achievementRepository.findById(ACHIEVEMENT_ID)).thenReturn(Optional.of(achievement()));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new UserAchievement()));

        Optional<Achievement> first = service.grantByCode(USER_ID, CODE);
        Optional<Achievement> second = service.grantByCode(USER_ID, CODE);

        assertTrue(first.isPresent());
        assertTrue(second.isEmpty());
        verify(userAchievementRepository, times(1)).save(any());
    }

    @Test
    void testSample6() {
        assertTrue(service.grantByCode(null, CODE).isEmpty());
        assertTrue(service.grantByCode(USER_ID, null).isEmpty());
        verifyNoInteractions(achievementRepository);
    }
}
