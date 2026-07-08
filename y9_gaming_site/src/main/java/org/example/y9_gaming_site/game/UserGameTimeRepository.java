package org.example.y9_gaming_site.game;

import org.example.y9_gaming_site.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGameTimeRepository extends JpaRepository<UserGameTime, Long> {

    Optional<UserGameTime> findByUserAndGameTitle(User user, String gameTitle);

    @Query(value = "SELECT * FROM user_game_time WHERE user_id = :userId " +
            "ORDER BY total_time_seconds DESC LIMIT 3", nativeQuery = true)
    List<UserGameTime> findTop3FavoriteGames(@Param("userId") Long userId);

    @Query(value = "SELECT category, SUM(total_time_seconds) as total_seconds " +
            "FROM user_game_time WHERE user_id = :userId " +
            "GROUP BY category ORDER BY total_seconds DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTop5CategoriesByUserId(@Param("userId") Long userId);
}