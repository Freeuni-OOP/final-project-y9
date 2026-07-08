package org.example.y9_gaming_site.game.joker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface  JokerSessionRepository extends JpaRepository<JokerSession, Long>{
    Optional<JokerSession> findByRoomCode(String roomCode);
    List<JokerSession> findByStatusAndIsOpen(String status, boolean isOpen);
}
