package org.example.y9_gaming_site.chat;

import org.example.y9_gaming_site.chat.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long>{
    List<Message> findByRoomId(Long roomId);
}