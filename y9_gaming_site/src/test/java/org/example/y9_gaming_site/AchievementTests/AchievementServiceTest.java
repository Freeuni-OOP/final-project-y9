package org.example.y9_gaming_site.AchievementTests;

import org.example.y9_gaming_site.achievement.*;
import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AchievementServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long ACHIEVEMENT_ID = 42L;
    private static final String CODE = "TEST_CODE";

    @Mock
    private AchievementRepository achievementRepository;
    @Mock
    private UserAchievementRepository userAchievementRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AchievementService service;

    private Achievement achievement() {
        Achievement a = new Achievement();
        a.setId(ACHIEVEMENT_ID);
        a.setCode(CODE);
        a.setName("Test achievement");
        a.setDescription("Test description");
        return a;
    }

    private User regularUser() {
        User u = new User();
        u.setId(USER_ID);
        u.setRole(Role.USER);
        return u;
    }
    private void notAGuest() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(regularUser()));
    }

    @Test
    void testSample1() { // grants the achievement when it hasn't been earned yet
        notAGuest();
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID)).thenReturn(Optional.empty());
        when(achievementRepository.findById(ACHIEVEMENT_ID)).thenReturn(Optional.of(achievement()));

        Optional<Achievement> result = service.grantAchievement(USER_ID, ACHIEVEMENT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo(CODE);
        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    void testSample2() { // already-earned achievements are never granted twice
        notAGuest();
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID))
                .thenReturn(Optional.of(new UserAchievement()));

        Optional<Achievement> result = service.grantAchievement(USER_ID, ACHIEVEMENT_ID);

        assertThat(result).isEmpty();
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    void testSample3() { //guest accounts are blocked before any earned-check or save happens
        User guest = new User();
        guest.setId(USER_ID);
        guest.setRole(Role.GUEST);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(guest));

        Optional<Achievement> result = service.grantAchievement(USER_ID, ACHIEVEMENT_ID);

        assertThat(result).isEmpty();
        verify(userAchievementRepository, never()).findByUserIdAndAchievementId(any(), any());
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    void testSample4() { // grantByCode with an unknown code grants nothing
        when(achievementRepository.findByCode("NOPE")).thenReturn(Optional.empty());

        Optional<Achievement> result = service.grantByCode(USER_ID, "NOPE");

        assertThat(result).isEmpty();
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    void testSample5() { // grantByCode resolves the code then grants exactly like grantAchievement
        notAGuest();
        when(achievementRepository.findByCode(CODE)).thenReturn(Optional.of(achievement()));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID)).thenReturn(Optional.empty());
        when(achievementRepository.findById(ACHIEVEMENT_ID)).thenReturn(Optional.of(achievement()));

        Optional<Achievement> result = service.grantByCode(USER_ID, CODE);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo(CODE);
    }

    @Test
    void testSample6() { // the second grantByCode call for the same code is a no op
        notAGuest();
        when(achievementRepository.findByCode(CODE)).thenReturn(Optional.of(achievement()));
        when(achievementRepository.findById(ACHIEVEMENT_ID)).thenReturn(Optional.of(achievement()));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, ACHIEVEMENT_ID))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new UserAchievement()));

        Optional<Achievement> first = service.grantByCode(USER_ID, CODE);
        Optional<Achievement> second = service.grantByCode(USER_ID, CODE);

        assertThat(first).isPresent();
        assertThat(second).isEmpty();
        verify(userAchievementRepository, times(1)).save(any());
    }

    @Test
    void testSample7() { // a null userId or code short-circuits before touching either repository
        assertThat(service.grantByCode(null, CODE)).isEmpty();
        assertThat(service.grantByCode(USER_ID, null)).isEmpty();
        verifyNoInteractions(achievementRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void testSample8() { // getUserAchievement is plain passthrough to the repository
        List<UserAchievement> expected = List.of(new UserAchievement());
        when(userAchievementRepository.findByUserId(USER_ID)).thenReturn(expected);

        assertThat(service.getUserAchievement(USER_ID)).isEqualTo(expected);
    }

    @Test
    void testSample9() { //getEarnedView joins earned rows with catalog and fills in how many people share each one
        UserAchievement earned = new UserAchievement();
        earned.setAchievementId(ACHIEVEMENT_ID);
        earned.setEarnedTime(LocalDateTime.of(2026, 1, 1, 12, 0));
        when(userAchievementRepository.findByUserId(USER_ID)).thenReturn(List.of(earned));
        when(achievementRepository.findAll()).thenReturn(List.of(achievement()));
        when(userAchievementRepository.countByAchievementId(ACHIEVEMENT_ID)).thenReturn(7L);

        List<AchievementView> views = service.getEarnedView(USER_ID);

        assertThat(views).hasSize(1);
        assertThat(views.get(0).code()).isEqualTo(CODE);
        assertThat(views.get(0).earnedCount()).isEqualTo(7L);
    }

    @Test
    void testSample10() { //an earned row pointing at achievement no longer in the catalog is skipped not crash
        UserAchievement orphan = new UserAchievement();
        orphan.setAchievementId(999L);
        when(userAchievementRepository.findByUserId(USER_ID)).thenReturn(List.of(orphan));
        when(achievementRepository.findAll()).thenReturn(List.of());

        assertThat(service.getEarnedView(USER_ID)).isEmpty();
    }

    @Test
    void testSample11() { // no earned achievements means the catalog is never even fetched
        when(userAchievementRepository.findByUserId(USER_ID)).thenReturn(List.of());

        assertThat(service.getEarnedView(USER_ID)).isEmpty();
        verify(achievementRepository, never()).findAll();
    }

    @Test
    void testSample12() { // getRarestEarned sorts fewest holders first ties broken by most recently earned
        UserAchievement common = new UserAchievement();
        common.setAchievementId(1L);
        common.setEarnedTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        UserAchievement rareOld = new UserAchievement();
        rareOld.setAchievementId(2L);
        rareOld.setEarnedTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        UserAchievement rareNew = new UserAchievement();
        rareNew.setAchievementId(3L);
        rareNew.setEarnedTime(LocalDateTime.of(2026, 6, 1, 0, 0));

        when(userAchievementRepository.findByUserId(USER_ID)).thenReturn(List.of(common, rareOld, rareNew));
        when(achievementRepository.findAll()).thenReturn(List.of(
                achievementWith(1L, "COMMON"), achievementWith(2L, "RARE_OLD"), achievementWith(3L, "RARE_NEW")
        ));
        when(userAchievementRepository.countByAchievementId(1L)).thenReturn(50L);
        when(userAchievementRepository.countByAchievementId(2L)).thenReturn(1L);
        when(userAchievementRepository.countByAchievementId(3L)).thenReturn(1L);

        List<AchievementView> rarest = service.getRarestEarned(USER_ID, 2);

        assertThat(rarest).extracting(AchievementView::code).containsExactly("RARE_NEW", "RARE_OLD");
    }

    private Achievement achievementWith(Long id, String code) {
        Achievement a = new Achievement();
        a.setId(id);
        a.setCode(code);
        a.setName(code);
        a.setDescription(code);
        return a;
    }
}
