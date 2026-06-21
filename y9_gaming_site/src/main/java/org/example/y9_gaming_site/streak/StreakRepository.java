package org.example.y9_gaming_site.streak;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StreakRepository extends JpaRepository<Streak, Long>{
    //get specific user's streak
    Optional<Streak> findByUserId(Long userId);
}