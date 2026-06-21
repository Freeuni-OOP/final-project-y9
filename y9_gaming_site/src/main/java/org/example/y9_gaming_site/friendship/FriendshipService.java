package org.example.y9_gaming_site.friendship;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    public FriendshipService(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

    public Friendship sendRequest(Long senderId, Long receiverId) {
        Friendship friendship = new Friendship(senderId,receiverId,"Pending");
        return friendshipRepository.save(friendship);
    }
    public Friendship acceptRequest(Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId).orElseThrow();
        friendship.setStatus("Accepted");
        return friendshipRepository.save(friendship);
    }
    public List<Friendship> getPendingRequests(Long userId) {
        return friendshipRepository.findByReceiverIdAndStatus(userId,"Pending");
    }

}
