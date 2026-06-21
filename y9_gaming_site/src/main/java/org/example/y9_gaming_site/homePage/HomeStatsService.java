package org.example.y9_gaming_site.homePage;

import org.example.y9_gaming_site.homePage.HomeStatsDTO;
import org.example.y9_gaming_site.homePage.HomeStatsDTO.RankedPlayerDTO;
import org.example.y9_gaming_site.homePage.HomeStatsDTO.RecentAchievementDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HomeStatsService
 *
 * Assembles everything the home page needs in one place.
 * Right now it returns hard-coded stub data so you can build
 * and test the UI immediately.
 *
 * TODO: inject your UserRepository, AchievementRepository, etc.
 *       and replace each stub method with real DB queries.
 */
@Service
public class HomeStatsService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy")
                    .withZone(ZoneId.systemDefault());

    public HomeStatsDTO getHomeStats() {
        HomeStatsDTO dto = new HomeStatsDTO();

        dto.setTotalUsers(fetchTotalUsers());
        dto.setTopPlayers(fetchTopPlayers());
        dto.setRecentAchievements(fetchRecentAchievements());

        return dto;
    }

    // ---------------------------------------------------------------
    // stub methods — replace with repository calls
    // ---------------------------------------------------------------

    private long fetchTotalUsers() {
        // TODO: return userRepository.count();
        return 1_284;
    }

    private List<RankedPlayerDTO> fetchTopPlayers() {
        // TODO: return userRepository.findTop5ByOrderByScoreDesc()
        //           .stream().map(this::toRankedDTO).toList();
        return List.of(
                new RankedPlayerDTO("NinoG",   "/img/avatars/1.png", 1, 9820),
                new RankedPlayerDTO("GiorgiK", "/img/avatars/2.png", 2, 8740),
                new RankedPlayerDTO("TamaraB", "/img/avatars/3.png", 3, 7910),
                new RankedPlayerDTO("LevanM",  "/img/avatars/4.png", 4, 6550),
                new RankedPlayerDTO("MariamS", "/img/avatars/5.png", 5, 5320)
        );
    }

    private List<RecentAchievementDTO> fetchRecentAchievements() {
        // TODO: return achievementRepository.findTop10ByOrderByEarnedAtDesc()
        //           .stream().map(this::toAchievementDTO).toList();
        String now = FORMATTER.format(Instant.now());
        return List.of(
                new RecentAchievementDTO("NinoG",   "First Blood",    "/img/ach/first_blood.png",   now),
                new RecentAchievementDTO("GiorgiK", "Quiz Master",    "/img/ach/quiz_master.png",   now),
                new RecentAchievementDTO("TamaraB", "Social Butterfly","/img/ach/social.png",       now),
                new RecentAchievementDTO("LevanM",  "Speed Demon",    "/img/ach/speed.png",         now),
                new RecentAchievementDTO("MariamS", "Perfectionist",  "/img/ach/perfect.png",       now)
        );
    }
}