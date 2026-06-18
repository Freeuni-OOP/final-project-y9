package org.example.y9_gaming_site.homePage;

import org.example.y9_gaming_site.homePage.HomeStatsDTO;
import org.example.y9_gaming_site.homePage.HomeStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * HomeController
 *
 * GET /          → serves home.html from src/main/resources/static/
 * GET /stats/home → returns HomeStatsDTO as JSON (consumed by home.js)
 */
@Controller
public class HomeController {

    private final HomeStatsService homeStatsService;

    public HomeController(HomeStatsService homeStatsService) {
        this.homeStatsService = homeStatsService;
    }

    // Serves the HTML page
    @GetMapping("/homePage")
    public String homePage() {
        return "homePage"; // resolves to templates/homePage.html (Thymeleaf)
    }

    // Returns JSON stats for the JS to fetch
    @GetMapping("/stats/home")
    @ResponseBody
    public ResponseEntity<HomeStatsDTO> getHomeStats() {
        HomeStatsDTO stats = homeStatsService.getHomeStats();
        return ResponseEntity.ok(stats);
    }
}