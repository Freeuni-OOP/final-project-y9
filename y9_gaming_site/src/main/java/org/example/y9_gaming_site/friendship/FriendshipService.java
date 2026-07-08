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
        Friendship existing1 = friendshipRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        Friendship existing2 = friendshipRepository.findBySenderIdAndReceiverId(receiverId, senderId);
        if (existing1 != null || existing2 != null) {
            throw new IllegalArgumentException("Friend request already exists");
        }

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
        if("Accepted".equals(friendship.getStatus())){
            return "FRIENDS";
        }
        return "PENDING";
    }
}
