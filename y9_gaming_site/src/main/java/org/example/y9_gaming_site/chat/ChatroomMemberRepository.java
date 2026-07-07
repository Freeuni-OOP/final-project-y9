package org.example.y9_gaming_site.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatroomMemberRepository extends JpaRepository<ChatroomMember, Long> {
    List<ChatroomMember> findByRoomId(Long roomId);
    List<ChatroomMember> findByUserId(Long userId);
    Optional<ChatroomMember> findByRoomIdAndUserId(Long roomId, Long userId);
}