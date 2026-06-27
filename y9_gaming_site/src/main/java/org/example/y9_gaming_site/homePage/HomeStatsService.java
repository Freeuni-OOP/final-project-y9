package org.example.y9_gaming_site.homePage;

import org.example.y9_gaming_site.admin.Announcement;
import org.example.y9_gaming_site.admin.AnnouncementRepository;
import org.example.y9_gaming_site.admin.Challenge;
import org.example.y9_gaming_site.admin.ChallengeRepository;
import org.example.y9_gaming_site.homePage.HomeStatsDTO;
import org.example.y9_gaming_site.homePage.HomeStatsDTO.RankedPlayerDTO;
import org.example.y9_gaming_site.homePage.HomeStatsDTO.RecentAchievementDTO;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

    private final UserRepository userRepository;
    private final AnnouncementRepository announcementRepository;
    private final ChallengeRepository challengeRepository;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy")
                    .withZone(ZoneId.systemDefault());

    public HomeStatsService(UserRepository userRepository,
                            AnnouncementRepository announcementRepository,
                            ChallengeRepository challengeRepository) {
        this.userRepository = userRepository;
        this.announcementRepository = announcementRepository;
        this.challengeRepository = challengeRepository;
    }
    public HomeStatsDTO getHomeStats() {
        HomeStatsDTO dto = new HomeStatsDTO();

        dto.setTotalUsers(fetchTotalUsers());
        dto.setTopPlayers(fetchTopPlayers());
        dto.setRecentAchievements(fetchRecentAchievements());
        dto.setAnnouncements(fetchAnnouncements());
        dto.setChallenges(fetchChallenges());

        return dto;
    }

    // ---------------------------------------------------------------
    // stub methods — replace with repository calls
    // ---------------------------------------------------------------

    private long fetchTotalUsers() {
        return userRepository.findAll()
                .stream()
                .filter(a -> !a.getBanned())
                .collect(Collectors.toList()).size();
    }
    private List<Announcement> fetchAnnouncements() {
        return announcementRepository.findAll(); // real DB call
    }

    private List<Challenge> fetchChallenges() {
        return challengeRepository.findAll(); // real DB call
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