package org.example.y9_gaming_site.friendship;

import org.example.y9_gaming_site.notification.NotificationService;
import org.example.y9_gaming_site.profile.UserProfileResponse;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    public FriendshipService(FriendshipRepository friendshipRepository, NotificationService notificationService, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Friendship sendRequest(Long senderId, Long receiverId) {
        Friendship existing1 = friendshipRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        Friendship existing2 = friendshipRepository.findBySenderIdAndReceiverId(receiverId, senderId);
        if (existing1 != null || existing2 != null) {
            throw new IllegalArgumentException("Friend request already exists");
        }

        Friendship friendship = new Friendship(senderId,receiverId,"PENDING");
        friendshipRepository.save(friendship);

        notificationService.createFriendRequest(senderId, receiverId, friendship.getId());
        return friendship;
    }

    public Friendship acceptRequest(Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId).orElseThrow();
        friendship.setStatus("ACCEPTED");
        return friendshipRepository.save(friendship);
    }
    public List<Friendship> getPendingRequests(Long userId) {
        return friendshipRepository.findByReceiverIdAndStatus(userId,"PENDING");
    }

    public String getStatus(Long myId, Long otherId){
        Friendship f1 = friendshipRepository.findBySenderIdAndReceiverId(myId,otherId);
        Friendship f2 = friendshipRepository.findBySenderIdAndReceiverId(otherId,myId);

        Friendship friendship = null;
        if(f1 != null){
            friendship = f1;
        }else if(f2 != null){
            friendship = f2;
        }
        if(friendship == null){
            return "NONE";
        }
        if("ACCEPTED".equals(friendship.getStatus())){
            return "FRIENDS";
        }
        return "PENDING";
    }

    public List<Long> getFriendIds(Long userId) {
        List<Friendship> asSender = friendshipRepository.findBySenderIdAndStatus(userId, "ACCEPTED");
        List<Friendship> asReceiver = friendshipRepository.findByReceiverIdAndStatus(userId, "ACCEPTED");

        List<Long> friendIds = new ArrayList<>();
        for (Friendship f : asSender) friendIds.add(f.getReceiverId());
        for (Friendship f : asReceiver) friendIds.add(f.getSenderId());
        return friendIds;
    }

    public List<UserProfileResponse> searchFriends(Long userId, String query) {
        List<Long> friendIds = getFriendIds(userId);
        if (friendIds.isEmpty()) return List.of();

        String needle = query.toLowerCase();
        List<UserProfileResponse> matches = new ArrayList<>();
        for (User friend : userRepository.findAllById(friendIds)) {
            if (friend.getUsername().toLowerCase().contains(needle)) {
                matches.add(new UserProfileResponse(friend.getId(), friend.getUsername(), friend.getAvatarUrl(), friend.getRole().name()));
            }
        }
        return matches;
    }

    public List<Friendship> getAcceptedFriendships(Long userId) {
        return friendshipRepository.findByStatusAndSenderIdOrStatusAndReceiverId("ACCEPTED", userId, "ACCEPTED", userId);
    }
}
