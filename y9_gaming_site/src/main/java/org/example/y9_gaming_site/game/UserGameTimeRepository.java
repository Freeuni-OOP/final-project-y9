package org.example.y9_gaming_site.game;

import org.example.y9_gaming_site.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserGameTimeRepository extends JpaRepository<UserGameTime, Long> {

    Optional<UserGameTime> findByUserAndGameTitle(User user, String gameTitle);

    @Query("SELECT u FROM UserGameTime u WHERE u.user.id = :userId ORDER BY u.totalTimeSeconds DESC")
    List<UserGameTime> findTop3FavoriteGames(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT u.category, SUM(u.totalTimeSeconds) FROM UserGameTime u WHERE u.user.id = :userId GROUP BY u.category ORDER BY SUM(u.totalTimeSeconds) DESC")
    List<Object[]> findTop3CategoriesByUserId(@Param("userId") Long userId, Pageable pageable);
}