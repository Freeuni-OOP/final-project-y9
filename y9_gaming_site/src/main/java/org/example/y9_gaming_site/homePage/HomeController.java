package org.example.y9_gaming_site.homePage;

import org.example.y9_gaming_site.homePage.HomeStatsDTO;
import org.example.y9_gaming_site.homePage.HomeStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HomeController
 *
 * GET /          → serves home.html from src/main/resources/static/
 * GET /stats/home → returns HomeStatsDTO as JSON (consumed by home.js)
 */
@RestController
public class HomeController {

    private final HomeStatsService homeStatsService;

    public HomeController(HomeStatsService homeStatsService) {
        this.homeStatsService = homeStatsService;
    }

    /**
     * Returns all stats the home page needs.
     * The auth guard (SecurityConfig) ensures only authenticated
     * users (or guests with a session) can reach this.
     */
    @GetMapping("/stats/home")
    public ResponseEntity<HomeStatsDTO> getHomeStats() {
        HomeStatsDTO stats = homeStatsService.getHomeStats();
        return ResponseEntity.ok(stats);
    }
}