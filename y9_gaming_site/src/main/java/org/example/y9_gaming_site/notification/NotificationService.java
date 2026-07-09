package org.example.y9_gaming_site.notification;

import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    public void createFriendRequest(Long senderId, Long receiverId) {
        User sender = userRepository.findById(senderId).orElseThrow();

        Notification notification = new Notification(receiverId, senderId, "FRIEND_REQUEST",sender.getUsername() + " sent you a friend request");
        notificationRepository.save(notification);
    }

    public List<Notification> getNotification(Long userId) {
        return notificationRepository.findByUserIdOrderByDateTimeDesc(userId);
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    public void acceptFriendship(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);

        if(notification == null){
            throw new RuntimeException("Notification not found");
        }

        Friendship friendship = friendshipRepository.findBySenderIdAndReceiverId(notification.getSenderId(), notification.getUserId());
        if (friendship == null) {
            throw new RuntimeException("Friend request not found");
        }

        friendship.setStatus("Accepted");
        friendshipRepository.save(friendship);

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void declineFriendship(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);

        Friendship friendship = friendshipRepository.findBySenderIdAndReceiverId(notification.getSenderId(), notification.getUserId());

        if(friendship != null) {
            friendshipRepository.delete(friendship);
        }

        notificationRepository.delete(notification);
    }

    public void markRead(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByDateTimeDesc(userId);
        for(Notification notification : list) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }
}
