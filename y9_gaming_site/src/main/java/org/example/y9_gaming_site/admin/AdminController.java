package org.example.y9_gaming_site.admin;

import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.game.Game;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        CompletableFuture<List<User>> usersF = CompletableFuture.supplyAsync(adminService::getAllUsers);
        CompletableFuture<List<Challenge>> challengesF = CompletableFuture.supplyAsync(adminService::getAllChallenges);
        CompletableFuture<List<Game>> gamesF = CompletableFuture.supplyAsync(adminService::getAllGames);


        model.addAttribute("users", usersF.join());
        model.addAttribute("challenges", challengesF.join());
        model.addAttribute("games", gamesF.join());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String viewAllUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        adminService.deleteUser(id);
        ra.addFlashAttribute("message", "User deleted successfully.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id,
                             @RequestParam Role role,
                             RedirectAttributes ra) {
        adminService.changeUserRole(id, role);
        ra.addFlashAttribute("message", "User role updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/ban")
    public String banUser(@PathVariable Long id,
                          @RequestParam(required = false) String reason,
                          RedirectAttributes ra) {
        adminService.banUser(id, reason); // passing both id and reason
        ra.addFlashAttribute("message", "User banned.");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/banned")
    public String viewBannedUsers(Model model) {
        model.addAttribute("bannedUsers", adminService.getAllBannedUsers());
        return "admin/banned-users";
    }

    @PostMapping("/users/{id}/unban")
    public String unbanUser(@PathVariable Long id, RedirectAttributes ra) {
        adminService.unbanUser(id);
        ra.addFlashAttribute("message", "User unbanned.");
        return "redirect:/admin/users";
    }




    @GetMapping("/challenges")
    public String viewChallenges(Model model, @AuthenticationPrincipal User loggedInUser) {
        model.addAttribute("challenges", adminService.getAllChallenges());
        model.addAttribute("newChallenge", new ChallengeDTO());
        model.addAttribute("currentAdmin", loggedInUser);
        return "admin/challenges";
    }

    @PostMapping("/challenges/create")
    public String createChallenge(@ModelAttribute ChallengeDTO dto,
                                  @AuthenticationPrincipal User loggedInUser,
                                  RedirectAttributes ra) throws AccessDeniedException {
        adminService.createChallenge(dto, loggedInUser.getUsername());
        ra.addFlashAttribute("message", "Challenge created successfully.");
        return "redirect:/admin/challenges";
    }

    @PostMapping("/challenges/{id}/delete")
    public String deleteChallenge(@PathVariable Long id, RedirectAttributes ra) {
        adminService.deleteChallenge(id);
        ra.addFlashAttribute("message", "Challenge deleted.");
        return "redirect:/admin/challenges";
    }




}