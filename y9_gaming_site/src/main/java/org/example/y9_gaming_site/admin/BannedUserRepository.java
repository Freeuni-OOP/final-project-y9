package org.example.y9_gaming_site.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BannedUserRepository extends JpaRepository<BannedUser, Long> {
    Optional<BannedUser> findById(Long userId);
    boolean existsById(Long userId);
}