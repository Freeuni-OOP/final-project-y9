package org.example.y9_gaming_site.friendship;


import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;


public interface FriendshipRepository extends JpaRepository<Friendship,Long>{
    List<Friendship> findBySenderIdAndStatus(Long senderId, String status);
    List<Friendship> findByReceiverIdAndStatus(Long receiverId, String status);
}