package org.example.y9_gaming_site.chat;

import org.example.y9_gaming_site.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatroomRepository extends JpaRepository<ChatRoom, Long> {
    ChatRoom findByUser1IdAndUser2Id(Long user1Id, Long user2Id);
}
