package org.example.y9_gaming_site.achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// Repository for tracking which achievements each user has earned.
// Acts as the connection table between User and Achievement.
public interface  UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    // Returns all achievements earned by a specific user (used on the profile page)
    List<UserAchievement> findByUserId(Long userId);
    // Checks whether a user has already earned a specific achievement,
    // used to prevent granting the same achievement twice
    Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId);

    long countByAchievementId(Long achievementId);
}
