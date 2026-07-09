package org.example.y9_gaming_site.notification;

import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.RequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @GetMapping("/unread-count/{userId}")
    public ResponseEntity<Integer> unreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PostMapping("/accept/{notificationId}")
    public ResponseEntity<String> accept(@PathVariable Long notificationId){
        notificationService.acceptFriendship(notificationId);
        return ResponseEntity.ok("Friendship accepted");
    }

    @PostMapping("/decline/{notificationId}")
    public ResponseEntity<String> decline(@PathVariable Long notificationId){
        notificationService.declineFriendship(notificationId);
        return ResponseEntity.ok("Friendship declined");
    }

    @PostMapping("/mark-read/{userId}")
    public ResponseEntity<String> markRead(@PathVariable Long userId){
        notificationService.markRead(userId);
        return ResponseEntity.ok("Seen");
    }
}
