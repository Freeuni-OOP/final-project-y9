package org.example.y9_gaming_site.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByDateTimeDesc(Long userId);
    int countByUserIdAndIsRead(Long userId, boolean isRead);
    void deleteByChallengeIdAndType(Long challengeId, String type);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}
