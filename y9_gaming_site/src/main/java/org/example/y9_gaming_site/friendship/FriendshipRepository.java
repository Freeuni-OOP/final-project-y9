package org.example.y9_gaming_site.friendship;


import org.example.y9_gaming_site.friendship.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;


public interface FriendshipRepository extends JpaRepository<Friendship,Long>{
    List<Friendship> findBySenderIdandStatus(Long senderId, String status);
    List<Friendship> findByReceiverIdandStatus(Long receiverId, String status);
}