package org.example.y9_gaming_site.game;


import org.example.y9_gaming_site.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserGameTimeRepository extends JpaRepository<UserGameTime, Long> {

    Optional<UserGameTime> findByUserAndGameTitle(User user, String gameTitle);

    @Query(value = "SELECT * FROM user_game_time WHERE user_id = :userId ORDER BY total_time_seconds DESC LIMIT 5", nativeQuery = true)
    List<UserGameTime> findTop5ByUserIdOrderByTotalTimeSecondsDesc(@Param("userId") Long userId);
}