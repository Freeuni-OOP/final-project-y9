package org.example.y9_gaming_site.homePage;

import org.example.y9_gaming_site.homePage.HomeStatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomePageController {
    private final HomeStatsService homeStatsService;

    public HomePageController(HomeStatsService homeStatsService) {
        this.homeStatsService = homeStatsService;
    }


    @GetMapping("/stats/home")
    @ResponseBody
    public HomeStatsDTO getHomeStats() {
        return homeStatsService.getHomeStats();
    }


}
