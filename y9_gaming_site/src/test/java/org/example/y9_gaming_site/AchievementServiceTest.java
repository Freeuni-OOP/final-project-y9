package org.example.y9_gaming_site;

import org.example.y9_gaming_site.achievement.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
        service = new AchievementService(achievementRepository, userAchievementRepository);
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
    @DisplayName("Granting a new achievement by id saves it and returns it")
    void grantAchievementReturnsAchievementWhenNew() {
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID)).thenReturn(Optional.empty());
        when(achievementRepository.findById(ACHIEVEMENT_ID)).thenReturn(Optional.of(achievement()));

        Optional<Achievement> result = service.grantAchievement(USER_ID, ACHIEVEMENT_ID);

        assertTrue(result.isPresent());
        assertEquals(CODE, result.get().getCode());
        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    @DisplayName("Granting an already-earned achievement returns empty and does not save again")
    void grantAchievementReturnsEmptyWhenAlreadyEarned() {
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID))
                .thenReturn(Optional.of(new UserAchievement()));

        Optional<Achievement> result = service.grantAchievement(USER_ID, ACHIEVEMENT_ID);

        assertTrue(result.isEmpty());
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Granting by an unknown code returns empty and never touches the user-achievement table")
    void grantByCodeReturnsEmptyForUnknownCode() {
        when(achievementRepository.findByCode("NOPE")).thenReturn(Optional.empty());

        Optional<Achievement> result = service.grantByCode(USER_ID, "NOPE");

        assertTrue(result.isEmpty());
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Granting by code resolves the achievement through its code and grants it")
    void grantByCodeGrantsResolvedAchievement() {
        when(achievementRepository.findByCode(CODE)).thenReturn(Optional.of(achievement()));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID)).thenReturn(Optional.empty());
        when(achievementRepository.findById(ACHIEVEMENT_ID)).thenReturn(Optional.of(achievement()));

        Optional<Achievement> result = service.grantByCode(USER_ID, CODE);

        assertTrue(result.isPresent());
        assertEquals(CODE, result.get().getCode());
    }

    @Test
    @DisplayName("A repeated grantByCode call for the same user+code only ever returns present once")
    void grantByCodeIsIdempotent() {
        when(achievementRepository.findByCode(CODE)).thenReturn(Optional.of(achievement()));
        when(achievementRepository.findById(ACHIEVEMENT_ID)).thenReturn(Optional.of(achievement()));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID))
                .thenReturn(Optional.empty()) // first call: not earned yet
                .thenReturn(Optional.of(new UserAchievement())); // second call: now earned

        Optional<Achievement> first = service.grantByCode(USER_ID, CODE);
        Optional<Achievement> second = service.grantByCode(USER_ID, CODE);

        assertTrue(first.isPresent());
        assertTrue(second.isEmpty());
        verify(userAchievementRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Granting by code with a null userId or code is a no-op")
    void grantByCodeHandlesNulls() {
        assertTrue(service.grantByCode(null, CODE).isEmpty());
        assertTrue(service.grantByCode(USER_ID, null).isEmpty());
        verifyNoInteractions(achievementRepository);
    }
}
