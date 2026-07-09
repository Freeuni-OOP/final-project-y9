package org.example.y9_gaming_site.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByDateTimeDesc(Long userId);
    int countByUserIdAndIsRead(Long userId, boolean isRead);
}
