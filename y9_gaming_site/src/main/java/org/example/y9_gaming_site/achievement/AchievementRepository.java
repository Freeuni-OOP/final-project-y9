package org.example.y9_gaming_site.achievement;

//import org.example.y9_gaming_site.model.Achievements;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// Repository for the Achievement catalog (list of all possible achievements).
// Extending JpaRepository automatically provides save(), findById(), findAll(), delete(), etc.
// No custom queries needed here — we only need to fetch the full catalog or a single achievement by ID.
public interface  AchievementRepository extends JpaRepository<Achievement,Long> {
    Optional<Achievement> findByCode(String code);
}

