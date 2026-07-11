package org.example.y9_gaming_site.notification;

import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

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

    public void createFriendRequest(Long senderId, Long receiverId, Long friendshipId) {
        User sender = userRepository.findById(senderId).orElseThrow();

        Notification notification = new Notification(receiverId, senderId, "FRIEND_REQUEST",sender.getUsername() + " sent you a friend request", friendshipId);
        notificationRepository.save(notification);
    }

    public void createGameChallenge(Long receiverId, Long senderId, Long challengeId, String message) {
        Notification notification = new Notification(receiverId, senderId, "GAME_CHALLENGE", message, null);
        notification.setChallengeId(challengeId);
        notificationRepository.save(notification);
    }

    public void createGameChallengeAccepted(Long recipientId, Long otherUserId, String message) {
        Notification notification = new Notification(recipientId, otherUserId, "GAME_CHALLENGE_ACCEPTED", message, null);
        notificationRepository.save(notification);
    }

    public void createGameChallengeResult(Long recipientId, Long otherUserId, String message) {
        Notification notification = new Notification(recipientId, otherUserId, "GAME_CHALLENGE_RESULT", message, null);
        notificationRepository.save(notification);
    }

    @Transactional
    public void resolvePendingChallengeNotification(Long challengeId) {
        notificationRepository.deleteByChallengeIdAndType(challengeId, "GAME_CHALLENGE");
    }

    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByDateTimeDesc(userId);
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public void acceptFriendship(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);

        if (notification == null) {
            throw new RuntimeException("Notification not found");
        }
        if (notification.getFriendshipId() == null) {
            throw new RuntimeException("Friendship not found");
        }

        Friendship friendship = friendshipRepository.findById(notification.getFriendshipId()).orElseThrow();
        if (friendship == null) {
            throw new RuntimeException("Friend request not found");
        }

        friendship.setStatus("ACCEPTED");
        friendshipRepository.save(friendship);

        //notify the sendeerrr as welll
        Long accepterId = notification.getUserId();
        Long originalSenderId = notification.getSenderId();
        User accepter = userRepository.findById(accepterId).orElseThrow();

        Notification acceptedNotification = new Notification(
                originalSenderId,
                accepterId,
                "FRIEND_ACCEPTED",
                accepter.getUsername() + " accepted your friend request",
                friendship.getId()
        );
        notificationRepository.save(acceptedNotification);

        notification.setRead(true);
        notificationRepository.save(notification);
        notificationRepository.delete(notification);
    }

    @Transactional
    public void declineFriendship(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);

        if(notification == null){
            throw new RuntimeException("Notification not found");
        }

        if(notification.getFriendshipId() != null){
            friendshipRepository.findById(notification.getFriendshipId()).ifPresent(friendshipRepository::delete);
        }

        notificationRepository.delete(notification);
    }

    public void markRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Async
    public void createMessageNotification(Long senderId, Long receiverId, Long roomId) {
        User sender = userRepository.findById(senderId).orElseThrow();


        Notification notification = new Notification(receiverId, senderId, "NEW_MESSAGE", sender.getUsername() + " sent you a message ", roomId, true);
        notificationRepository.save(notification);
    }
}
