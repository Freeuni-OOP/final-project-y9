package org.example.y9_gaming_site.friendship;


import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;


public interface FriendshipRepository extends JpaRepository<Friendship,Long>{
    List<Friendship> findByReceiverIdAndStatus(Long receiverId, String status);
    List<Friendship> findBySenderIdAndStatus(Long senderId, String status);
    Friendship findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    List<Friendship> findByStatusAndSenderIdOrStatusAndReceiverId(String status1, Long senderId, String status2, Long receiverId);
}