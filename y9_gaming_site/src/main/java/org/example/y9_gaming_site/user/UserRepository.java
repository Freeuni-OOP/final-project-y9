package org.example.y9_gaming_site.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String query);

    @Modifying
    @Query("UPDATE User u SET u.points = u.points + :amount WHERE u.id = :userId")
    int addPoints(@Param("userId") Long userId, @Param("amount") int amount);

    @Modifying
    @Query("UPDATE User u SET u.points = u.points - :amount WHERE u.id = :userId AND u.points >= :amount")
    int spendPoints(@Param("userId") Long userId, @Param("amount") int amount);
}
