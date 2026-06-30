package org.example.y9_gaming_site.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByGameType(GameType gameType);

    Optional<Game> findByTitle(String title);
}