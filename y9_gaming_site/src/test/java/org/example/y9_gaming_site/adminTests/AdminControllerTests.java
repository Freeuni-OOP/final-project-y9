package org.example.y9_gaming_site.adminTests;

import junit.framework.TestCase;
import org.example.y9_gaming_site.admin.*;
import org.example.y9_gaming_site.game.Game;
import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.mockito.Mockito.*;

public class AdminControllerTests extends TestCase {

    private AdminService adminService;
    private AdminController adminController;

    private Model model;
    private RedirectAttributesModelMap redirectAttributes;

    private User testUser;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        adminService = Mockito.mock(AdminService.class);
        adminController = new AdminController(adminService);

        model = new ExtendedModelMap();
        redirectAttributes = new RedirectAttributesModelMap();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setRole(Role.USER);
        testUser.setBanned(false);
    }


    public void testDashboard() {
        Challenge c = new Challenge();
        c.setTitle("Win 10 games");
        Game g = new Game();

        when(adminService.getAllUsers()).thenReturn(List.of(testUser));
        when(adminService.getAllChallenges()).thenReturn(List.of(c));
        when(adminService.getAllGames()).thenReturn(List.of(g));

        String view = adminController.dashboard(model);

        assertEquals("admin/dashboard", view);
        assertEquals(List.of(testUser), model.getAttribute("users"));
        assertEquals(List.of(c), model.getAttribute("challenges"));
        assertEquals(List.of(g), model.getAttribute("games"));
        verify(adminService, times(1)).getAllUsers();
        verify(adminService, times(1)).getAllChallenges();
        verify(adminService, times(1)).getAllGames();
    }


    public void testViewAllUsers() {
        when(adminService.getAllUsers()).thenReturn(List.of(testUser));

        String view = adminController.viewAllUsers(model);

        assertEquals("admin/users", view);
        assertEquals(List.of(testUser), model.getAttribute("users"));
        verify(adminService, times(1)).getAllUsers();
    }


    public void testDeleteUser() {
        String view = adminController.deleteUser(1L, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        assertEquals("User deleted successfully.", redirectAttributes.getFlashAttributes().get("message"));
        verify(adminService, times(1)).deleteUser(1L);
    }


    public void testChangeRole() {
        String view = adminController.changeRole(1L, Role.ADMIN, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        assertEquals("User role updated.", redirectAttributes.getFlashAttributes().get("message"));
        verify(adminService, times(1)).changeUserRole(1L, Role.ADMIN);
    }


    public void testBanUser() {
        String view = adminController.banUser(1L, "cheating", redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        assertEquals("User banned.", redirectAttributes.getFlashAttributes().get("message"));
        verify(adminService, times(1)).banUser(1L, "cheating");
    }


    public void testBanUserNoReason() {
        String view = adminController.banUser(1L, null, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        verify(adminService, times(1)).banUser(1L, null);
    }


    public void testViewBannedUsers() {
        testUser.setBanned(true);
        when(adminService.getAllBannedUsers()).thenReturn(List.of(testUser));

        String view = adminController.viewBannedUsers(model);

        assertEquals("admin/banned-users", view);
        assertEquals(List.of(testUser), model.getAttribute("bannedUsers"));
        verify(adminService, times(1)).getAllBannedUsers();
    }


    public void testUnbanUser() {
        String view = adminController.unbanUser(1L, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        assertEquals("User unbanned.", redirectAttributes.getFlashAttributes().get("message"));
        verify(adminService, times(1)).unbanUser(1L);
    }


    public void testViewChallenges() {
        Challenge c = new Challenge();
        c.setTitle("Win 10 games");
        when(adminService.getAllChallenges()).thenReturn(List.of(c));

        String view = adminController.viewChallenges(model, testUser);

        assertEquals("admin/challenges", view);
        assertEquals(List.of(c), model.getAttribute("challenges"));
        assertNotNull(model.getAttribute("newChallenge"));
        assertTrue(model.getAttribute("newChallenge") instanceof ChallengeDTO);
        assertEquals(testUser, model.getAttribute("currentAdmin"));
        verify(adminService, times(1)).getAllChallenges();
    }


    public void testCreateChallenge() throws Exception {
        ChallengeDTO dto = new ChallengeDTO();
        dto.setTitle("New Challenge");
        dto.setDescription("Beat the high score");
        dto.setReward("100");

        String view = adminController.createChallenge(dto, testUser, redirectAttributes);

        assertEquals("redirect:/admin/challenges", view);
        assertEquals("Challenge created successfully.", redirectAttributes.getFlashAttributes().get("message"));
        verify(adminService, times(1)).createChallenge(dto, "testuser");
    }


    public void testDeleteChallenge() {
        String view = adminController.deleteChallenge(1L, redirectAttributes);

        assertEquals("redirect:/admin/challenges", view);
        assertEquals("Challenge deleted.", redirectAttributes.getFlashAttributes().get("message"));
        verify(adminService, times(1)).deleteChallenge(1L);
    }

}






