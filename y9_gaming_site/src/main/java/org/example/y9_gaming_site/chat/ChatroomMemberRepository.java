package org.example.y9_gaming_site.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatroomMemberRepository extends JpaRepository<ChatroomMember, Long> {
    List<ChatroomMember> findByRoomId(Long roomId);
    List<ChatroomMember> findByUserId(Long userId);
    Optional<ChatroomMember> findByRoomIdAndUserId(Long roomId, Long userId);
    @Query("SELECT cm.roomId FROM ChatroomMember cm WHERE cm.userId = :user1Id " +
            "AND cm.roomId IN (SELECT cm2.roomId FROM ChatroomMember cm2 WHERE cm2.userId = :user2Id) " +
            "AND cm.roomId IN (SELECT r.id FROM ChatRoom r WHERE r.type = 'PRIVATE')")
    List<Long> findPrivateRoomIdsBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}