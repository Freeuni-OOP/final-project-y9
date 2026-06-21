package org.example.y9_gaming_site;

import junit.framework.TestCase;
import org.example.y9_gaming_site.admin.*;
import org.example.y9_gaming_site.streak.StreakService;
import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.example.y9_gaming_site.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminServiceTests extends TestCase {


    private UserRepository userRepository;


    private AnnouncementRepository announcementRepository;


    private ChallengeRepository challengeRepository;


    private AdminService adminService;

    private User testUser;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        userRepository = Mockito.mock(UserRepository.class);
        announcementRepository = Mockito.mock(AnnouncementRepository.class);
        challengeRepository = Mockito.mock(ChallengeRepository.class);
        adminService = new AdminService(userRepository, announcementRepository, challengeRepository);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setRole(Role.USER);
        testUser.setBanned(false);
    }



    public void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<User> result = adminService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }


    public void testBanUser1() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        adminService.banUser(1L, "cheating");

        assertTrue(testUser.getBanned());
        verify(userRepository, times(1)).save(testUser);
    }


    public void testBanUser2() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.banUser(99L, "reason"));
        verify(userRepository, never()).save(any());
    }


    public void testUnbanUser() {
        testUser.setBanned(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        adminService.unbanUser(1L);

        assertFalse(testUser.getBanned());
        verify(userRepository, times(1)).save(testUser);
    }


    public void testChangeUserRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        adminService.changeUserRole(1L, Role.ADMIN);

        assertEquals(Role.ADMIN, testUser.getRole());
        verify(userRepository, times(1)).save(testUser);
    }


    public void testDeleteUser() {
        adminService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }



    public void testGetAllAnnouncements() {
        Announcement a = new Announcement();
        a.setTitle("Test Announcement");
        when(announcementRepository.findAll()).thenReturn(List.of(a));

        List<Announcement> result = adminService.getAllAnnouncements();

        assertEquals(1, result.size());
        assertEquals("Test Announcement", result.get(0).getTitle());
    }


    public void testCreateAnnouncement() {
        AnnouncementDTO dto = new AnnouncementDTO("Title", "Content");

        adminService.createAnnouncement(dto);

        verify(announcementRepository, times(1)).save(any(Announcement.class));
    }

    public void testDeleteAnnouncement() {
        adminService.deleteAnnouncement(1L);

        verify(announcementRepository, times(1)).deleteById(1L);
    }



    public void testGetAllChallenges() {
        Challenge c = new Challenge();
        c.setTitle("Win 10 games");
        when(challengeRepository.findAll()).thenReturn(List.of(c));

        List<Challenge> result = adminService.getAllChallenges();

        assertEquals(1, result.size());
        assertEquals("Win 10 games", result.get(0).getTitle());
    }


    public void testCreateChallenge() {
        ChallengeDTO dto = new ChallengeDTO("Title", "Description", "100 points");

        adminService.createChallenge(dto);

        verify(challengeRepository, times(1)).save(any(Challenge.class));
    }


    public void testDeleteChallenge() {
        adminService.deleteChallenge(1L);

        verify(challengeRepository, times(1)).deleteById(1L);
    }

}